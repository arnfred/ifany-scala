package ifany

import net.liftweb.json._


sealed abstract class DataResponse
case class DataList(data : List[String]) extends DataResponse
case class DataItem(data : String) extends DataResponse


trait DataLoader {

  val collection : String

  def putItem[A <: SmugmugData : Manifest](id : String, data : A) : Unit = {
    Cache.putItem[A](collection, id, data)
  }

  def getList[A <: SmugmugData : Manifest](ids : List[String]) : List[A] = {
    Cache.getList[A](collection, ids)
  }

  def getQuery[A <: SmugmugData : Manifest](query : Map[String, String] = Map.empty, 
                                            limit : Option[Int] = None) : List[A] = {
    Cache.getQuery[A](collection, query, limit)
  }

  def getItem[A <: SmugmugData : Manifest](id : String = "none") : A = {

    // TODO: wrap the whole thing in a future
    Cache.getItem[A](collection, id) match {
      case Some(response)  => response
      case None            => throw new InternalError("Error getting item " + id + " from " + collection)//parseJSON(SmugmugAPI.getList(dataType, ids, params))
    }
  }


  // Parse a response in JSON to the object
  def parseJSON(json : JValue) : SmugmugData
}
