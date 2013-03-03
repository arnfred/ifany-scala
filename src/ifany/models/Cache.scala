package ifany

import com.mongodb.casbah.Imports._
import scala.util.Properties

object Cache {

  val dataField = "data"
  val MongoSetting(mongoDB) = Properties.envOrNone("MONGOHQ_URL")
  //val mongoDB = MongoClient(mongoString)(dbName)

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

// From:
// http://nihito.tumblr.com/post/12106440217/a-easy-setting-of-mongohq-by-scala-on-heroku-com
object MongoSetting {

  val localDbName = "ifany"

  def unapply(url: Option[String]): Option[MongoDB] = {
    val regex = """mongodb://(\w+):(\w+)@([\w|\.]+):(\d+)/(\w+)""".r
    url match {
      case Some(regex(u, p, host, port, dbName)) =>
        val db = MongoConnection(host, port.toInt)(dbName)
        db.authenticate(u,p)
        Some(db)
      case None =>
        Some(MongoConnection("localhost", 27017)(localDbName))
    }
  }
}
