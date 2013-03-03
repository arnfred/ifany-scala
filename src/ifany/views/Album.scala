package ifany

case class AlbumView(album : Album, gallery : Gallery) extends View {

  val name = "album"
  val index = getCurrentIndex
  def getTitle : String = album.title
  def getDescription : String = album.description

  def getNextAlbum : Option[Album] = index match {
    case 0      => None
    case n      => Some(gallery.albums(n-1))
  }

  def getPrevAlbum : Option[Album] = {
    if (index > gallery.albums.size - 2) None
    else Some(gallery.albums(index+1))
  }

  def getAlbumKey : String = album.key
  def getAlbumId : String = album.id

  private def getCurrentIndex : Int = { 
    gallery.albums.zipWithIndex.filter { case (a,index) => a.id == album.id } .head._2
  }

  def getDateString : String = getDate(album.images, true)

  def getThumbnailRows : List[List[Image]] = {
    val rows = album.images.foldLeft(List(List().asInstanceOf[List[Image]])) { case (a,b) => 
      if (a.head.size < 4) (b :: a.head) :: a.tail else List(b) :: a 
    }

    rows.map(_.reverse).reverse
  }
}
