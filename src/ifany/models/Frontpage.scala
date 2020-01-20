package ifany

import dispatch._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.shuffle
import java.io.File
import net.liftweb.json._
import scala.io.Source
import java.io.FileNotFoundException

case class Frontpage(galleries : Seq[Gallery],
                     covers : Seq[Cover])

case class Cover(image : Image, album : Album) {
  def makeImage: Image = image.copy(
    description = image.description + " (from " + album.title + ")",
    file = album.url + "/" + image.file,
    banner = false,
    cover = false)
}



object Frontpage {

  implicit val formats              = DefaultFormats

  // We cache the data since it takes a bit of computation to get
  var data : Option[Frontpage]  = None

  // Update data
  def update(jsonPath : String = "galleries.json", nbCovers : Int = 20) : Frontpage = {

    // Get galleries
    val galleries = Gallery.updateGalleries

    // Map all albums
    val albums: Seq[Album] = galleries.flatMap(_.albums)

    // Get covers
    val covers = for (a <- albums; i <- a.images if i.banner) yield {
      Cover(i, a)
    }
    println(covers)

    // Get frontpage
    val frontpage = Frontpage(galleries, covers)

    data = Some(frontpage)
    frontpage
  }


  // return frontpagefrom cache if available
  def get(jsonPath : String = "galleries.json") : Frontpage = data match {
    case None       => update(jsonPath)
    case Some(d)    => d
  }

}
