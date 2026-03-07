package ifany

case class LoginRequiredView(albumPath: String) extends View {
  val name = "frontpage"
  def getTitle: String = "Login Required"
}

case class LoginRequiredTemplate(albumPath: String) extends Template {

  val view: LoginRequiredView = LoginRequiredView(albumPath)
  given View = view

  val css: String = """<link rel="stylesheet" type="text/css" href="/css/frontpage.css"/>"""

  override def toString: String = Base(Template(body), Some(Template(css)), None)

  def loginUrl: String = s"/auth/login?return_to=${java.net.URLEncoder.encode(albumPath, "UTF-8")}"

  def body: String = s"""
    <div class="row" style="margin-top: 80px; text-align: center;">
      <div class="col-sm-6 col-sm-offset-3">
        <h2>Login Required</h2>
        <p style="margin: 20px 0; color: #666;">
          This album requires you to log in before viewing.
        </p>
        <a href="$loginUrl"
           style="display: inline-block; margin: 20px 0; padding: 12px 32px;
                  background: #333; color: #fff; text-decoration: none;
                  font-size: 16px;">
          Log in to view
        </a>
      </div>
    </div>
  """
}
