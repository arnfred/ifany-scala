package ifany

case class FrontpageTemplate(view : FrontpageView) extends Template {

  // Implicit conversion 
  implicit val v = view

  val css: String = s"""<link rel="stylesheet" type="text/css" href="/css/frontpage.css"/>"""

  override def toString : String = Base(Template(header + galleries), Some(Template(css)))

  val coverSrcset = for (label <- view.cover.image.versions) yield {
    s"${view.cover.image.imageURL(view.cover.album.url, label)} ${view.cover.image.width(label)}w"
  }

  def header : Template = Template(s"""
    <div class="row top topmost">
        <div class="col-md-3 col-md-offset-1" id="about">
            <h1 id="header"><span><span id="if">if</span><span id="any">any</span></span></h1>
            <h4 id="subheader"><span>photography</span></h4>

            <p id="about-text">The photos on this site are an ongoing collection of things, people
            and places that happen to stand in my way the moment I press the
            shutter. They are licensed under a copyleft license (<a
                href="http://creativecommons.org/licenses/by-sa/3.0/" alt="Creative
                Commons CC-BY-SA">CC-BY-SA</a>) which means that as long as you
            credit me and keep any deriviative work under the same license, you can
            use the photos for whatever you want. Check out the meta albums of <a href="/all">all</a>, 
            <a href="/random">random</a> and <a href="/covers">cover</a> photos. 
            Inquiries and fawning fan mail are all welcome at <a
                href="mailto:jonas@ifany.org"
                alt="jonas@ifany.org">jonas@ifany.org</a>.</p>
                <p id="my-name"><a mailto="jonas@ifany.org">Jonas Arnfred</a></p>

        </div>

        <div class="col-md-7 hidden-xs" id="image">

          <img src="${ view.cover.image.imageURL(view.cover.album.url, "800") }"
          srcset="${ coverSrcset.mkString(", ") }"
          sizes="(min-width: 800px) 50vw, 100vw"
          alt="${ view.cover.image.description }" class="frame">
          <p>From the album "<a href="${ view.cover.album.path }/" >${ view.cover.album.title }</a>"</p>
        </div>
    </div>
  """)

  def galleries : Template = Template({
    for (g <- view.getGalleries) yield {
       val cover = view.getGalleryCover(g)
       gallery(g, cover) + galleryAlbums(g)
    }
  }.mkString)

  def gallery(g : Gallery, cover : Cover) : Template = Template { 
    val albumNum : Int = g.albums.size
    val imagesNum : Int = view.getGallerySize(g)
    s"""
      <div class="row category">
          <div class="col-sm-3 col-xs-12 col-sm-offset-1 cat-image">
              <img src="${ cover.image.imageURL(cover.album.url, "s") }" class="frame"/>
          </div>

          <div class="col-sm-7 col-xs-12 cat-info">
              <h2 class="cat-title">${ g.name }</h2>
              <p class="cat-date">${ view.getGalleryDateString(g) }.</p>
              <p class="cat-meta">This gallery contains 
                <span class="num">${ albumNum }</span> 
                ${ if (albumNum == 1) "album" else "albums" } with 
                <span class="num">${ imagesNum }</span> images.
              </p>
              <p class="cat-desc">${ g.description }</p>
          </div>
      </div>
    """
  }

  def galleryAlbums(g : Gallery) : Template = Template {
    (for (album <- g.albums.reverse) yield s"""
      <div class="row album album-hidden">
        <a href="/${ g.url }/${ album.url }/">
          <div class="col-sm-3 col-sm-offset-1 album-info hidden-xs">
            <h3 class="album-title">${ album.title }</h3>
            <p class="album-date">${ view.getAlbumDateString(album) }</p>
            <p class="album-meta">
              <span class="num">${ view.getAlbumSize(album) }</span> 
              ${ if (view.getAlbumSize(album) == 1) "Image" else "Images" } 
            </p>
          </div>
          <div class="col-sm-3 col-sm-offset-1 visible-xs">
            <h3 class="album-title">${ album.title }</h3>
            <p class="album-date album-date-small">${ view.getAlbumDateString(album) }.
            <span class="album-meta"><span class="num">${ view.getAlbumSize(album) }</span> 
              ${ if (view.getAlbumSize(album) == 1) "Image" else "Images" } 
            </span></p>
          </div>

          <div class="col-sm-7 album-images">
            <div class="row">
              ${ albumThumbnails(album) }
            </div>
          </div>
          <div class="col-sm-1 album-arrow hidden-xs">
            <p>ᐅ</p>
          </div>
        </a>
      </div>
    """).mkString
  }

  def albumThumbnails(album : Album) : Template = Template {
    val images = view.getAlbumImages(album, 4)
    val first = (for (image <- images.take(3)) yield s"""
      <div class="col-xs-4 col-sm-3 img">
        <img href="${ image.imageURL(album.url, "t") }" class="frame" src="/img/loader.gif"/>
      </div>
    """).mkString
    val last = s"""
      <div class="col-sm-3 hidden-xs img">
        <img href="${ images.last.imageURL(album.url, "t") }" class="frame" src="/img/loader.gif"/>
      </div>"""
    first + last
  }
}
