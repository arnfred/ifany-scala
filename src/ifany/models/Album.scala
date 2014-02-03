package ifany

import org.joda.time.DateTime 
import scala.io.Source
import java.io.FileNotFoundException
import net.liftweb.json._

case class AlbumData(title : String,
                     description : String,
                     url : String,
                     galleries : List[String],
                     images : List[ImageData])

case class ImageData(file : String,
                     description : String,
                     datetime : DateTime,
                     iso : String,
                     focallength : String,
                     f_stop : String,
                     size : List[Int],
                     model : String,
                     exposure : String)

case class AlbumModel(url : String) {

    implicit val formats    = DefaultFormats
    val album_conf : String = "resources/photos/" + url + "/album.json"
    val json : String       = try {
      Source.fromFile(album_conf).getLines.mkString("\n")
    } catch {
      case e : FileNotFoundException => throw new AlbumNotFound(url)
    }
    val data : AlbumData    = Serialization.read[AlbumData](json)

}
