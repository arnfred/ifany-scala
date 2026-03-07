package ifany

case class PendingApprovalView(email: String) extends View {
  val name = "frontpage"
  def getTitle: String = "Pending Approval"
}

case class PendingApprovalTemplate(email: String) extends Template {

  val view: PendingApprovalView = PendingApprovalView(email)
  given View = view

  val css: String = """<link rel="stylesheet" type="text/css" href="/css/frontpage.css"/>"""

  override def toString: String = Base(Template(body), Some(Template(css)), Some(Session(email, Seq.empty, Seq.empty, 0)))

  def body: String = s"""
    <div class="row" style="margin-top: 80px; text-align: center;">
      <div class="col-sm-6 col-sm-offset-3">
        <h2>Pending Approval</h2>
        <p style="margin: 20px 0; color: #666;">
          You're logged in as <strong>$email</strong>, but your account
          hasn't been approved yet. Jonas will review your signup shortly.
        </p>
        <a href="/auth/logout"
           style="display: inline-block; margin: 20px 0; padding: 12px 32px;
                  background: #333; color: #fff; text-decoration: none;
                  font-size: 16px;">
          Log out
        </a>
      </div>
    </div>
  """
}
