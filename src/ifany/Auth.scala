package ifany

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.json4s.*
import org.json4s.native.JsonMethods.*
import pdi.jwt.{JwtJson4s, JwtOptions}

case class Session(email: String, roles: Seq[String], permissions: Seq[String], expiry: Long) {
  def hasRole(role: String): Boolean = roles.contains(role)
  def hasPermission(perm: String): Boolean = permissions.contains(perm)
  def isExpired: Boolean = System.currentTimeMillis / 1000 > expiry
  def isTrusted: Boolean = hasPermission("private/access") || isAdmin
  def isAdmin: Boolean = hasRole("Admin")
}

object SessionCookie {

  private val secret = sys.env.getOrElse("SESSION_SECRET", "dev-secret-change-me-in-prod")
  private val cookieName = "ifany_session"
  private val maxAge = 60 * 60 * 24 * 30 // 30 days

  def sign(data: String): String = {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
    val signature = Base64.getUrlEncoder.withoutPadding.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)))
    s"$data.$signature"
  }

  def verify(cookie: String): Option[String] = {
    val lastDot = cookie.lastIndexOf('.')
    if (lastDot < 0) return None
    val data = cookie.substring(0, lastDot)
    val expected = sign(data)
    if (cookie == expected) Some(data) else None
  }

  def encode(session: Session): String = {
    val data = Base64.getUrlEncoder.withoutPadding.encodeToString(
      s"${session.email}\t${session.roles.mkString(",")}\t${session.permissions.mkString(",")}\t${session.expiry}".getBytes(StandardCharsets.UTF_8)
    )
    sign(data)
  }

  def decode(cookie: String): Option[Session] = {
    verify(cookie).flatMap { data =>
      try {
        val decoded = new String(Base64.getUrlDecoder.decode(data), StandardCharsets.UTF_8)
        val parts = decoded.split("\t", 4)
        if (parts.length == 4) {
          val roles = if (parts(1).isEmpty) Seq.empty else parts(1).split(",").toSeq
          val permissions = if (parts(2).isEmpty) Seq.empty else parts(2).split(",").toSeq
          val session = Session(parts(0), roles, permissions, parts(3).toLong)
          if (session.isExpired) None else Some(session)
        } else None
      } catch {
        case _: Exception => None
      }
    }
  }

  def setCookieHeader(session: Session): String =
    s"$cookieName=${encode(session)}; Path=/; HttpOnly; SameSite=Lax; Max-Age=$maxAge"

  def clearCookieHeader: String =
    s"$cookieName=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0"

  def fromRequest(req: unfiltered.request.HttpRequest[?]): Option[Session] = {
    val cookieHeader = req.headers("Cookie").flatMap(_.split(";").map(_.trim))
    cookieHeader
      .find(_.startsWith(s"$cookieName="))
      .map(_.substring(cookieName.length + 1))
      .flatMap(decode)
  }
}

object KindeClient {

  private val domain = sys.env.getOrElse("KINDE_DOMAIN", "")
  private val clientId = sys.env.getOrElse("KINDE_CLIENT_ID", "")
  private val clientSecret = sys.env.getOrElse("KINDE_CLIENT_SECRET", "")
  private val redirectUri = sys.env.getOrElse("KINDE_REDIRECT_URI", "http://localhost:8000/auth/callback")

  private val httpClient = HttpClient.newHttpClient()

  def authorizationUrl(returnTo: String = "/"): String = {
    val nonce = new Array[Byte](16)
    new java.security.SecureRandom().nextBytes(nonce)
    val state = Base64.getUrlEncoder.withoutPadding.encodeToString(
      (nonce ++ returnTo.getBytes(StandardCharsets.UTF_8))
    )
    s"$domain/oauth2/auth?" +
      s"response_type=code" +
      s"&client_id=${enc(clientId)}" +
      s"&redirect_uri=${enc(redirectUri)}" +
      s"&scope=${enc("openid profile email")}" +
      s"&state=${enc(state)}"
  }

  def exchangeCode(code: String): Option[Session] = {
    val body = s"grant_type=authorization_code" +
      s"&code=${enc(code)}" +
      s"&client_id=${enc(clientId)}" +
      s"&client_secret=${enc(clientSecret)}" +
      s"&redirect_uri=${enc(redirectUri)}"

    val request = HttpRequest.newBuilder()
      .uri(URI.create(s"$domain/oauth2/token"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build()

    try {
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      if (response.statusCode() != 200) {
        println(s"Token exchange failed: ${response.statusCode()} ${response.body()}")
        return None
      }

      implicit val formats: Formats = DefaultFormats
      val json = parse(response.body())
      val accessToken = (json \ "access_token").extract[String]

      // Decode the access token (email + roles + permissions enabled in Kinde dashboard)
      val accessClaim = JwtJson4s.decode(accessToken, JwtOptions(signature = false, expiration = false)).get
      val accessJson = parse(accessClaim.content)
      val roles = extractRoles(accessJson)
      val permissions = extractPermissions(accessJson)
      val email = (accessJson \ "email").extractOpt[String].getOrElse("")

      val expiry = System.currentTimeMillis / 1000 + 60 * 60 * 24 * 30 // 30 days

      Some(Session(email, roles, permissions, expiry))
    } catch {
      case e: Exception =>
        println(s"Token exchange error: ${e.getMessage}")
        e.printStackTrace()
        None
    }
  }

  // Derive site origin from the redirect URI (e.g. https://www.ifany.org)
  private val siteOrigin = {
    val uri = URI.create(redirectUri)
    s"${uri.getScheme}://${uri.getHost}${if (uri.getPort > 0) s":${uri.getPort}" else ""}"
  }

  def logoutUrl: String = s"$domain/logout?redirect=${enc(siteOrigin)}"

  private def extractRoles(claimJson: JValue): Seq[String] = {
    implicit val formats: Formats = DefaultFormats
    // Kinde puts roles as an array of objects with "key" and "name"
    (claimJson \ "roles").extractOpt[Seq[Map[String, String]]]
      .map(_.flatMap(_.get("name")))
      .getOrElse(Seq.empty)
  }

  private def extractPermissions(claimJson: JValue): Seq[String] = {
    implicit val formats: Formats = DefaultFormats
    // Kinde puts permissions as a flat array of strings
    (claimJson \ "permissions").extractOpt[Seq[String]].getOrElse(Seq.empty)
  }

  private def enc(s: String): String = URLEncoder.encode(s, StandardCharsets.UTF_8)

  def returnToFromState(state: String): String = {
    try {
      val bytes = Base64.getUrlDecoder.decode(state)
      new String(bytes.drop(16), StandardCharsets.UTF_8) // skip 16-byte nonce prefix
    } catch {
      case _: Exception => "/"
    }
  }
}
