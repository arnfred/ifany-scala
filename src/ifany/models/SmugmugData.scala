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
case class Image(id : String, key : String, caption : String, urls : Urls, size : Size) extends SmugmugData

// Data for exif
case class EXIF(id : String, key : String, aperture : String, focalLength : String, iso : Int, model : String, dateTime : DateTime) extends SmugmugData

// Data for category
case class Category(id : String, name : String, description : String, url : String) extends SmugmugData

// Data for subCategory
case class SubCategory(id : String, name : String, url : String) extends SmugmugData

// Suplementary fields
case class Size(width : Int, height : Int)
case class Cover(id : String, key : String)
case class Urls(tiny : String, thumb : String, small : String, medium : String, large : String, xlarge : String, x2large : String, x3large : String, original : String)



object Album extends DataLoader {

  val collection : String = "albums"
  val method : String = "albums.get"

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

object Image extends DataLoader {

  val collection : String = "images"
  val method : String = "images.get"

  def treatResponse(data : DataResponse) : Iterator[JValue] = data match {
    case DataList(ds) => for (d <- ds) yield parse(d)
    case DataItem(d) => (for (json <- (parse(d) \ "Album" \ "Images").children) yield json).toIterator
  }

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
    val urls = Urls(tiny, thumb, small, medium, large, xlarge, x2large, x3large, original)
    Image(id.toString, key, caption, urls, Size(width.toInt, height.toInt))
  }
}


object EXIF extends DataLoader {

  val collection : String = "exif"
  val method : String = "images.getEXIF"

  def treatResponse(data : DataResponse) : Iterator[JValue] = data match {
    case DataList(ds) => for (d <- ds) yield parse(d) \ "Image"
    case DataItem(d) => Iterator(parse(d) \ "Image")
  }

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
    EXIF(id.toString, key, aperture, focalLength, iso, model, new DateTime(date))
  }
}


object Category extends DataLoader {

  val collection : String = "categories"
  val method : String = "categories.get"

  def treatResponse(data : DataResponse) : Iterator[JValue] = data match {
    case DataList(ds) => for (d <- ds) yield parse(d)
    case DataItem(d) => (for (json <- (parse(d) \ "Categories").children;
                              JString(t) = json \ "Type" if t == "User") yield json).toIterator
  }

  def parseJSON(json : JValue) : Category = {
    val JString(desc) = json \ "Name";
    val JString(url) = json \ "NiceName";   
    val JInt(id) = json \ "id";
    val JString(t) = json \ "Type";
    Category(id.toString, url.replace('-',' '), desc, url)
  }
}
