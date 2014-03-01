package ifany

case class GalleryTemplate(view : GalleryView) extends Template {

  // Implicit conversion 
  import com.dongxiguo.fastring.Fastring.Implicits._
  implicit val v = view

  override def toString : String = Base(Template(header + gallery))

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
        <a href="/${ view.gallery.url.get }/${ album.url }/">
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
