package ifany

case class GalleryView(gallery : Gallery, nav : Navigation) extends View {

  // Used to fetch the right css and javascript
  val name = "frontpage"

  def getTitle : String = gallery.name

  def getDescription = gallery.description

  def getNav : Navigation = nav

  // Find a cover image for the gallery
  val cover : Cover = gallery.cover

  // The amount of images in a gallery
  def getSize : Int = {
    gallery.albums.map { a => a.size }.sum
  }

  def getDateString : String = {
    getDateString(for (a <- gallery.albums; i <- a.images) yield i, false)
  }


}
