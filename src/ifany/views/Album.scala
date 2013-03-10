package ifany

case class AlbumView(data : AlbumModel) extends View {

  val name = "album"
  def getTitle : String = data.album.title
  def getDescription : String = data.album.description

  def getNextAlbum : Option[String] = data.album.nav.next
  def getPrevAlbum : Option[String] = data.album.nav.prev

  def getAlbumKey : String = data.album.key
  def getAlbumId : String = data.album.id

  def getDateString : String = getDate(data.exifs, true)

  def getThumbnailRows : List[List[Image]] = {
    val rows = data.images.foldLeft(List(List().asInstanceOf[List[Image]])) { case (a,b) => 
      if (a.head.size < 4) (b :: a.head) :: a.tail else List(b) :: a 
    }

    rows.map(_.reverse).reverse
  }
}
