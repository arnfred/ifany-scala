package ifany

import net.liftweb.json._


sealed abstract class DataResponse
case class DataList(data : Iterator[String]) extends DataResponse
case class DataItem(data : String) extends DataResponse


trait DataLoader {

  val collection : String
  val method : String

  def getList[A <: SmugmugData : Manifest](ids : Iterator[String]) : Iterator[A] = {
    Cache.getList[A](collection, ids)
  }


  def getItem[A <: SmugmugData : Manifest](id : String = "none") : A = {

    // TODO: wrap the whole thing in a future
    Cache.getItem[A](collection, id) match {
      case Some(response)  => response
      case None            => throw new Exception("error")//parseJSON(SmugmugAPI.getList(dataType, ids, params))
    }
  }


  // Take a dataresponse and either break up the json or keep it as is
  def treatResponse(data : DataResponse) : Iterator[JValue]
  

  // Parse a response in JSON to the object
  def parseJSON(json : JValue) : SmugmugData
}
