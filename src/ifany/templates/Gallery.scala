package ifany

case class GalleryTemplate(view : GalleryView) extends Template {

  // Implicit conversion 
  implicit val v = view

  val css: String = s"""<link rel="stylesheet" type="text/css" href="/css/frontpage.css"/>"""

  override def toString : String = Base(
    Template(navigation(view.getNav) + header + gallery),
    Some(Template(css + nextprev))
  )

  def nextprev : Template = {
    val next = for (n <- view.getNav.next) yield n.url
    val prev = for (p <- view.getNav.prev) yield p.url
    Template(s"""
      <link rel="next" href="${ next.getOrElse("/") }"/>
      <link rel="prev" href="${ prev.getOrElse("/") }"/>
    """)
  }

  def navigation(nav : Navigation) : Template = {
    val prevPhone = for (p <- nav.prev) yield getLink("Older", "/" + p.url + "/", "ᐊ")
    val nextPhone = for (n <- nav.next) yield getLink("Newer", "/" + n.url + "/", "ᐅ")
    val prev = for (p <- nav.prev) yield getLink(p.title, "/" + p.url + "/", "ᐊ")
    val next = for (n <- nav.next) yield getLink(n.title, "/" + n.url + "/", "ᐅ")
    val t = Template(s"""

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
    if (prev != None || next != None) t
    else Template("")
  }

  def getHomeLink(text : String, url : String) : Template = Template(s"""
    <a href="/"><span class="nav home">$text</span></a>
  """)

  def getLink(text : String, url : String, sign : String) : Template = Template(s"""
    <a href="$url" alt="$text">
      <span class="laquo">$sign</span>
      <span class="nav other">$text</span>
    </a>
  """)

  def header : Template = Template {
    val albumNum : Int = view.gallery.albums.size
    val imagesNum : Int = view.getSize
    s"""
    <div class="row top topmost">
        <div class="col-sm-3 col-sm-offset-1" id="about">
          <h1 id="gallery-name"><span>${ view.getTitle }</span></h1>

            <p class="cat-date">${ view.getDateString }.</p>
            <p class="cat-meta">This gallery contains 
              <span class="num">${ albumNum }</span> 
              ${ if (albumNum == 1) "album" else "albums" } with 
              <span class="num">${ imagesNum }</span> images.
            </p>
            <p id="gallery-desc">${ view.getDescription }</p>

        </div>
        <div class="col-sm-7" id="image">
            <img style="background-image:url('${ view.cover.image.imageURL(view.cover.album.url, "l") }')" id="gallery-cover"/>
            <p>From the album "<a href="${ view.cover.album.url }/" >${ view.cover.album.title }</a>"</p>
        </div>

    </div>
    """
  }


  def gallery : Template = Template {
    (for (album <- view.gallery.albums) yield s"""
      <div class="row album">
        <a href="/${ view.gallery.url }/${ album.url }/">
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
        <img src="${ image.imageURL(album.url, "t") }" class="frame"/>
      </div>
    """).mkString
    val last = s"""
      <div class="col-sm-3 hidden-xs img">
        <img src="${ images.last.imageURL(album.url, "t") }" class="frame" href="/img/loader.gif"/>
      </div>"""
    first + last
  }
}
