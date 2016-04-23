package ifany

sealed trait Row
abstract class TwoImageRow extends Row {
  val left: Image
  val right: Image
  val (leftRatio: Double, rightRatio: Double) = {
    val (w1, h1) = (left.size(0), left.size(1))
    val (w2, h2) = (right.size(0), right.size(1))
    val s2 = (h1*w1 + h1*w2) / (h1*w2 + h2*w1).toDouble
    val s1 = s2*h2/h1.toDouble
    val r1 = s1*w1 / (w1+w2).toDouble
    val r2 = s2*w2 / (w1+w2).toDouble
    (r1, r2)
  }
}
case class CoverRow(image: Image) extends Row
case class PortraitPortraitRow(left: Image, right: Image) extends TwoImageRow
case class PortraitLandscapeRow(left: Image, right: Image) extends TwoImageRow
case class LandscapePortraitRow(left: Image, right: Image) extends TwoImageRow
case class LandscapeLandscapeRow(left: Image, right: Image) extends TwoImageRow

case class AlbumView(album : Album, nav : Navigation) extends View {

  val name = "album"
  def getTitle : String = album.title
  def getDescription : String = album.description
  def getURL = album.url

  def getNav : Navigation = nav

  def getDateString : String = getDateString(album.images, true)

  def getGalleries: String = {
    val galleries = album.galleries.filter(_!="all")
    val links = galleries map { g => s"""<a href="/${Gallery.url(g)}" alt="Go to Gallery: $g">$g</a>""" }
    links.mkString(", ")
  }

  def getJson : String = album.json

  def getRows(images: List[Image], rows: List[Row] = List.empty): List[Row] = images.map(_.orientation) match {
    case Nil => rows.reverse
    case _ :: Nil => (CoverRow(images(0)) :: rows).reverse
    case Landscape :: _ if images(0).cover => getRows(images.drop(1), CoverRow(images(0)) :: rows)
    case Portrait :: Landscape :: _ if images(1).cover => getRows(images(0) :: images.drop(2), CoverRow(images(1)) :: rows)
    case Portrait :: Portrait :: _ => getRows(images.drop(2), PortraitPortraitRow(images(0), images(1)) :: rows)
    case Portrait :: Landscape :: _ => getRows(images.drop(2), PortraitLandscapeRow(images(0), images(1)) :: rows)
    case Landscape :: Portrait :: _ => getRows(images.drop(2), LandscapePortraitRow(images(0), images(1)) :: rows)
    case Landscape :: Landscape :: _ => getRows(images.drop(2), LandscapeLandscapeRow(images(0), images(1)) :: rows)
  }
}
