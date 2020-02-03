package ifany

case class AlbumTemplate(view : AlbumView) extends Template {

  implicit val v = view

  val css: String = """<link rel="stylesheet" type="text/css" href="/css/album.css"/>"""

  override def toString : String = Base(
    Template(navigation(view.getNav) + overlay + album + navigation(view.getNav)),
    Some(Template(css + responsiveStyles(view) + javascript + nextprev))
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
        <span class="img-container" role="img" id="${ image.id }">
          <span class="inner" style="padding-top: ${ image.ratio*100 }%;">
          </span>
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
          <span class="img-container" role="img" id="${ row.left.id }">
            <span class="inner" style="padding-top: ${ row.left.ratio*100 }%;"></span>
          </span>
        </div>
        <div class="img-box right" style="width:${row.rightRatio*100}%">
          <span class="img-container" role="img" id="${ row.right.id }">
            <span class="inner" style="padding-top: ${ row.right.ratio*100 }%;"></span>
          </span>
        </div>
      </div>
    </div>
    """)

  def responsiveStyles(view: AlbumView): String = {
    val normalSizes: Map[Int, String] = Map(
      400 -> "400",
      600 -> "400",
      800 -> "400",
      1280 -> "600",
      1600 -> "800",
      2000 -> "1600",
      3200 -> "1600",
      4000 -> "2000",
      6400 -> "3200")
    val coverSizes: Map[Int, String] = Map(
      400 -> "400",
      600 -> "600",
      800 -> "800",
      1280 -> "1280",
      1600 -> "1600",
      2000 -> "2000",
      3200 -> "3200",
      4000 -> "original",
      6400 -> "original")

    def style(min: Option[Int], max: Option[Int]): String = {
      val size = max.getOrElse(min.get)
      val maxMarginFactor = if (size > 800) 1.0/0.8 else 1.0// images larger than 800px only take up ~80% of the screen
      val minMarginFactor = if (size > 1280) 1.0/0.8 else 1.0// images larger than 800px only take up ~80% of the screen
      val maxWidth = max.map(m => s"and (max-width: ${m*maxMarginFactor}px)").getOrElse("")
      val minWidth = min.map(m => s"and (min-width: ${m*minMarginFactor}px)").getOrElse("")
      val covers: Set[String] = view.album.images.filter(_.cover).map(_.file).toSet ++ Set(view.album.images.last.file)
      val css = for (image <- view.album.images) yield covers.contains(image.file) match {
        case true => s"#${image.id} { background-image: url(${ image.url(coverSizes(size), view.getURL) }); }"
        case false => s"#${image.id} { background-image: url(${ image.url(normalSizes(size), view.getURL) }); }"
      }
      css.mkString(s"\n\t\t@media only screen $minWidth $maxWidth {\n\t\t\t", "\n\t\t\t", "\n\t\t}")
    }

    val styles = Seq(
      style(None, Some(400)),
      style(Some(400), Some(600)),
      style(Some(600), Some(800)),
      style(Some(800), Some(1280)),
      style(Some(1280), Some(1600)),
      style(Some(1600), Some(2000)),
      style(Some(2000), Some(3200)),
      style(Some(3200), Some(4000)),
      style(Some(4000), Some(6400)),
      style(Some(6400), None))

    styles.mkString("<style>", "\n", "</style>")
  }
}
