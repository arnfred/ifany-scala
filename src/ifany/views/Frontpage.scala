package ifany

import scala.util.Random.nextInt
import scala.util.Random.shuffle
import scala.math.abs

case class FrontpageView(frontpage : Frontpage) extends View {

  val name = "frontpage"
  def getTitle : String = "Photos by Jonas Arnfred"

  // Choose a random cover
  val cover : Cover = shuffle(frontpage.covers).head

  // Return all galleries
  def getGalleries : Seq[Gallery] = frontpage.galleries


  // The amount of images in an album
  def getAlbumSize(album : Album) : Int = album.size


  // The amount of images in a gallery
  def getGallerySize(gallery : Gallery) : Int = {
    gallery.albums.map { a => a.size }.sum
  }


  // Find a cover image for the gallery
  def getGalleryCover(gallery : Gallery) : Cover = {
    val covers = for (a <- gallery.albums; i <- a.images if i.cover) yield {
      Cover(i, a)
    }
    if (covers.size > 0) shuffle(covers).head

    // If we have no covers, just use any image in landscape format (width > height)
    else {
      val landscapes = {
        for (a <- gallery.albums; i <- a.images if !i.isVertical && i.published && !i.is_video) yield {
          Cover(i, a)
        }
      }
      shuffle(landscapes).head
    }
  }


  // Find n pictures from an album to display
  def getAlbumImages(album : Album, n : Int) : Iterable[Image] = { 
    val candidates = for (i <- album.images if i.published) yield i
    shuffle(candidates).take(n)
  }

  def getGalleryDateString(gallery : Gallery) : String = {
    getDateString(for (a <- gallery.albums; i <- a.images if i.published) yield i, false)
  }

  def getAlbumDateString(album : Album) : String = {
    val images = for (i <- album.images if i.published) yield i
    getDateString(images, true)
  }
}
