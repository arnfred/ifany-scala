package ifany

import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import dispatch._
import java.io.PrintWriter
import java.io.File

trait Smugmug {

  val APIKey = "CXsQWfZPNH6Tw6k0MpbTdHJAHcs9MJfG"
  val endpoint = "http://api.smugmug.com/services/api/json/1.3.0/"
  val NickName = "Arnfred"
  val defaultId : String = "0"

  val handshake = fetchHandshake

  /**
   * Fetches from cache or remote and throws an exception in case of connection
   * problems
   */
  def get(method : String, params : Map[String, String] = Map.empty, id : String = defaultId) : Promise[String] = {

    // Get initial promise
    val p = getOption(method, params, id)

    // Throw an error if the request fail, else parse it
    p.map {
      case Some(text)   => text
      case None         => throw new Exception("The call to " + method + " was not successful")
    }
  }

  /**
   * Tries to fetch query from db, but if it isn't possible we load from remote
   * Returns an option as response with None if the request couldn't be fulfilled
   */
  def getOption(method : String, params : Map[String,String] = Map.empty, id : String = defaultId) : Promise[Option[String]]= {

    // Test if we have catch hit
    Cache.load(id, method) match {
      case Some(result) => Http.promise(Some(result))
      case None         => getRemote(method, params, id)
    }
  }

  /**
   * Fetches a query from the smugmug api
   */
  def getRemote(method : String, params : Map[String,String] = Map.empty, id : String = defaultId) : Promise[Option[String]]= {

    println("fetching " + method)

    // Create a request which isn't evaluated yet
    val m = "smugmug." + method
    val p = getParams(m) ++ params
    lazy val request = Http((url(endpoint) <<? p) OK as.String).option

    // Make sure we're saving the answer to cache
    for (_ <- handshake; r <- request) yield { r.map(Cache.save(id, method, _)); r }
  }


  private def getParams(method : String) : Map[String, String] = {
    Map(("method", method), ("NickName", NickName), ("APIKey", APIKey))
  }

  // Define that we are browsing my smugmug account
  private def fetchHandshake = {
    val method = "smugmug.accounts.browse"
    Http((url(endpoint) <<? getParams(method)) OK as.String).option
  }
}
