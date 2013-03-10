package ifany

import net.liftweb.json._
import org.joda.time.DateTime
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._

@Salat
sealed trait SmugmugData

sealed abstract class ImageUrl(url : String) {
  def urls(size : String) : String = size match {
    case "tiny" => url.replace("__SIZE__","Ti")
    case "thumb" => url.replace("__SIZE__","Th")
    case "small" => url.replace("__SIZE__","S")
    case "medium" => url.replace("__SIZE__","M")
    case "large" => url.replace("__SIZE__","L")
    case "xlarge" => url.replace("__SIZE__","XL")
    case "x2large" => url.replace("__SIZE__","X2")
    case "x3large" => url.replace("__SIZE__","X3")
    case "original" => url.replace("__SIZE__","O").replace("-O","")
  }
}


// Data for an album
case class Album(id : String, key : String, title : String, description : String, imageIDs : List[String], url : String, categoryID : Option[String], cover : Cover, nav : Navigation) extends SmugmugData

// Data for an Image
case class Image(id : String, key : String, albumID : String, caption : String, url : String, size : Size) extends ImageUrl(url) with SmugmugData

// Data for exif
case class EXIF(id : String, key : String, albumID : String, aperture : String, focalLength : String, iso : Int, model : String, dateTime : DateTime) extends SmugmugData

// Data for category
case class Category(id : String, name : String, description : String, url : String) extends SmugmugData

// Data for subCategory
case class SubCategory(id : String, name : String, url : String) extends SmugmugData

// Suplementary fields
case class Size(width : Int, height : Int)
case class Cover(id : String, key : String)
case class Navigation(next : Option[String], prev : Option[String])



object Album extends DataLoader {

  val collection : String = "albums"


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
    // Use June scala dates instead of java date
    Album(id.toString, key, title, description, Nil, url, categoryId, Cover(coverId.toString, coverKey), Navigation(None,None))
  }
}

object Image extends DataLoader {

  val collection : String = "images"


  def parseJSON(json : JValue) : Image = {
    val JInt(id) = json \ "id";   
    val JString(key) = json \ "Key";   
    val JString(caption) = json \ "Caption";
    val JString(tiny) = json \ "TinyURL";
    val JString(thumb) = json \ "ThumbURL";
    val JString(small) = json \ "SmallURL";
    val JString(medium) = json \ "MediumURL";
    val JString(large) = json \ "LargeURL";
    val JString(xlarge) = json \ "XLargeURL";
    val JString(x2large) = json \ "X2LargeURL";
    val JString(x3large) = json \ "X3LargeURL";
    val JString(original) = json \ "OriginalURL";
    val JInt(height) = json \ "Height";
    val JInt(width) = json \ "Width";
    val url = tiny.replace("Ti","__SIZE__")
    Image(id.toString, key, "none", caption, url, Size(width.toInt, height.toInt))
  }

  def getAlbumImages(albumID : String, limit : Option[Int] = None) : List[Image] = {
    Cache.getQuery[Image](collection, Map("albumID" -> albumID), limit)
  }
}


object EXIF extends DataLoader {

  val collection : String = "exif"

  def parseJSON(json : JValue) : EXIF = {
    val JString(key) = json \ "Key"
    val JInt(id) = json \ "id"
    val JString(aperture) = json \ "Aperture"
    val JString(focalLength) = json \ "FocalLength"
    // TODO, figure out why the iso is so problematic
    val iso = json \ "ISO" match {
      case JInt(n) => n.toInt
      case _ => 0
    }
    val JString(exposure) = json \ "ExposureTime"
    val JString(dateTimeOriginal) = json \ "DateTimeOriginal"
    val JString(dateTime) = json \ "DateTime"
    val JString(model) = json \ "Model"
    val date = if (dateTimeOriginal.take(10) == "2000-01-01") dateTime else dateTimeOriginal
    EXIF(id.toString, key, "none", aperture, focalLength, iso, model, new DateTime(date.replace(" ","T")))
  }
}


object Category extends DataLoader {

  val collection : String = "categories"
  val method : String = "categories.get"

  def treatResponse(data : DataResponse) : List[JValue] = data match {
    case DataList(ds) => for (d <- ds) yield parse(d)
    case DataItem(d) => (for (json <- (parse(d) \ "Categories").children;
                              JString(t) = json \ "Type" if t == "User") yield json)
  }

  def parseJSON(json : JValue) : Category = {
    val JString(desc) = json \ "Name";
    val JString(url) = json \ "NiceName";   
    val JInt(id) = json \ "id";
    val JString(t) = json \ "Type";
    Category(id.toString, url.replace('-',' '), desc, url)
  }
}
