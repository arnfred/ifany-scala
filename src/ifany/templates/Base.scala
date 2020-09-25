package ifany

case class Base(body : Template, header : Option[Template] = None) extends Template {

  implicit val view = body.view

  override def toString : String = s"""

    <!DOCTYPE html>
    <html lang="en">
    <head>

      <title>&laquo; If Any &raquo; ${ view.getTitle }</title>
      <meta name="description" content="Photos by Jonas Toft Arnfred">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <link rel="icon" type="image/png" href="/img/favicon.png"/>
      <link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css"/>
      <link rel="stylesheet" type="text/css" href="/css/global.css"/>

      ${ header.getOrElse("") }

      <!-- css3-mediaqueries.js for IE less than 9 -->
      <!-- [if lt IE 9]>
      <script src="http://css3-mediaqueries-js.googlecode.com/svn/trunk/css3-mediaqueries.js"></script>
      <![endif]-->
      <script type="text/javascript" src="/js/lib/curl/curl.js"></script>
      <script type="text/javascript" src="/js/controllers/${ view.name }.js"></script>

    </head>
    <body>

      <div class="container-fluid">
        $body

        <div class="row">
          <div class="col-sm-7 col-sm-offset-4" id="credits">
            <p>Design, code and photos by <a href="mailto:jonas@ifany.org"
              alt="jonas@ifany.org">Jonas Arnfred</a><p>
          </div>
        </div>
      </div>
    </body>
    </html>
  """
}
