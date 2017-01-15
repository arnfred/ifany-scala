package ifany

import com.dongxiguo.fastring.Fastring.Implicits._

case class MetaAlbumTemplate(view : AlbumView) extends Template {

  implicit val v = view

  val css: String = fast"""<link rel="stylesheet" type="text/css" href="/css/album.css"/>"""

  override def toString : String = Base(
    Template(navigation(view.getNav) + album + navigation(view.getNav)),
    Some(Template(css + responsiveStyles(view) + javascript + nextprev))
  )

  def nextprev : Template = {
    val next = for (n <- view.getNav.next) yield n.url
    val prev = for (p <- view.getNav.prev) yield p.url
    Template(fast"""
      <link rel="next" href="${ next.getOrElse("/") }"/>
      <link rel="prev" href="${ prev.getOrElse("/") }"/>
    """)
  }

  def javascript : Template = Template(fast"""
    <script type="text/javascript">data = ${ view.getJson }</script>
  """)

  def navigation(nav : Navigation) : Template = {
    val prevPhone = for (p <- nav.prev) yield getLink("Older", "/" + p.url + "/", "&laquo;")
    val nextPhone = for (n <- nav.next) yield getLink("Newer", "/" + n.url + "/", "&raquo;")
    val prev = for (p <- nav.prev) yield getLink(p.title, "/" + p.url + "/", "&laquo;")
    val next = for (n <- nav.next) yield getLink(n.title, "/" + n.url + "/", "&raquo;")
    Template(fast"""

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

  def getHomeLink(text : String, url : String) : Template = Template(fast"""
    <a href="/$url"><span class="nav home">$text</span></a>
  """)

  def getLink(text : String, url : String, sign : String) : Template = Template(fast"""
    <a href="$url" alt="$text">
      <span class="laquo">$sign</span>
      <span class="nav other">$text</span>
    </a>
  """)

  def album : Template = Template(fast"""
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
      fast"""<div id="page-${index + 1}" class="page">${ row.mkString("\n") }</div>""".toString
    }
    val nav: String = pageNav(pages)
    Template(nav + pages.mkString("\n") + nav)
  }

  def pageNav(pages: List[String]): String = {
    if (pages.length == 1) return ""
    val navElems = for (i <- 1 to pages.length) yield {
      fast"""\n<span class="pageNavElem pageNav-$i" data-page="$i">
        <a href="#$i">$i</a>
      </span>"""
    }
    fast"""
    <div class="col-sm-10 col-sm-offset-1 col-xs-12 nav-row">
      <div class="pageNav">
        <span class="pageNav-prev"><a href="javascript:;">prev</a></span>
        ${ navElems.mkString("\n") }
        <span class="pageNav-next"><a href="javascript:;">next</a></span>
      </div>
    </div>\n"""
  }

  def coverRow(image: Image, tag: String = ""): Template = Template(fast"""
    <div class="col-xs-12 col-sm-10 $tag col-sm-offset-1 album-row img">
      <div class="img-box" style="width:100%">
        <span class="img-container" role="img" id="${ image.id }">
          <span class="inner" style="padding-top: ${ image.ratio*100 }%;">
          </span>
        </span>
      </div>
    </div>
    """)

  def twoImageRow(row: DualRow): Template = Template(fast"""
    ${coverRow(row.left, "visible-xs-block")}
    ${coverRow(row.right, "visible-xs-block")}

    <div class="col-xs-12 hidden-xs col-sm-10 col-sm-offset-1 album-row img">
      <div class="img-box" style="width:${row.leftRatio*100}%">
        <span class="img-container" role="img" id="${ row.left.id }">
          <span class="inner" style="padding-top: ${ row.left.ratio*100 }%;">
          </span>
        </span>
      </div>
      <div class="img-box" style="width:${row.rightRatio*100}%">
        <span class="img-container" role="img" id="${ row.right.id }">
          <span class="inner" style="padding-top: ${ row.right.ratio*100 }%;">
          </span>
        </span>
      </div>
    </div>
    """)

  def responsiveStyles(view: AlbumView): String = {
    val normalSizes: Map[String, String] = Map(
      "400" -> "400",
      "600" -> "400",
      "800" -> "400",
      "1200" -> "600",
      "1600" -> "800",
      "3200" -> "1600",
      "4000" -> "2000")
    val coverSizes: Map[String, String] = Map(
      "400" -> "400",
      "600" -> "600",
      "800" -> "800",
      "1200" -> "1280",
      "1600" -> "1600",
      "3200" -> "2000",
      "4000" -> "2000")

    def style(min: Option[String], max: Option[String]): String = {
      val size = max.getOrElse(min.get)
      val maxWidth = max.map(m => s"and (max-width: ${m}px)").getOrElse("")
      val minWidth = min.map(m => s"and (min-width: ${m}px)").getOrElse("")
      val covers: Set[String] = view.album.images.filter(_.cover).map(_.file).toSet ++ Set(view.album.images.last.file)
      val css = for (image <- view.album.images) yield covers.contains(image.file) match {
        case true => s"#${image.id} { background-image: url(${ image.url(coverSizes(size), view.getURL) }); }"
        case false => s"#${image.id} { background-image: url(${ image.url(normalSizes(size), view.getURL) }); }"
      }
      css.mkString(s"\n\t\t@media only screen $minWidth $maxWidth {\n\t\t\t", "\n\t\t\t", "\n\t\t}")
    }

    val styles = List(
      style(None, Some("400")),
      style(Some("400"), Some("600")),
      style(Some("600"), Some("800")),
      style(Some("800"), Some("1200")),
      style(Some("1200"), Some("1600")),
      style(Some("1600"), Some("3200")),
      style(Some("3200"), Some("4000")),
      style(Some("4000"), None))

    styles.mkString("<style>", "\n", "</style>")
  }
}
