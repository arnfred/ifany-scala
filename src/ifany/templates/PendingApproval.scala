package ifany

import scalatags.Text.all.*

case class PendingApprovalView(email: String) extends View {
  val name = "frontpage"
  def getTitle: String = "Pending Approval"
}

object PendingApprovalTemplate {

  def apply(email: String): String = {
    val view = PendingApprovalView(email)
    val session = Some(Session(email, Seq.empty, Seq.empty, 0))

    Base.page(view, session, body = div(cls := "mt-20 text-center",
      div(cls := "max-w-lg mx-auto",
        h2(cls := "text-2xl font-light", "Pending Approval"),
        p(cls := "my-5 text-site-muted",
          "You're logged in as ", strong(email), ", but your account hasn't been approved yet. Jonas will review your signup shortly."
        ),
        a(href := "/auth/logout",
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
          "Log out"
        )
      )
    ))
  }
}
