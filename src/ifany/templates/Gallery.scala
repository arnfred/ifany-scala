package ifany

case class GalleryTemplate(view : GalleryView) extends Template {

  // Implicit conversion 
  import com.dongxiguo.fastring.Fastring.Implicits._
  implicit val v = view

  override def toString : String = Base(
    Template(navigation(view.getNav) + header + gallery),
    Some(nextprev)
  )

  def nextprev : Template = {
    val next = for (n <- view.getNav.next) yield n.url
    val prev = for (p <- view.getNav.prev) yield p.url
    Template(fast"""
      <link rel="next" href="${ next.getOrElse("/") }"/>
      <link rel="prev" href="${ prev.getOrElse("/") }"/>
    """)
  }

  def navigation(nav : Navigation) : Template = {
    val prevPhone = for (p <- nav.prev) yield getLink("Older", "/" + p.url + "/", "&laquo;")
    val nextPhone = for (n <- nav.next) yield getLink("Newer", "/" + n.url + "/", "&raquo;")
    val prev = for (p <- nav.prev) yield getLink(p.title, "/" + p.url + "/", "&laquo;")
    val next = for (n <- nav.next) yield getLink(n.title, "/" + n.url + "/", "&raquo;")
    val t = Template(fast"""

      <div class="row-fluid visible-phone navigation">
          <div class="album-nav prev span4 offset1">
            ${ prevPhone.getOrElse("")  }
          </div>
          <div class="home album-nav span2">
            <a href="/"><span class="nav home">Home</span></a>
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
            <a href="/"><span class="nav home">Home</span></a>
          </div>
          <div class="album-nav next span4">
            ${ next.getOrElse("") }
          </div>
      </div>
    """)
    if (prev != None || next != None) t
    else Template("")
  }

  def getHomeLink(text : String, url : String) : Template = Template(fast"""
    <a href="/$url/"><span class="nav home">$text</span></a>
  """)

  def getLink(text : String, url : String, sign : String) : Template = Template(fast"""
    <a href="$url" alt="$text">
      <span class="laquo">$sign</span>
      <span class="nav other">$text</span>
    </a>
  """)

  def header : Template = Template {
    val albumNum : Int = view.gallery.albums.size
    val imagesNum : Int = view.getSize
    fast"""
    <div class="row-fluid top topmost">
        <div class="span3 offset1" id="about">
          <h1 id="gallery-name"><span>${ view.getTitle }</span></h1>

            <p class="cat-date">${ view.getDateString }.</p>
            <p class="cat-meta">This gallery contains 
              <span class="num">${ albumNum }</span> 
              ${ if (albumNum == 1) "album" else "albums" } with 
              <span class="num">${ imagesNum }</span> images.
            </p>
            <p id="gallery-desc">${ view.getDescription }</p>

        </div>
        <div class="span7" id="image">
            <img style="background-image:url('${ view.cover.image.url("l", view.cover.albumURL) }')" id="gallery-cover"/>
            <p>From the album "<a href="${ view.cover.albumURL }/" >${ view.cover.albumTitle }</a>"</p>
        </div>

    </div>
    """
  }


  def gallery : Template = Template {
    (for (album <- view.gallery.albums) yield fast"""
      <div class="row-fluid album">
        <a href="/${ view.gallery.url }/${ album.url }/">
          <div class="span3 offset1 album-info">
            <h3 class="album-title">${ album.title }</h3>
            <p class="album-date">${ view.getAlbumDateString(album) }</p>
            <p class="album-meta">
              <span class="num">${ view.getAlbumSize(album) }</span> 
              ${ if (view.getAlbumSize(album) == 1) "Image" else "Images" } 
            </p>
          </div>

          <div class="span7 album-images">
            <div class="row-fluid">
              ${ albumThumbnails(album) }
            </div>
          </div>
          <div class="span1 album-arrow hidden-phone">
            <p>&raquo;</p>
          </div>
        </a>
      </div>
    """).mkString
  }

  def albumThumbnails(album : Album) : Template = Template {
    (for (image <- view.getAlbumImages(album, 4)) yield fast"""
      <div class="span3 img">
        <img src="${ image.url("t", album.url) }" class="frame"/>
      </div>
    """).mkString
  }
}
