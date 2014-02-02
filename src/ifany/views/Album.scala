package ifany

case class AlbumView(model : AlbumModel) extends View {

  val name = "album"
  val directory = "/photos/" + model.url + "/"
  def getTitle : String = model.title
  def getDescription : String = model.description

  def getNextAlbum : Option[NavElem] = None //data.album.nav.next
  def getPrevAlbum : Option[NavElem] = None //data.album.nav.prev

  def getDateString : String = "" //getDateString(data.exifs, true)

  def getImgUrl(img : AlbumImage, size : String) : String = size match {
    case "original"     => directory + img.file + ".jpg"
    case "thumbnail"    => directory + img.file + "_150x150.jpg"
    case s              => directory + img.file + "_" + s + ".jpg"
  }

  def getThumbnailRows : List[List[AlbumImage]] = {
    val rows = model.images.foldLeft(List(List().asInstanceOf[List[AlbumImage]])) { case (a,b) => 
      if (a.head.size < 4) (b :: a.head) :: a.tail else List(b) :: a
    }

    rows

    //rows.map(_.reverse).reverse
  }
}
