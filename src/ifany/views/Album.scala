package ifany

case class AlbumView(model : AlbumModel) extends View {

  val name = "album"
  val directory = "/photos/" + model.data.url + "/"
  def getTitle : String = model.data.title
  def getDescription : String = model.data.description

  def getNextAlbum : Option[NavElem] = None //data.album.nav.next
  def getPrevAlbum : Option[NavElem] = None //data.album.nav.prev

  def getDateString : String = "" //getDateString(data.exifs, true)

  def getJson : String = model.json

  def getImgUrl(img : ImageData, size : String) : String = size match {
    case "original"     => directory + img.file + ".jpg"
    case "thumbnail"    => directory + img.file + "_150x150.jpg"
    case s              => directory + img.file + "_" + s + ".jpg"
  }

  def getThumbnailRows : List[List[ImageData]] = {
    val rows = model.data.images.foldLeft(List(List().asInstanceOf[List[ImageData]])) { case (a,b) => 
      if (a.head.size < 4) (b :: a.head) :: a.tail else List(b) :: a
    }

    rows.map(_.reverse).reverse
  }
}
