package ifany

import unfiltered.request.*
import unfiltered.response.*
import unfiltered.netty.*
import scala.concurrent.*
import ExecutionContext.Implicits.global

@io.netty.channel.ChannelHandler.Sharable
object AuthPlan extends async.Plan with ServerErrorResponse {

  def intent = {

    // Login page at /login — shows login page or redirects home if already logged in
    case req @ Path(Seg("login" :: Nil)) => {
      val session = SessionCookie.fromRequest(req)
      session match {
        case Some(_) => req.respond(Redirect("/"))
        case None =>
          val output = LoginRequiredTemplate("/").toString
          req.respond(HtmlContent ~> ResponseString(output))
      }
    }

    // Login: redirect to Kinde with return URL
    case req @ Path(Seg("auth" :: "login" :: Nil)) => {
      val returnTo = req.parameterNames
        .find(_ == "return_to")
        .flatMap(n => req.parameterValues(n).headOption)
        .getOrElse("/")
      val url = KindeClient.authorizationUrl(returnTo)
      req.respond(Redirect(url))
    }

    // Callback: exchange code for tokens, set session cookie
    case req @ Path(Seg("auth" :: "callback" :: Nil)) => {
      val code = req.parameterValues("code").headOption
      val state = req.parameterValues("state").headOption.getOrElse("")
      val returnTo = KindeClient.returnToFromState(state)

      code.flatMap(KindeClient.exchangeCode) match {
        case Some(session) =>
          val cookie = SessionCookie.setCookieHeader(session)
          req.respond(
            Found ~> Location(returnTo) ~> ResponseHeader("Set-Cookie", List(cookie))
          )
        case None =>
          req.respond(
            Redirect(s"/auth/login?return_to=${java.net.URLEncoder.encode(returnTo, "UTF-8")}")
          )
      }
    }

    // Logout: clear cookie and redirect to Kinde logout
    case req @ Path(Seg("auth" :: "logout" :: Nil)) => {
      req.respond(
        Found ~> Location(KindeClient.logoutUrl) ~> ResponseHeader("Set-Cookie", List(SessionCookie.clearCookieHeader))
      )
    }
  }
}
