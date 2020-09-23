package ifany

case class AlbumTemplate(view : AlbumView) extends Template {

  implicit val v = view

  val css: String = """<link rel="stylesheet" type="text/css" href="/css/album.css"/>"""
  val covers: Set[String] = view.album.images.filter(_.cover).map(_.file).toSet ++ Set(view.album.images.last.file)

  override def toString : String = Base(
    Template(navigation(view.getNav) + overlay + album + navigation(view.getNav)),
    Some(Template(css + javascript + nextprev))
  )

  def nextprev : Template = {
    val next = for (n <- view.getNav.next) yield n.url
    val prev = for (p <- view.getNav.prev) yield p.url
    Template(s"""
      <link rel="next" href="${ next.getOrElse("/") }"/>
      <link rel="prev" href="${ prev.getOrElse("/") }"/>
    """)
  }

  def javascript : Template = Template(s"""
    <script type="text/javascript">data = ${ view.getJson }</script>
  """)

  def navigation(nav : Navigation) : Template = {
    val prevPhone = for (p <- nav.prev) yield getLink("Older", "/" + p.url + "/", "ᐊ")
    val nextPhone = for (n <- nav.next) yield getLink("Newer", "/" + n.url + "/", "ᐅ")
    val prev = for (p <- nav.prev) yield getLink(p.title, "/" + p.url + "/", "ᐊ")
    val next = for (n <- nav.next) yield getLink(n.title, "/" + n.url + "/", "ᐅ")
    Template(s"""

      <div class="row visible-xs-block navigation">
          <div class="album-nav prev col-xs-5 col-sm-offset-1">
            ${ prevPhone.getOrElse("")  }
          </div>
          <div class="home album-nav col-xs-2 col-sm-2">
            ${ getHomeLink("Home", "") }
          </div>
          <div class="album-nav next col-xs-5">
            ${ nextPhone.getOrElse("") }
          </div>
      </div>

      <div class="row hidden-xs navigation">
          <div class="album-nav prev col-xs-4 col-sm-offset-1">
            ${ prev.getOrElse("")  }
          </div>
          <div class="home album-nav col-sm-2">
            ${ getHomeLink("Home", "") }
          </div>
          <div class="album-nav next col-xs-4">
            ${ next.getOrElse("") }
          </div>
      </div>
    """)
  }

  def getHomeLink(text : String, url : String) : Template = Template(s"""
    <a href="/$url"><span class="nav home">$text</span></a>
  """)

  def getLink(text : String, url : String, sign : String) : Template = Template(s"""
    <a href="$url" alt="$text">
      <span class="laquo">$sign</span>
      <span class="nav other">$text</span>
    </a>
  """)

  def overlay : Template = Template(s"""
  <div class="overlay" id="overlay">
      <div class="col-xs-1 overlay-prev overlay-nav">
        <div id="overlay-prev">
          <span class="laquo">ᐊ</span>
        </div>
      </div>
      <div class="col-xs-10 overlay-img" id="overlay-img">
          <div>
            <img alt="Overlay image"/>
            <span id="caption">Sample Caption</span>
          </div>
      </div>
      <div class="col-xs-1 overlay-next overlay-nav">
        <div id="overlay-next">
          <span class="laquo">ᐅ</span>
        </div>
      </div>
  </div>
  """)

  def album : Template = Template(s"""
    <div class="row album top">
        <div class="col-sm-4 col-sm-offset-4 album-info">
          <h2 class="album-title">${ view.getTitle }</h2>
          <p class="album-galleries">${ view.getGalleries }</p>
          <p class="album-desc">${ view.getDescription }</p>
          <p class="album-date">${ view.getDateString }</p>
          <br class="clear" />
        </div>

        ${ thumbnails }
    </div>
  """)

  def thumbnails : Template = {
    val rows = for (row <- view.getRows(view.album.images)) yield row match {
      case CoverRow(image) => coverRow(image)
      case t: DualRow => twoImageRow(t)
    }
    Template(rows.mkString("\n"))
  }


  def coverRow(image: Image, tag: String = ""): Template = Template(s"""
    <div class="col-xs-12 col-sm-10 $tag col-sm-offset-1 album-row img">
      <div class="img-box" style="width:100%">
          ${imageBox(image, 100.0)}
        </span>
      </div>
    </div>
    """)

  def twoImageRow(row: DualRow): Template = Template(s"""
    ${coverRow(row.left, "visible-xs-block")}
    ${coverRow(row.right, "visible-xs-block")}

    <div class="col-xs-12 hidden-xs col-sm-10 col-sm-offset-1 album-row img">
      <div class="frame-box">
        <div class="img-box left" style="width:${row.leftRatio*100}%;">
          ${imageBox(row.left, row.leftRatio*100)}
        </div>
        <div class="img-box right" style="width:${row.rightRatio*100}%">
          ${imageBox(row.right, row.rightRatio*100)}
        </div>
      </div>
    </div>
    """)

  def imageBox(image: Image, ratio: Double): Template = image.is_video match {
    case true => Template(s"""<video controls poster=\"${view.imageURL(image, "original")}\">
      <source src="${view.videoURL(image)}" type="video/mp4"></video>""")
    case false => 
      val srcset = for (label <- image.versions) yield s"${view.imageURL(image, label)} ${image.width(label)}w"
      Template(s"""
        <img src="${ view.imageURL(image, "800") }"
             srcset="${ srcset.mkString(", ") }"
             sizes="(min-width: 800px) ${ratio * 0.8}vw, 100vw"
             alt="${ image.description }">""")
  }
}
