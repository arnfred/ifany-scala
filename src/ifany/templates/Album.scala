package ifany

case class AlbumTemplate(view : AlbumView) extends Template {

  // Implicit conversion 
  import com.dongxiguo.fastring.Fastring.Implicits._
  implicit val v = view

  override def toString : String = Base(
    Template(navigation(view.getNextAlbum, view.getPrevAlbum) + overlay + album), 
    Some(javascript)
  )

  def javascript : Template = Template(fast"""
    <script type="text/javascript">data = ${ view.getJson }</script>
  """)

  def navigation(next : Option[NavElem], prev : Option[NavElem]) : Template = Template(fast"""

    <div class="row-fluid visible-phone">
        <div class="span4 offset1 album-nav next">
          ${ if (next != None) getLink("Newer", "/" + next.get.url + "/", "&laquo;") else "" }
        </div>
        <div class="span2 home album-nav">
          <a href="/"><span class="nav">Home</span></a>
        </div>
        <div class="span4 album-nav prev">
          ${ if (prev != None) getLink("Older", "/" + prev.get.url + "/", "&raquo;") else "" }
        </div>
    </div>

    <div class="row-fluid hidden-phone">
        <div class="span4 offset1 album-nav next">
          ${ if (next != None) getLink(next.get.title, "/" + next.get.url + "/", "&laquo;") else "" }
        </div>
        <div class="span2 home album-nav">
          <a href="/"><span class="nav">Home</span></a>
        </div>
        <div class="span4 album-nav prev">
          ${ if (prev != None) getLink(prev.get.title, "/" + prev.get.url + "/", "&raquo;") else "" }
        </div>
    </div>
  """)

  def getLink(text : String, url : String, sign : String) : Template = Template(fast"""
    <a href="$url" alt="$text">
      <span class="laquo">$sign</span>
      <span class="nav">$text</span>
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

  def thumbnailRow(row : List[ImageData]) : Template = Template({
    for (thumb <- row) yield fast"""
      <div class="span3 img">
          <img src="${ view.getImgUrl(thumb, "thumbnail") }" id="${ thumb.file }" class="frame"/>
      </div>
    """}.mkString
  )
}
