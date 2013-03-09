package ifany

import net.liftweb.json._


sealed abstract class DataResponse
case class DataList(data : Iterable[String]) extends DataResponse
case class DataItem(data : String) extends DataResponse


trait DataLoader[A <: SmugmugData] {

  def getList(dataType : String, 
              idKeys : Iterable[(String, String)], 
              params : Map[String, String] = Map.empty) : Iterable[A] = {

    // TODO: wrap the whole thing in a future
    Cache.getList(dataType, ids) match {
      case Some(response)  => for (r <- response) yield r.asInstanceOf[A]
      case None            => {
        val ls = treatResponse(SmumugAPI.getList(dataType, ids, params))
        for (json <- ls) yield parseJSON(json)
      }
    }
  }


  def getItem(dataType : String,
              id : String = 0,
              params : Map[String, String] = Map.empty) : A = {

    // TODO: wrap the whole thing in a future
    Cache.getItem(dataType, id) match {
      case Some(response)  => response.asInstanceOf[A]
      case None            => parseJSON(SmugmugAPI.getList(dataType, ids, params))
    }
  }


  // Take a dataresponse and either break up the json or keep it as is
  def treatResponse(data : DataResponse) : Iterable[JSON]
  

  // Parse a response in JSON to the object
  def parseJSON(json : JSON) : A
}
