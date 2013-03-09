package ifany

import net.liftweb.json._


sealed abstract class DataResponse
case class DataList(data : Iterator[String]) extends DataResponse
case class DataItem(data : String) extends DataResponse


trait DataLoader {

  def getIds(idKeys : Iterator[(String, String)]) : Iterator[String] = {
    for ((id, keys) <- idKeys) yield id
  }

  def getList(dataType : String, 
              idKeys : Iterator[(String, String)], 
              params : Map[String, String] = Map.empty) : Iterator[AnyRef] = {

    // TODO: wrap the whole thing in a future
    val response = Cache.getList(dataType, getIds(idKeys))
    for (r <- response) yield r

    /* Cache.getList(dataType, getIds(idKeys)) match { */
    /*   case response if (!response.isEmpty)  => for (r <- response) yield r.asInstanceOf[A] */
    /*   case _                                => { */
    /*     val ls = treatResponse(SmumugAPI.getList(dataType, ids, params)) */
    /*     for (json <- ls) yield parseJSON(json) */
    /*   } */
    /* } */
  }


  def getItem(dataType : String,
              id : String = "none",
              params : Map[String, String] = Map.empty) : AnyRef = {

    // TODO: wrap the whole thing in a future
    Cache.getItem(dataType, id) match {
      case Some(response)  => response
      case None            => throw new Exception("error")//parseJSON(SmugmugAPI.getList(dataType, ids, params))
    }
  }


  // Take a dataresponse and either break up the json or keep it as is
  def treatResponse(data : DataResponse) : Iterator[JValue]
  

  // Parse a response in JSON to the object
  def parseJSON(json : JValue) : SmugmugData
}
