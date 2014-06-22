package ifany

import org.joda.time.DateTime 
import scala.io.Source
import java.io.FileNotFoundException
import net.liftweb.json._
import java.io.File

case class Image(file : String,
                 description : String,
                 datetime : Option[String],
                 banner : Boolean,
                 cover : Boolean,
                 size : List[Int]) {

  def url(size : String, albumURL : String) : String = {
    val sizes : Map[String, String] = (Map.empty +
      ("t" -> "150x150") + ("s" -> "400x300") + 
      ("m" -> "600x450") + ("l" -> "800x600") +
      ("150" -> "150x150") + ("400" -> "400x300") +
      ("600" -> "600x450") + ("800" -> "800x600") +
      ("1024" -> "1024x768") + ("1280" -> "1280x980") +
      ("1600" -> "1600x1200") + ("2000" -> "2000x1500"))
    try {
      Ifany.photoDir + albumURL + "/" + file + "_" + sizes(size) + ".jpg"
    } catch {
        case _ : Exception => throw InternalError("Image with size '" + size + "' doesn't exist")
    }
  }
}

case class Album(title : String,
                 description : String,
                 url : String,
                 galleries : List[String],
                 public : Option[Boolean],
                 images : List[Image]) {

  implicit val formats    = DefaultFormats
  def json : String = Serialization.write(this)

  val datetime : (DateTime, DateTime) = {

    // Take all images that have a date associated and sort them
    val sorted_dates : List[DateTime] = {
      for (i <- images; dt <- i.datetime) yield new DateTime(i.datetime.get)
    } sortBy(_.getMillis)

    // Return the first and last date
    sorted_dates.size match {
      case 0 => throw InternalError("Album '" + url + "' has no date information associated with any pictures")
      case 1 => (sorted_dates.head, sorted_dates.head)
      case n => (sorted_dates.head, sorted_dates.last)
    }
  }

  val isPublic : Boolean = public match {
    case None => true
    case Some(b) => b
  }

  val getGallery : Option[String] = galleries match {
    case Nil => None
    case "all" :: Nil => None
    case g :: Nil => Some(g)
    case "all" :: g :: rest => Some(g)
    case otherwise => None
  }

  val path : String = getGallery match {
    case None => "album/" + url
    case Some(gURL) => Gallery.url(gURL) + "/" + url
  }
}


object Album {

  implicit val formats    = DefaultFormats

  // Based on an url we return an album
  def get(url : String, jsonPath : String = "album.json") : Album = {

    // Location of album data file
    val json_path : String = "resources" + Ifany.photoDir + url + "/" + jsonPath

    // Load JSON
    val json : String       = try {
      Source.fromFile(json_path).getLines.mkString("\n")
    } catch {
      case e : FileNotFoundException => throw new AlbumNotFound(url)
    }

    // Parse JSON as Album and return result
    val album = try {
      Serialization.read[Album](json)
    } catch {
      case e : Throwable => {
        throw new InternalError("Couldn't read json from '" + url + "':\n" + json)
      }
    }

    return album
  }

  // Returns all albums in the photo dir
  def getAll : List[Album] = {
    val root = new File("resources" + Ifany.photoDir)
    for (f <- (root).listFiles if f.isDirectory) yield {
      get(f.getName)
    }
  } toList
}
