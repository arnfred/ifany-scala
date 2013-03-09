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
  def update(method : String, collection : String, id : String = defaultId, params : Map[String,String] = Map.empty) : Promise[Option[String]]= {

    println("fetching " + method)

    // Create a request which isn't evaluated yet
    val m = "smugmug." + method
    val p = getParams(m) ++ params
    lazy val request = Http((url(endpoint) <<? p) OK as.String).option

    // Make sure we're saving the answer to cache
    for (_ <- handshake; r <- request) yield r
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
