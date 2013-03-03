package ifany

import dispatch._
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import java.util.Date

case class Gallery(albums : List[Album], categories : List[Category]) {
  def getImages : Promise[Gallery] = {
    val newAlbums = for (album <- albums) yield album.getImages
    for (as <- Http.promise.all(newAlbums)) yield Gallery(as.toList, categories)
  }
  def album(url : String) : Option[Album] = albums.filter { album => album.url == url } match {
    case Nil  => None
    case album :: _ => Some(album)
  }
}

case class Album(id : String, key : String, title : String, description : String, url : String, images : List[Image], categoryId : Option[String], subCategoryId : Option[String], cover : Either[String,Image]) {
  def getImages : Promise[Album] = {

    def getCover(c : Either[String,Image], images : List[Image]) : Promise[Image] = c match {
      case Left(id) => images.find(_.id == id).get.getEXIF
      case Right(i) => Http.promise(i)
    }

    for (images <- Image.getAll(id,key);
         c <- getCover(cover, images)) yield {
      Album(id,key,title,description,url,images,categoryId,subCategoryId, Right(c)) }
  }

  def getExif : Promise[Album] = {
    val newImages = for (image <- images) yield image.getEXIF
    for (is <- Http.promise.all(newImages)) yield {
      Album(id,key,title, description, url, is.toList, categoryId, subCategoryId, cover)
    }
  }

  def thumbnails : List[String] = images.map(_.urls("thumb"))
}

case class Image(id : String, key : String, caption : String, urls : Map[String,String], exif : Option[EXIF], size : Size) {
  def getEXIF : Promise[Image] = EXIF.fetch(id,key).map { e : EXIF => Image(id, key, caption, urls, Some(e), size) }
}

case class EXIF(id : String, key : String, aperture : String, focalLength : String, iso : Int, model : String, dateTime : Date)

case class Category(id : String, name : String, description : String, url : String, subCategories : List[SubCategory]) {
  def getSubs : Promise[Category] = SubCategory.getAll(id).map { Category(id, name, description, url, _) }
}

case class Size(width : Int, height : Int)

case class SubCategory(id : String, name : String, url : String)

object Gallery {

  def update : Unit = {
    Album.update
    Category.update
  }

  def fetch : Promise[Gallery] = {
    val albums = Album.getAll
    val categories = Category.getAll
    for {
      a <- albums
      c <- categories
    } yield Gallery(a, c)
  }
}

object Album extends Smugmug {

  val method : String = "albums.get"

  def update : Unit = {
    val params = Map(("Heavy" -> "true"))
    val p = getRemote(method, params)
  }
  
  def getAll : Promise[List[Album]] = {

    // Get initial promise
    val params = Map(("Heavy" -> "true"))
    for (a <- get(method, params)) yield parseJSON(a)
  }


  def parseJSON(text : String) : List[Album] = {
    for { (c,index) <- (parse(text) \ "Albums").children.zipWithIndex;
          JInt(id) = c \ "id";
          JString(key) = c \ "Key";
          JString(url) = c \ "NiceName";
          JString(title) = c \ "Title";
          JString(description) = c \ "Description";
          JInt(coverId) = c \ "Highlight" \ "id"
    } yield {
          val categoryId = c \ "Category" \ "id" match {
            case JNothing   => None
            case JInt(id)   => Some(id.toString)
          }
          val subcategoryId = c \ "SubCategory" \ "id" match {
            case JNothing   => None
            case JInt(id)   => Some(id.toString)
          }
          // Use June scala dates instead of java date
          Album(id.toString, key, title, description, url, Nil, categoryId, subcategoryId, Left(coverId.toString))
    }
  }
}


object Image extends Smugmug {

  val method : String = "images.get"

  def update(id : String, key : String) : Unit = {
    val params = Map(("AlbumID" -> id), ("AlbumKey" -> key), ("Heavy" -> "true"))
    val p = getRemote(method, params, id)
  }


  def getAll(id : String, key : String) : Promise[List[Image]] = {

    // Get initial promise
    val params = Map(("AlbumID" -> id), ("AlbumKey" -> key), ("Heavy" -> "true"))
    for (i <- get(method, params, id)) yield parseJSON(i)
  }

  def parseJSON(text : String) : List[Image] = {
    for { c <- (parse(text) \ "Album" \ "Images").children; 
          JInt(id) = c \ "id";   
          JString(key) = c \ "Key";   
          JString(caption) = c \ "Caption";
          JString(tiny) = c \ "TinyURL";
          JString(thumb) = c \ "ThumbURL";
          JString(small) = c \ "SmallURL";
          JString(medium) = c \ "MediumURL";
          JString(large) = c \ "LargeURL";
          JString(xlarge) = c \ "XLargeURL";
          JString(x2large) = c \ "X2LargeURL";
          JString(x3large) = c \ "X3LargeURL";
          JString(original) = c \ "OriginalURL";
          JInt(height) = c \ "Height";
          JInt(width) = c \ "Width";
          urls = Map(("tiny" -> tiny), ("thumb" -> thumb), ("small" -> small), ("medium" -> medium), ("large" -> large), ("xlarge" -> xlarge), ("x2large" -> x2large), ("x3large" -> x3large), ("original" -> original))
    } yield Image(id.toString, key, caption, urls, None, Size(width.toInt, height.toInt))
  }

}

object EXIF extends Smugmug {

  val method : String = "images.getEXIF"

  def update(id : String, key : String) : Unit = {
    val params = Map(("ImageID" -> id), ("ImageKey" -> key))
    val p = getRemote(method, params, id)
  }

  def fetch(id : String, key : String) : Promise[EXIF] = {
    // Get initial promise
    val params = Map(("ImageID" -> id), ("ImageKey" -> key))
    for (e <- get(method, params, id)) yield parseJSON(e)
  }

  def parseJSON(text : String) : EXIF = {
    val exif = (parse(text) \ "Image")
    val JString(key) = exif \ "Key"
    val JInt(id) = exif \ "id"
    val JString(aperture) = exif \ "Aperture"
    val JString(focalLength) = exif \ "FocalLength"
    // TODO, figure out why the iso is so problematic
    val iso = exif \ "ISO" match {
      case JInt(n) => n.toInt
      case JNothing => 0
    }
    val JString(exposure) = exif \ "ExposureTime"
    val JString(dateTimeOriginal) = exif \ "DateTimeOriginal"
    val JString(dateTime) = exif \ "DateTime"
    val JString(model) = exif \ "Model"
    val date = if (dateTimeOriginal.take(10) == "2000-01-01") dateTime else dateTimeOriginal
    EXIF(id.toString, key, aperture, focalLength, iso, model, new Date(date.replace('-','/')))
  }
}


object Category extends Smugmug {

  val method : String = "categories.get"

  def update : Unit = {
    val p = getRemote(method)
  }

  def getAll : Promise[List[Category]] = {

    for (c <- get(method)) yield parseJSON(c)
  }

  def parseJSON(text : String) : List[Category] = {
    for { c <- (parse(text) \ "Categories").children; 
          JString(desc) = c \ "Name";
          JString(url) = c \ "NiceName";   
          JInt(id) = c \ "id";
          JString(t) = c \ "Type";
          if t == "User"
    } yield Category(id.toString, url.replace('-',' '), desc, url, Nil)
  }
}


object SubCategory extends Smugmug {

  val method = "subcategories.get"

  
  def update(id : String) : Unit = {
    val params = Map("CategoryID" -> id.toString)
    val p = getRemote(method, params, id)
  }

  def getAll(id : String) : Promise[List[SubCategory]] = {

    val params = Map("CategoryID" -> id.toString)
    for (s <- get(method, params, id)) yield parseJSON(s)
  }

  def parseJSON(text : String) : List[SubCategory] = {
    for { c <- (parse(text) \ "SubCategories").children; 
          JString(name) = c \ "Name";
          JString(url) = c \ "NiceName";   
          JInt(id) = c \ "id"
        } yield SubCategory(id.toString, name, url)
  }
}
