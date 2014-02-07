package ifany

import scala.util.Random.nextInt
import scala.util.Random.shuffle
import scala.math.abs
import org.joda.time.DateTime

case class FrontpageView(frontpage : Frontpage) extends View {

  val name = "frontpage"
  def getTitle : String = "Photos by Jonas Arnfred"

  // Choose a random cover
  val cover : Cover = shuffle(frontpage.covers).head

  // Return all galleries
  def getGalleries : List[Gallery] = frontpage.galleries


  // The amount of images in an album
  def getAlbumSize(album : Album) : Int = album.images.size


  // The amount of images in a gallery
  def getGallerySize(gallery : Gallery) : Int = {
    gallery.albums.map { a => a.images.size }.sum
  }


  // Find a cover image for the gallery
  def getGalleryCover(gallery : Gallery) : Cover = {
    val covers = for (a <- gallery.albums; i <- a.images if i.cover) yield {
      Cover(i, a.title, a.url)
    }
    if (covers.size > 0) shuffle(covers).head

    // If we have no covers, just use any image in landscape format (width > height)
    else {
      val landscapes = {
        for (a <- gallery.albums; i <- a.images if i.size(0) > i.size( 1 )) yield {
          Cover(i, a.title, a.url)
        }
      }
      shuffle(landscapes).head
    }
  }


  // Find n pictures from an album to display
  def getAlbumImages(album : Album, n : Int) : Iterable[Image] = { 
    shuffle(album.images).take(n)
  }

  def getGalleryDateString(gallery : Gallery) : String = {
    getDateString(for (a <- gallery.albums; i <- a.images) yield i, false)
  }


  def getAlbumDateString(album : Album) : String = {
    getDateString(album.images, true)
  }

}
