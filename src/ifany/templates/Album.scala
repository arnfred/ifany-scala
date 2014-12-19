package ifany

case class AlbumTemplate(view : AlbumView) extends Template {

  // Implicit conversion 
  import com.dongxiguo.fastring.Fastring.Implicits._
  implicit val v = view

  override def toString : String = Base(
    Template(navigation(view.getNav) + overlay + album), 
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

      <div class="row-fluid visible-phone navigation">
          <div class="album-nav prev span4 offset1">
            ${ prevPhone.getOrElse("")  }
          </div>
          <div class="home album-nav span2">
            ${ getHomeLink("Home", "") }
          </div>
          <div class="album-nav next span4">
            ${ nextPhone.getOrElse("") }
          </div>
      </div>

      <div class="row-fluid hidden-phone navigation">
          <div class="album-nav prev span4 offset1">
            ${ prev.getOrElse("")  }
          </div>
          <div class="home album-nav span2">
            ${ getHomeLink("Home", "") }
          </div>
          <div class="album-nav next span4">
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
  <div class="row-fluid overlay" id="overlay">
      <div class="span1 overlay-prev overlay-nav hidden-phone">
        <div id="overlay-prev">
          <span class="laquo">&laquo;</span>
        </div>
      </div>
      <div class="span10 overlay-img" id="overlay-img">
          <div>
            <img alt="Overlay image"/>
            <span id="caption">Sample Caption</span>
          </div>
      </div>
      <div class="span1 overlay-next overlay-nav hidden-phone">
        <div id="overlay-next">
          <span class="laquo">&raquo;</span>
        </div>
      </div>
  </div>
  """)

  def album : Template = Template(fast"""
    <div class="row-fluid album top">
        <div class="span3 offset1 album-info">
            <h2 class="album-title">${ view.getTitle }</h2>
            <p class="album-date">${ view.getDateString }</p>
            <p class="album-desc">${ view.getDescription }</p>
            <br class="clear" />
        </div>

	    <div class="span7 album-images">
            ${ thumbnails }
        </div>
    </div>
  """)

  def thumbnails : Template = Template({
    for (row <- view.getThumbnailRows) yield fast"""
      <div class="row-fluid album-row">
        ${ thumbnailRow(row) }
      </div>
    """}.mkString
  )

  def thumbnailRow(row : List[Image]) : Template = Template({
    for (thumb <- row) yield fast"""
      <div class="span3 img">
          <img src="${ thumb.url("t", view.getURL) }" id="${ thumb.file }" class="frame"/>
      </div>
    """}.mkString
  )
}
