package ifany

case class AlbumView(album : Album, nav : Navigation) extends View {

  val name = "album"
  def getTitle : String = album.title
  def getDescription : String = album.description
  def getURL = album.url

  def getNextAlbum : Option[NavElem] = nav.next
  def getPrevAlbum : Option[NavElem] = nav.prev

  def getDateString : String = getDateString(album.images, true)

  def getJson : String = album.json

  def getThumbnailRows : List[List[Image]] = {
    val rows = album.images.foldLeft(List(List().asInstanceOf[List[Image]])) { case (a,b) => 
      if (a.head.size < 4) (b :: a.head) :: a.tail else List(b) :: a
    }

    rows.map(_.reverse).reverse
  }
}
