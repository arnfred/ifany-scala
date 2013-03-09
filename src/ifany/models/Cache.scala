package ifany

import scala.reflect.runtime.universe._
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import scala.util.Properties

object Cache {

  println(Properties.envOrNone("MONGOHQ_URL"));
  val MongoSetting(mongoDB) = Some(Properties.envOrElse("MONGOHQ_URL", "mongodb://heroku:d9aa08dde373b85276f93b9b44dfdaa8@linus.mongohq.com:10004/app12728917"))
  //val mongoDB = MongoClient(mongoString)(dbName)

  def putItem[A <: SmugmugData : Manifest](dataType : String, id : String, data : A) : Unit = {

    // Serialize data
    val dbo = grater[A].asDBObject(data) + ("_id" -> id)
   
    // Get the right collection
    val mongoColl = mongoDB(dataType)

    // Then save
    mongoColl.save(dbo)
  }

  def getItem[A <: AnyRef : Manifest](dataType : String, id : String) : Option[A] = {
    // Get the right collection
    val mongoColl = mongoDB(dataType)

    println(manifest[A].erasure.getName)
    // Then load
    mongoColl.findOne(Map("_id" -> id)) match {
      case Some(data) => Some(grater[A].asObject(data))
      case None       => None
    }
  }

  def getList[A <: SmugmugData : Manifest](dataType : String, ids : Iterator[String]) : Iterator[A] = {
    // Get the right collection
    val mongoColl = mongoDB(dataType)

    // Prepare id map
    val idMap = (for (id <- ids) yield ("_id" -> id)).toMap

    // Load all ids
    for (data <- mongoColl.find("_id" $in ids.toList)) yield {
      grater[A].asObject(data)
    }
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
