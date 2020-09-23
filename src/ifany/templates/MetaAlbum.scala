package ifany

case class MetaAlbumTemplate(view : AlbumView) extends Template {

  implicit val v = view

  val css: String = s"""<link rel="stylesheet" type="text/css" href="/css/album.css"/>"""

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
          <div id="overlay-center-box">
            <img alt="Overlay image"/>
            <div id="caption-box"><span id="caption">Sample Caption</span></div>
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

        ${ images }
    </div>
  """)

  def images : Template = {
    val rows = for (row <- view.getRows(view.album.images)) yield row match {
      case CoverRow(image) => coverRow(image)
      case t: DualRow => twoImageRow(t)
    }
    val pages = rows.grouped(100).toList.zipWithIndex.map { case (row, index) =>
      s"""<div id="page-${index + 1}" class="page">${ row.mkString("\n") }</div>""".toString
    }
    val nav: String = pageNav(pages)
    Template(nav + pages.mkString("\n") + nav)
  }

  def pageNav(pages: List[String]): String = {
    if (pages.length == 1) return ""
    val navElems = for (i <- 1 to pages.length) yield {
      s"""\n<span class="pageNavElem pageNav-$i" data-page="$i">
        <a href="#$i">$i</a>
      </span>"""
    }
    s"""
    <div class="col-sm-10 col-sm-offset-1 col-xs-12 nav-row">
      <div class="pageNav">
        <span class="pageNav-prev"><a href="javascript:;">prev</a></span>
        ${ navElems.mkString("\n") }
        <span class="pageNav-next"><a href="javascript:;">next</a></span>
      </div>
    </div>\n"""
  }

  def coverRow(image: Image, tag: String = ""): Template = Template(s"""
    <div class="col-xs-12 col-sm-10 $tag col-sm-offset-1 album-row img">
      <div class="img-box" style="width:100%">
        ${imageBox(image, 100)}
      </div>
    </div>
    """)

  def twoImageRow(row: DualRow): Template = Template(s"""
    ${coverRow(row.left, "visible-xs-block")}
    ${coverRow(row.right, "visible-xs-block")}

    <div class="col-xs-12 hidden-xs col-sm-10 col-sm-offset-1 album-row img">
      <div class="frame-box">
        <div class="img-box left" style="width:${row.leftRatio*100}%">
          ${imageBox(row.left, row.leftRatio*100)}
        </div>
        <div class="img-box right" style="width:${row.rightRatio*100}%">
          ${imageBox(row.right, row.rightRatio*100)}
        </div>
      </div>
    </div>
    """)

  def imageBox(image: Image, ratio: Double): Template = image.is_video match {
    case true => Template(s"""<video controls poster=\"${image.imageURL(view.album.url, "800")}\">
      <source class="media" id="${image.file}" src="${image.videoURL(view.album.url)}" type="video/mp4"></video>""")
    case false => 
      val srcset = for (label <- image.versions) yield s"${image.imageURL(view.album.url, label)} ${image.width(label)}w"
      Template(s"""
        <img class="media" id="${image.file}"
             src="${ image.imageURL(view.album.url, "800") }"
             srcset="${ srcset.mkString(", ") }"
             sizes="(min-width: 800px) ${ratio * 0.8}vw, 100vw"
             alt="${ image.description }">""")
  }
}
