package ifany

import net.liftweb.json._
import org.joda.time.DateTime
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._

@Salat
sealed trait SmugmugData


// Data for an album
case class Album(id : String, key : String, title : String, description : String, url : String, categoryId : Option[String], subCategoryId : Option[String], cover : Cover) extends SmugmugData

// Data for an Image
case class Image(id : String, key : String, caption : String, urls : Map[String,String], size : Size) extends SmugmugData

// Data for exif
case class EXIF(id : String, key : String, aperture : String, focalLength : String, iso : Int, model : String, dateTime : DateTime) extends SmugmugData

// Data for category
case class Category(id : String, name : String, description : String, url : String, subCategories : List[SubCategory]) extends SmugmugData

// Data for subCategory
case class SubCategory(id : String, name : String, url : String) extends SmugmugData

// Suplementary fields
case class Size(width : Int, height : Int)
case class Cover(id : String, key : String)

object Album extends DataLoader {

  def treatResponse(data : DataResponse) : Iterator[JValue] = data match {
    case DataList(ds) => for (d <- ds) yield parse(d)
    case DataItem(d) => (for (json <- (parse(d) \ "Albums").children) yield json).toIterator
  }


  def parseJSON(json : JValue) : Album = {
    val JInt(id) = json \ "id";
    val JString(key) = json \ "Key";
    val JString(url) = json \ "NiceName";
    val JString(title) = json \ "Title";
    val JString(description) = json \ "Description";
    val JInt(coverId) = json \ "Highlight" \ "id"
    val JString(coverKey) = json \ "Highlight" \ "Key"
    val categoryId = json \ "Category" \ "id" match {
      case JInt(id)   => Some(id.toString)
      case _          => None
    }
    val subcategoryId = json \ "SubCategory" \ "id" match {
      case JInt(id)   => Some(id.toString)
      case _          => None
    }
    // Use June scala dates instead of java date
    Album(id.toString, key, title, description, url, categoryId, subcategoryId, Cover(coverId.toString, coverKey))
  }
}
