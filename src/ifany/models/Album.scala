package ifany

import org.joda.time.DateTime 
import scala.io.Source
import java.io.FileNotFoundException
import scala.collection.JavaConverters._
import java.io.File
import awscala._, dynamodbv2._

case class Image(file : String,
                 description : String,
                 datetime : Option[String],
                 banner : Boolean,
                 cover : Boolean,
                 size : Seq[Int]) {

  def id: String = "id" ++ file.replace(".","-").replace("#","-").replace("/","--")

  def ratio: Double = size(1) / size(0).toDouble

  def url(size : String, albumURL : String) : String = {
    val album : String = if (albumURL.length == 0) "" else albumURL + "/" 
    val sizes : Map[String, String] = (Map.empty +
      ("t" -> "150x150") + ("s" -> "400x300") + 
      ("m" -> "600x450") + ("l" -> "800x600") +
      ("150" -> "150x150") + ("400" -> "400x300") +
      ("600" -> "600x450") + ("800" -> "800x600") +
      ("1024" -> "1024x768") + ("1280" -> "1280x980") +
      ("1600" -> "1600x1200") + ("2000" -> "2000x1500"))
    try {
      Ifany.photoDir + album + file + "_" + sizes(size) + ".jpg"
    } catch {
        case _ : Exception => throw InternalError("Image with size '" + size + "' doesn't exist")
    }
  }
}

case class Album(title : String,
                 description : String,
                 url : String,
                 galleries : Seq[String],
                 public : Option[Boolean],
                 images : Seq[Image]) {

  val datetime : (DateTime, DateTime) = {

    // Take all images that have a date associated and sort them
    val sorted_dates : Seq[DateTime] = {
      for (i <- images; dt <- i.datetime) yield new DateTime(i.datetime.get)
    } sortBy(_.getMillis)

    // Return the first and last date
    sorted_dates.size match {
      case 0 => throw InternalError("Album '" + url + "' has no date information associated with any pictures")
      case 1 => (sorted_dates.head, sorted_dates.head)
      case n => (sorted_dates.head, sorted_dates.last)
    }
  }

  val isPublic : Boolean = public.getOrElse(true)

  val getGallery : Option[String] = galleries match {
    case "all" +: rest => rest.headOption
    case g +: _ => Some(g)
    case otherwise => None
  }

  val path : String = getGallery match {
    case None => "album/" + url
    case Some(gURL) => gURL + "/" + url
  }
}


object Album {

  implicit val dynamoDB = DynamoDB.at(Region.EU_WEST_1)
  val albumTable = sys.env("ALBUMS_TABLE")
  val table: Table = dynamoDB.table(albumTable).get
  // Should be a map
  var albums: Option[Map[String, Album]] = None

  // Look up album in map
  def get(id: String): Album = getAll.get(id) match {
    case Some(a) => a
    case None => throw new AlbumNotFound(id)
  }

  private def albumFromItem(item: Item): Album = {
    val attributes: Map[String, AttributeValue] = item.attributes.map { case Attribute(k, v) => (k, v) }.toMap
    Album(
      title = attributes("title").s.get,
      description = attributes("description").s.getOrElse(""),
      url = attributes("url").s.get,
      galleries = attributes("galleries").l.map(av => AttributeValue(av).s.get),
      public = attributes("public").bl,
      images = attributes("images").l.map(av => imageFromAttributeValue(AttributeValue(av)))
    )
  }

  private def imageFromAttributeValue(value: AttributeValue): Image = {
    val attributes: scala.collection.Map[String, AttributeValue] = value.m.get.asScala.mapValues(AttributeValue(_))
    val im = Image(
      file = attributes("file").s.get,
      description = attributes("description").s.getOrElse(""),
      datetime = attributes("datetime").s,
      banner = attributes("banner").bl.get,
      cover = attributes("cover").bl.get,
      size = attributes("size").l.map(s => AttributeValue(s).n.get.toInt)
    )
    im
  }

  // Returns all albums in the photo dir
  def getAll: Map[String, Album] = albums.getOrElse {
    albums = Some(table.scan(filter = Seq(), limit = 99999).map(albumFromItem(_)).map(a => (a.url, a)).toMap)
    albums.get
  }

  def updateAll: Map[String, Album] = {
    albums = None
    getAll
  }
}
