package ifany

import scalatags.Text.all.*

case class LoginRequiredView(albumPath: String) extends View {
  val name = "frontpage"
  def getTitle: String = "Login Required"
}

object LoginRequiredTemplate {

  def apply(albumPath: String): String = {
    val view = LoginRequiredView(albumPath)
    val loginUrl = s"/auth/login?return_to=${java.net.URLEncoder.encode(albumPath, "UTF-8")}"

    Base.page(view, None, body = div(cls := "mt-20 text-center",
      div(cls := "max-w-lg mx-auto",
        h2(cls := "text-2xl font-light", "Login Required"),
        p(cls := "my-5 text-site-muted",
          "This album requires you to log in before viewing."
        ),
        a(href := loginUrl,
          cls := Seq(
            "inline-block",
            "my-5",
            "px-8",
            "py-3",
            "bg-site-text",
            "text-site-bg",
            "no-underline",
            "text-base",
            "hover:opacity-80"
          ).mkString(" "),
          "Log in to view"
        )
      )
    ))
  }
}
