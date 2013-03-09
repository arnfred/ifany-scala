package ifany

import net.liftweb.json._


sealed trait SmugmugData


// Data for an album
case class Album(id : String, key : String, title : String, description : String, url : String, categoryId : Option[String], subCategoryId : Option[String], cover : (String,String)) extends SmugmugData

// Data for an Image
case class Image(id : String, key : String, caption : String, urls : Map[String,String], size : Size) extends SmugmugData

// Data for exif
case class EXIF(id : String, key : String, aperture : String, focalLength : String, iso : Int, model : String, dateTime : Date) extends SmugmugData

// Data for category
case class Category(id : String, name : String, description : String, url : String, subCategories : List[SubCategory]) extends SmugmugData

// Data for subCategory
case class SubCategory(id : String, name : String, url : String) extends SmugmugData

case class Size(width : Int, height : Int)

object Album extends DataLoader {

  def treatResponse(data : DataResponse) : Iterable[JSON] = data match {
    case DataList(ds) => for (d <- ds) yield parse(d)
    case DataItem(d) => for (json <- (parse(text) \ "Albums").children) yield json
  }


  def parseJSON(json : JSON) : Album = {
    val JInt(id) = json \ "id";
    val JString(key) = json \ "Key";
    val JString(url) = json \ "NiceName";
    val JString(title) = json \ "Title";
    val JString(description) = json \ "Description";
    val JInt(coverId) = json \ "Highlight" \ "id"
    val categoryId = c \ "Category" \ "id" match {
      case JNothing   => None
      case JInt(id)   => Some(id.toString)
    }
    val subcategoryId = c \ "SubCategory" \ "id" match {
      case JNothing   => None
      case JInt(id)   => Some(id.toString)
    }
    // Use June scala dates instead of java date
    Album(id.toString, key, title, description, url, categoryId, subcategoryId, coverId.toString)
  }
}
