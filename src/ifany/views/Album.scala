package ifany

sealed trait Row
case class CoverRow(image: Image) extends Row
case class DualRow(left: Image, right: Image) extends Row {
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

  def getRows(images: List[Image], rows: List[Row] = List.empty): List[Row] = images match {
    case Nil => rows.reverse
    case image :: Nil => (CoverRow(image) :: rows).reverse
    case image :: rest if (image.cover) => getRows(rest, CoverRow(image) :: rows)
    case image1 :: image2 :: rest if (image2.cover) => getRows(image1 :: rest, CoverRow(image2) :: rows)
    case image1 :: image2 :: rest => getRows(rest, DualRow(image1, image2) :: rows)
  }
}
