package ifany

import com.mongodb.casbah.Imports._

object Cache {

  val dbName = "ifany"
  val dataField = "data"
  val mongoDB = MongoClient()(dbName)

  def save(id : String, coll : String, data : String) : Unit = {
   
    // Get the right collection
    val mongoColl = mongoDB(coll)

    // Then save
    mongoColl.save(Map("_id" -> id, dataField -> data))
  }

  def load(id : String, coll : String) : Option[String] = {
    // Get the right collection
    val mongoColl = mongoDB(coll)

    // Then load
    for (col <- mongoColl.findOne(Map("_id" -> id))) yield col(dataField).asInstanceOf[String]
  }
}
