package ifany

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import dispatch._
import java.io.PrintWriter
import java.io.File

object SmugmugAPI {

  val APIKey = "CXsQWfZPNH6Tw6k0MpbTdHJAHcs9MJfG"
  val endpoint = "http://api.smugmug.com/services/api/json/1.3.0/"
  val NickName = "Arnfred"
  val defaultId : String = "0"

  val handshake = fetchHandshake

  /**
   * Fetches a query from the smugmug api
   */
  def get(method : String, params : Map[String,String] = Map.empty) : Promise[String]= {

    println("fetching " + method)

    // Create a request which isn't evaluated yet
    val m = "smugmug." + method
    val p = getParams(m) ++ params
    lazy val request = Http((url(endpoint) <<? p) OK as.String)

    // Make sure we're saving the answer to cache
    for (_ <- handshake; r <- request) yield r
  }


  def getImages(albumID : String, albumKey : String) : Promise[List[Image]] = {
    val imagesPromise = SmugmugAPI.get("images.get", Map("AlbumID" -> albumID, 
                                                         "AlbumKey" -> albumKey,
                                                         "Heavy" -> "true"))

    imagesPromise map { data =>
      for (json <- (parse(data) \ "Album" \ "Images").children) yield {
        Image.parseJSON(json)
      }
    }
  }

  def getAlbums : Promise[List[Album]] = {
    val albumsPromise = SmugmugAPI.get("albums.get", Map("Heavy" -> "true"))

    albumsPromise map { data =>
      for (json <- (parse(data) \ "Albums").children) yield {
        Album.parseJSON(json)
      }
    }
  }

  def getEXIF(imageID : String, imageKey : String) : Promise[EXIF] = {
    val exifPromise = SmugmugAPI.get("images.getEXIF", Map("ImageID" -> imageID, 
                                                           "ImageKey" -> imageKey))

    exifPromise map { data => EXIF.parseJSON(parse(data) \ "Image") }
  }


  def getCategories : Promise[List[Category]] = {
    val categoriesPromise = SmugmugAPI.get("categories.get")

    categoriesPromise map { data =>
      for (json <- (parse(data) \ "Categories").children) yield {
        Category.parseJSON(json)
      }
    }
  }


  private def getParams(method : String) : Map[String, String] = {
    Map(("method", method), ("NickName", NickName), ("APIKey", APIKey))
  }

  // Define that we are browsing my smugmug account
  private def fetchHandshake = {
    println("fetching handshake")
    val method = "smugmug.accounts.browse"
    Http((url(endpoint) <<? getParams(method)) OK as.String).option
  }
}
