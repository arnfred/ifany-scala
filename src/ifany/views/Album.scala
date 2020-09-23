package ifany

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{write}

sealed trait Row
case class CoverRow(image: Image) extends Row
case class DualRow(left: Image, right: Image) extends Row {
  val (leftRatio: Double, rightRatio: Double) = {
    val (x1, y1) = (left.size(0).toDouble, left.size(1).toDouble)
    val (x2, y2) = (right.size(0).toDouble, right.size(1).toDouble)
    val r2 = 1.0 // Can be any number. It's just a ratio
    val r1 = y2/y1 // Relationship between heights
    val p1 = r1*x1 / (x1 * r1 + x2 * r2) // Percentage of image 1 width to entire width
    val p2 = r2*x2 / (x1 * r1 + x2 * r2) // Percentage of image 2 width to entire width
    (p1, p2)
  }
}

case class AlbumView(album : Album, nav : Navigation, name : String = "album", cssname: String = "album") extends View {

  implicit val formats = Serialization.formats(NoTypeHints)

  val urls: Map[String, String] = S3Photo.generatePresignedUrls(album.url)

  def videoURL(image: Image) = {
    val key = s"albums/${album.url}/${image.key("")}"
    urls(key)
  }

  def imageURL(image: Image, sizeLabel: String) = {
    val key = s"albums/${album.url}/${image.key(sizeLabel)}"
    urls.get(key) match {
      case Some(url) => url
      case None => image.url(sizeLabel, album.url)
    }
  }

  def getTitle : String = album.title
  def getDescription : String = album.description
  def getURL = album.url

  def getNav : Navigation = nav

  def getDateString : String = getDateString(album.images, true)

  def getGalleries: String = {
    val galleries = album.galleries.filter(_!="all")
    val links = galleries map { g => s"""<a href="/${Gallery.get(g).url}" alt="Go to Gallery: $g">$g</a>""" }
    links.mkString(", ")
  }

  def getJson : String = write(album)

  def getRows(images: Seq[Image], rows: Seq[Row] = Seq.empty): Seq[Row] = images match {
    case Nil => rows.reverse
    case image +: Nil => (CoverRow(image) +: rows).reverse
    case image +: rest if (image.cover) => getRows(rest, CoverRow(image) +: rows)
    case image1 +: image2 +: Nil => (DualRow(image1, image2) +: rows).reverse
    case image1 +: image2 +: rest if (image2.cover) => getRows(image1 +: rest, CoverRow(image2) +: rows)
    case image1 +: image2 +: rest => getRows(rest, DualRow(image1, image2) +: rows)
  }
}
