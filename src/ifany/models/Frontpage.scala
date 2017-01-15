package ifany

import dispatch._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.shuffle
import java.io.File
import net.liftweb.json._
import scala.io.Source
import java.io.FileNotFoundException

case class Frontpage(galleries : List[Gallery],
                     covers : List[Cover])

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

    // Get all albums
    val albums = for (a <- Album.getAll if a.isPublic) yield a

    // Get galleries
    val galleries = getGalleries(albums, "resources" + Ifany.photoDir + jsonPath)

    // Get covers
    val covers = for (a <- albums; i <- a.images if i.banner) yield {
      Cover(i, a)
    }

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


  // Fetches galleries and fills in the relevant albums (in linear time)
  private def getGalleries(albums : List[Album], 
                           jsonPath : String) : List[Gallery] = {

    // Group albums by gallery
    val grouped : Map[String, List[Album]] = { 
      for (a <- albums; g <- a.galleries) yield (g -> a)
    } groupBy { // Returns type of List[String, Array[(String, Album)]]
      case (k,v) => k
    } map { // Simplify touple to Album
      case (k,v) => (k, v.map(_._2).toList)
    } map { // Sort all lists of albums by date
      case (k,v) => (k, v.sortBy(_.datetime._2.getMillis))
    }

    // Read galleries from json
    val jsonGalleries : Map[String, Gallery] = { 
      for (g <- galleryList(jsonPath)) yield g.name -> g
    } toMap 

    // Create a list of galleries
    val galleries = (for ((k, as) <- grouped; g <- jsonGalleries.get(k)) yield {
      Gallery(g.name, g.description, as)
    }) toList

    // Sort the list and return
    galleries.sortBy(g => g.albums.map(_.datetime._2).last.getMillis).reverse

  }


  // Fetches galleries from json
  def galleryList(jsonPath : String) : List[Gallery] = {

    // Try to load galleries.json
    val json : String       = try {
      Source.fromFile(jsonPath).getLines.mkString("\n")
    } catch {
      case e : FileNotFoundException => throw new InternalError(jsonPath + " not found")
    }

    Serialization.read[List[Gallery]](json)
  }

}
