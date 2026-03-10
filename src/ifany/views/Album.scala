package ifany

import java.time.format.DateTimeFormatter
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{compact, render}

sealed trait Row
case class CoverRow(image: Image) extends Row
case class DualRow(left: Image, right: Image) extends Row


case class AlbumView(album : Album, nav : Navigation, name : String = "album") extends View {

  def getTitle : String = album.title
  def getDescription : String = album.description
  def getURL = album.url

  def getNav : Navigation = nav

  def getDateString : String = getDateString(album.images, true)

  def getGalleries: String = {
    val galleries = album.galleries.filter(_ != "all")
    val links = galleries map { g => s"""<a href="/${Gallery.get(g).url}" class="text-site-link hover:text-site-link-hover" alt="Go to Gallery: $g">${Gallery.get(g).name}</a>""" }
    links.mkString(", ")
  }

  def getJson : String = {
    val imagesJson = album.images.map { i =>
      ("file" -> i.file) ~
      ("description" -> i.description) ~
      ("datetime" -> i.datetime) ~
      ("banner" -> i.banner) ~
      ("cover" -> i.cover) ~
      ("size" -> i.size) ~
      ("published" -> i.published) ~
      ("is_video" -> i.is_video)
    }
    val json =
      ("title" -> album.title) ~
      ("description" -> album.description) ~
      ("url" -> album.url) ~
      ("galleries" -> album.galleries) ~
      ("visible" -> album.visible) ~
      ("images" -> imagesJson) ~
      ("datetime" -> List(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(album.datetime._1),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(album.datetime._2)
      ))
    compact(render(json))
  }

  def getRows(images: Seq[Image], rows: Seq[Row] = Seq.empty): Seq[Row] = images match {
    case Nil => rows.reverse
    case image +: Nil => (CoverRow(image) +: rows).reverse
    case image +: rest if (image.cover) => getRows(rest, CoverRow(image) +: rows)
    case image1 +: image2 +: Nil => (DualRow(image1, image2) +: rows).reverse
    case image1 +: image2 +: rest if (image2.cover) => getRows(image1 +: rest, CoverRow(image2) +: rows)
    case image1 +: image2 +: rest => getRows(rest, DualRow(image1, image2) +: rows)
  }
}
