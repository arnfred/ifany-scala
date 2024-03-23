package ifany

import java.time.LocalDateTime;
import scala.io.Source
import java.io.FileNotFoundException
import java.security.MessageDigest
import java.math.BigInteger
import scala.jdk.CollectionConverters._
import scala.util.Try
import java.io.File
import awscala._, dynamodbv2._

case class Image(file : String,
                 description : String,
                 datetime : Option[String],
                 banner : Boolean,
                 cover : Boolean,
                 size : Seq[Int],
                 published: Boolean,
                 is_video: Boolean) {

  def id: String = s"id-${file.replace("/", "--").replace(".", "--")}"

  def ratio: Double = size(1) / size(0).toDouble

  def isVertical: Boolean = if (ratio < 1) false else true

  def height(label: String): Int = isVertical match {
    case false => label match {
      case "t"        => 150
      case "original" => size(1)
      case _          => math.min(size(1), (size(1) * (width(label) / size(0).toDouble)).toInt)
    }
    case true => label match {
      case "t"        => 150
      case "s"        => math.min(300, size(0))
      case "m"        => math.min(450, size(0))
      case "l"        => math.min(600, size(0))
      case "original" => size(1)
      case _          => math.min(label.toInt, size(1))
    }
  }

  def width(label: String): Int = isVertical match {
    case false  => label match {
      case "t"        => 150
      case "s"        => math.min(400, size(0))
      case "m"        => math.min(600, size(0))
      case "l"        => math.min(800, size(0))
      case "original" => size(0)
      case _          => math.min(label.toInt, size(0))
    }
    // When images are saved, they are truncated between a width and a height
    // that assumes a horisontal image. That means that if we ask for a
    // vertical image between "800x600" we get back an image that is 450x600
    // (assuming the aspect ratio is the same).
    //
    // This is really dumb, but it's the way that things have worked for a long
    // time, and trying to redo it would take a lot of work.
    //
    // The best fix would be to reupload images and change how they are sized,
    // but that's not very doable, so instead I'm providing a translation table
    // which encodes the fact that a vertical image needs to be just under
    // double the size of a horizontal one.
    case true => label match {
      case "t"        => 150
      case "original" => size(0)
      case _          => math.min(size(0), (size(0) * (height(label) / size(1).toDouble)).toInt)
    }
  }

  def versions: Seq[String] = {
    val allLabels = Seq("150", "400", "600", "800", "1024", "1280", "1600", "2000", "3200")
    val filtered = for (label <- allLabels if (width(label) <= size(0))) yield label
    filtered.toSeq ++ Seq("original")
  }

  def imageURL(album: String, size: String): String = {
    val sizes : Map[String, String] = (Map.empty +
      ("t" -> "150x150") + ("s" -> "400x300") + 
      ("m" -> "600x450") + ("l" -> "800x600") +
      ("150" -> "150x150") + ("400" -> "400x300") +
      ("600" -> "600x450") + ("800" -> "800x600") +
      ("1024" -> "1024x768") + ("1280" -> "1280x980") +
      ("1600" -> "1600x1200") + ("2000" -> "2000x1500") +
      ("3200" -> "3200x2400") + ("original" -> "original"))
    try {
      S3Photo.getPresignedURL(album, file + "_" + sizes(size) + ".jpg")
    } catch {
      case _ : Exception => throw InternalError("Image with size '" + size + "' doesn't exist")
    }
  }

  def videoURL(album: String) = S3Photo.getPresignedURL(album, file + ".mp4")
}

case class Album(title : String,
                 description : String,
                 url : String,
                 galleries : Seq[String],
                 visible : Boolean,
                 secret : Option[String],
                 images : Seq[Image],
                 datetime: (LocalDateTime, LocalDateTime)) {

  val getGallery : Option[String] = galleries match {
    case "all" +: rest => rest.headOption
    case g +: _ => Some(g)
    case otherwise => None
  }

  val path : String = getGallery match {
    case None => "album/" + url
    case Some(gURL) => gURL + "/" + url
  }

  val size : Int = images.size

}


object Album {

  implicit val dynamoDB: DynamoDB = DynamoDB.at(Region.EU_WEST_1)
  val albumTable = sys.env("ALBUMS_TABLE")
  val table: Table = dynamoDB.table(albumTable).get
  // Should be a map
  var albums: Option[Map[String, Album]] = None

  def dynamic(title: String, desc: String, images: Seq[Image], datetime: (LocalDateTime, LocalDateTime)): Album =
    Album(title, desc, "", Seq(), false, None, images, datetime)

  // Look up album in map
  def get(id: String, password: Option[String]): Album = getAll.get(id) match {
    case Some(album) => {
      val showPrivate = password.map{ pass =>
        val secretSHA = hash64(pass, id)
        album.secret.getOrElse("") == secretSHA } .getOrElse(false)
      val images = album.images.filter(im => showPrivate || im.published)
      val visible = showPrivate || album.visible
      album.copy(visible=visible, images=images)
    }
    case None => throw new AlbumNotFound(id)
  }

  def datetimeFromImages(images: Seq[Image], id: String): (LocalDateTime, LocalDateTime) = {

    // Take all images that have a date associated and sort them
    val sorted_dates : Seq[LocalDateTime] = {
      for (i <- images;
           dt <- i.datetime;
           d <- Try(LocalDateTime.parse(dt)).toOption) yield d
    } .sorted

    // Return the first and last date
    sorted_dates.size match {
      case 0 => throw InternalError("Album '" + id + "' has no date information associated with any pictures")
      case 1 => (sorted_dates.head, sorted_dates.head)
      case n => (sorted_dates.head, sorted_dates.last)
    }
  }

  private def albumFromDBItem(item: Item): Option[Album] = {
    val attributes: Map[String, AttributeValue] = item.attributes.map { case Attribute(k, v) => (k, v) }.toMap
    val images = attributes("images").l.map(av => imageFromAttributeValue(AttributeValue(av)))
    val url = attributes("url").s.get
    if (images.size == 0) {
      None
    } else {
      Some(Album(
        title = attributes("title").s.get,
        description = attributes("description").s.getOrElse(""),
        url = url,
        galleries = attributes("galleries").l.map(av => AttributeValue(av).s.get),
        visible = attributes("public").bl.getOrElse(true),
        secret = attributes.get("secret").flatMap(_.s),
        images = images,
        datetime = datetimeFromImages(images, url)
      ))
    }
  }

  private def imageFromAttributeValue(value: AttributeValue): Image = {
    val attributes: scala.collection.MapView[String, AttributeValue] = value.m.get.asScala.view.mapValues(AttributeValue(_))
    val im = Image(
      file = attributes("file").s.get,
      description = attributes("description").s.getOrElse(""),
      datetime = attributes("datetime").s,
      banner = attributes("banner").bl.get,
      cover = attributes("cover").bl.get,
      size = attributes("size").l.map(s => AttributeValue(s).n.get.toInt),
      published = attributes.get("published").flatMap(_.bl).getOrElse(true),
      is_video = attributes.get("is_video").flatMap(_.bl).getOrElse(false)
    )
    im
  }

  // Returns all albums in the photo dir
  private def getAll: Map[String, Album] = albums.getOrElse {
    albums = Some(table.scan(filter = Seq(), limit = 99999)
                       .map(albumFromDBItem(_))
                       .collect { case Some(a) => (a.url, a) }
                       .toMap)
    albums.get
  }

  // Refresh all albums and return only visible albums with visible images
  def updateAll: Map[String, Album] = {
    albums = None
    // Only include published images and public albums
    getAll.map { case (k, a) =>
      val images: Seq[Image] = a.images.filter(im => im.published)
      val visible: Boolean = a.visible && images.size > 0
      (k, a.copy(visible=visible, images=images))
    }.filter(_._2.visible)
  }

  // From: https://stackoverflow.com/a/46332228/1722504
  private def hash64(password: String, id: String): String = {
    val data = password + id
    MessageDigest.getInstance("SHA-256")
      .digest(data.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }
}
