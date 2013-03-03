package ifany

import scala.util.Random.nextInt
import scala.util.Random.shuffle
import scala.math.abs
import java.util.Date

case class FrontpageView(frontpageImages : List[Image], gallery : Gallery) extends View {

  val name = "frontpage"
  def getTitle : String = "Photos by Jonas Arnfred"


  def getCoverImg : Image = getRandImage(frontpageImages)


  // TODO: return in the right order
  def getCategories : List[(Category, List[Album])] = {

    def getDate(album : Album) : Date = getAlbumCover(album).exif match {
      case None => new Date("1986/04/17")
      case Some(exif) => exif.dateTime
    }

    val cats = gallery.categories.zip(gallery.categories.map(getCatAlbums(_)))
    cats.filter { case(c,as) => as.size > 0 } sortWith { case ((_,as1),(_,as2)) =>
      val d1 = as1.map(getDate(_)).max
      val d2 = as2.map(getDate(_)).max
      d1.after(d2)
    }
  }





  def getCatNumImages(albums : List[Album]) : Int = albums.map { _.images.size }.sum


  // TODO: get album cover
  def getRandCover(albums : List[Album]) : Image = getAlbumCover(getRandAlbum(albums))


  // TODO: convert to ajax call
  def getAlbumImages(album : Album, n : Int) : Iterable[Image] = shuffle(album.images).take(n)

  def getCatDate(albums : List[Album]) : String = getDate(albums.map(getAlbumCover(_)), false)

  def getAlbumDate(album : Album) : String = getDate(List(getAlbumCover(album)), true)

  // Expensive
  private def getCatAlbums(cat : Category) : List[Album] = {
    for (a <- gallery.albums if a.categoryId == Some(cat.id)) yield a
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
