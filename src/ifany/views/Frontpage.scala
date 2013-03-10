package ifany

import scala.util.Random.nextInt
import scala.util.Random.shuffle
import scala.math.abs
import org.joda.time.DateTime

case class FrontpageView(data : FrontpageModel) extends View {

  val name = "frontpage"
  def getTitle : String = "Photos by Jonas Arnfred"


  val banner : Image = {
    (for ((albumID,imgs) <- data.imageMap; i <- imgs if i.id == data.bannerID) yield i) match {
      case Nil   => throw new Exception("Banner image with id: " + data.bannerID + " not found among images")
      case list  => list.head
    }
  }


  // TODO: return in the right order
  def getCategories : List[(Category, List[Album])] = {

    def getDate(album : Album) : DateTime = data.exifMap(album.id).dateTime

    val cats = data.categories.zip(data.categories.map(getCatAlbums(_)))
    cats.filter { case(c,as) => as.size > 0 } sortBy { case (_,as) =>
      // Get all the dates of the album
      as.map { getDate(_).getMillis }.max
    } reverse
  }



  def getAlbumSize(album : Album) : Int = album.imageIDs.size


  def getCatNumImages(albums : List[Album]) : Int = {
    albums.map { a => a.imageIDs.size }.sum
  }


  // TODO: get album cover
  def getRandCover(albums : List[Album]) : Image = {
    getAlbumCoverImage(getRandAlbum(albums), data.imageMap)
  }


  // TODO: convert to ajax call
  def getAlbumImages(album : Album, n : Int) : Iterable[Image] = { 
    shuffle(data.imageMap(album.id)).take(n)
  }

  def getCatDate(albums : List[Album]) : String = {
    getDate(albums.map(getAlbumCoverEXIF(_, data.exifMap)), false)
  }


  def getAlbumDate(album : Album) : String = {
    getDate(List(getAlbumCoverEXIF(album, data.exifMap)), true)
  }

  // Expensive
  private def getCatAlbums(cat : Category) : List[Album] = {
    for (a <- data.albums if a.categoryID == Some(cat.id)) yield a
  }



  private def getRandAlbum(albums : List[Album]) : Album = albums.size match {
    case 0 => throw new Exception("No album supplied to getRandomAlbum")
    case n => albums(abs(nextInt) % n)
  }


  private def getRandImage(images : List[Image]) : Image = images.size match {
    case 0 => throw new Exception("No images supplied to getRandomImages")
    case n => images(abs(nextInt) % n)
  }
}
