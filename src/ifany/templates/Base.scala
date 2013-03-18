package ifany

import com.dongxiguo.fastring.Fastring.Implicits._

case class Base(body : Template, header : Option[Template] = None) extends Template {

  implicit val view = body.view

  override def toString : String = fast"""

    <!DOCTYPE html>
    <html lang="en">
    <head>

      <title>&laquo; If Any &raquo; ${ view.getTitle }</title>
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <link rel="icon" type="image/png" href="/img/favicon.png"/>
      <link rel="stylesheet" type="text/css" href="/css/bootstrap.min.css"/>
      <link rel="stylesheet" type="text/css" href="/css/bootstrap-responsive.min.css"/>
      <link rel="stylesheet" type="text/css" href="/css/global.css"/>
      <link rel="stylesheet" type="text/css" href="/css/${ view.name }.css"/>

      ${ if (header != None) header.get else "" }

      <script type="text/javascript" src="/js/lib/curl/curl.js"></script>
      <script type="text/javascript" src="/js/controllers/${ view.name }.js"></script>

    </head>
    <body>

      $body

      <div class="row-fluid">
        <div class="span7 offset4" id="credits">
          <p>Design, code and photos by <a href="mailto:jonas@ifany.org"
            alt="jonas@ifany.org">Jonas Arnfred</a><p>
        </div>
      </div>
    </body>
    </html>
  """
}
