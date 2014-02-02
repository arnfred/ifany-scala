package ifany

import org.joda.time.DateTime 
import scala.io.Source
import java.io.FileNotFoundException
import net.liftweb.json._

case class AlbumModel(title : String,
                      description : String,
                      url : String,
                      galleries : List[String],
                      images : List[AlbumImage])

case class AlbumImage(file : String,
                 description : String,
                 datetime : DateTime,
                 iso : String,
                 focallength : String,
                 f_stop : String,
                 size : List[Int],
                 model : String,
                 exposure : String)


object AlbumModel {

  implicit val formats = DefaultFormats

  def get(url : String) : AlbumModel = {

    // Try to read file and if it fails, throw and album not found exception
    try {
        val album_conf = "resources/photos/" + url + "/album.json"
        val json = Source.fromFile(album_conf).getLines.mkString("\n")
        Serialization.read[AlbumModel](json)
    } catch {
      case e : FileNotFoundException => throw new AlbumNotFound(url)
    }
  }

}
