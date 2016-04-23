package ifany

import com.dongxiguo.fastring.Fastring.Implicits._

case class AlbumTemplate(view : AlbumView) extends Template {

  implicit val v = view

  override def toString : String = Base(
    Template(navigation(view.getNav) + overlay + album + navigation(view.getNav)),
    Some(Template(javascript + nextprev))
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

  def overlay : Template = Template(fast"""
  <div class="overlay" id="overlay">
      <div class="col-xs-1 overlay-prev overlay-nav">
        <div id="overlay-prev">
          <span class="laquo">&laquo;</span>
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
          <span class="laquo">&raquo;</span>
        </div>
      </div>
  </div>
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

        <div class="album-images">
            ${ thumbnails }
        </div>
    </div>
  """)

  def thumbnails : Template = {
    val rows = for (row <- view.getRows(view.album.images)) yield row match {
      case CoverRow(image) => coverRow(image)
      case t: TwoImageRow => twoImageRow(t)
    }
    val startDiv = "<div class=\"col-xs-12 col-sm-10 col-sm-offset-1 album-row img\">"
    val endDiv = "</div>"
    Template(rows.mkString(startDiv, s"$startDiv\n$endDiv", endDiv))
  }

  def coverRow(image: Image): Template = Template(fast"""
      <span class="img-container" style="width:100%">
        <img src="${ image.url("l", view.getURL) }" id="${ image.file }"/>
      </span>
    """)

  def twoImageRow(row: TwoImageRow): Template = Template(fast"""
      <span class="img-container" style="width:${row.leftRatio*100}%">
        <img src="${ row.left.url("l", view.getURL) }"  id="${ row.left.file }"/>
      </span>
      <span class="img-container" style="width:${row.rightRatio*100}%">
        <img src="${ row.right.url("l", view.getURL) }" id="${ row.right.file }"/>
      </span>
    """)

}
