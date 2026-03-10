package ifany

import scalatags.Text.all.*
import scalatags.Text.tags2

object Base {

  def page(view: View, session: Option[Session],
           headerExtra: Seq[Frag] = Seq.empty, body: Frag): String =
    "<!DOCTYPE html>\n" + html(lang := "en",
      head(
        tags2.title(raw("&laquo; If Any &raquo; "), view.getTitle),
        meta(name := "description", content := "Photos by Jonas Toft Arnfred"),
        meta(name := "robots", content := "noai, noimageai"),
        meta(name := "viewport", content := "width=device-width, initial-scale=1.0"),
        link(rel := "icon", `type` := "image/png", href := "/img/favicon.png"),
        link(rel := "stylesheet", href := "/css/site.css"),
        script(src := "https://unpkg.com/htmx.org@2.0.4"),
        headerExtra
      ),
      tag("body")(cls := Seq(
        "bg-site-bg",
        "text-site-text",
        "font-sans"
      ).mkString(" "),
        div(
          body,
          div(cls := Seq(
            "max-w-[1200px]",
            "mx-auto",
            "text-right",
            "text-sm",
            "italic",
            "text-site-muted",
            "px-2",
            "py-8"
          ).mkString(" "),
            "Design, code and photos by ",
            a(href := "mailto:jonas@ifany.org", cls := Seq(
              "text-site-link",
              "hover:text-site-link-hover"
            ).mkString(" "), "Jonas Arnfred"),
            session match {
              case Some(s) => frag(raw(" &middot; "), s.email, " (", a(href := "/auth/logout", cls := Seq(
                "text-site-link",
                "hover:text-site-link-hover"
              ).mkString(" "), "log out"), ")")
              case None => frag(raw(" &middot; "), a(href := "/auth/login", cls := Seq(
                "text-site-link",
                "hover:text-site-link-hover"
              ).mkString(" "), "log in"))
            }
          )
        )
      )
    ).render
}
