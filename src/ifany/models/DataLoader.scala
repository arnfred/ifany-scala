package ifany

import net.liftweb.json._


sealed abstract class DataResponse
case class DataList(data : Iterator[String]) extends DataResponse
case class DataItem(data : String) extends DataResponse


trait DataLoader {

  val collection : String

  def putItem[A <: SmugmugData : Manifest](id : String, data : A) : Unit = {
    Cache.putItem[A](collection, id, data)
  }

  def getList[A <: SmugmugData : Manifest](ids : Iterator[String]) : Iterator[A] = {
    Cache.getList[A](collection, ids)
  }

  def getQuery[A <: SmugmugData : Manifest](query : Map[String, String] = Map.empty, 
                                            limit : Option[Int] = None) : Iterator[A] = {
    Cache.getQuery[A](collection, query, limit)
  }

  def getItem[A <: SmugmugData : Manifest](id : String = "none") : A = {

    // TODO: wrap the whole thing in a future
    Cache.getItem[A](collection, id) match {
      case Some(response)  => response
      case None            => throw new Exception("error")//parseJSON(SmugmugAPI.getList(dataType, ids, params))
    }
  }


  // Parse a response in JSON to the object
  def parseJSON(json : JValue) : SmugmugData
}
