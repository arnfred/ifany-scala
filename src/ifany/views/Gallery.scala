package ifany

import scala.util.Random.nextInt
import scala.util.Random.shuffle
import scala.math.abs

case class GalleryView(gallery : Gallery, nav : Navigation) extends View {

  // Used to fetch the right css and javascript
  val name = "frontpage"

  def getTitle : String = gallery.name

  def getDescription = gallery.description

  def getNav : Navigation = nav

  // Find a cover image for the gallery
  val cover : Cover = gallery.cover

  // The amount of images in an album
  def getAlbumSize(album : Album) : Int = album.size


  // The amount of images in a gallery
  def getSize : Int = {
    gallery.albums.map { a => a.size }.sum
  }

  // Find n pictures from an album to display
  def getAlbumImages(album : Album, n : Int) : Iterable[Image] = { 
    shuffle(album.images).take(n)
  }

  def getDateString : String = {
    getDateString(for (a <- gallery.albums; i <- a.images) yield i, false)
  }


  def getAlbumDateString(album : Album) : String = {
    val images = for (i <- album.images) yield i
    getDateString(images, true)
  }

}
