package ifany

import scala.util.Random.nextInt
import scala.util.Random.shuffle
import scala.math.abs
import org.joda.time.DateTime

case class FrontpageView(data : FrontpageModel) extends View {

  val name = "frontpage"
  def getTitle : String = "Photos by Jonas Arnfred"


  def getBanner : Image = getRandImage(data.banners)


  // TODO: return in the right order
  def getCategories : List[(Category, List[Album])] = {

    def getDate(album : Album) : DateTime = data.exifMap(album.id).dateTime

    val cats = data.categories.zip(data.categories.map(getCatAlbums(_)))
    cats.filter { case(c,as) => as.size > 0 } sortBy { case (_,as) =>
      as.map { getDate(_).getMillis }.max
    }
  }



  def getAlbumSize(album : Album) : Int = data.imageMap(album.id).size


  def getCatNumImages(albums : List[Album]) : Int = {
    albums.map { a => data.imageMap(a.id).size }.sum
  }


  // TODO: get album cover
  def getRandCover(albums : List[Album]) : Image = {
    getAlbumCoverImage(getRandAlbum(data.albums), data.imageMap)
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
