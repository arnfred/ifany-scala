package ifany

import java.time.LocalDateTime;
import scala.util.Random.shuffle
import awscala._, dynamodbv2._
import scala.util.Try

case class Gallery(name : String, description : String, url: String, albums : Seq[Album]) {

  val cover : Cover = {
    val covers = for (a <- albums; i <- a.images if (i.cover || i.banner)) yield {
      Cover(i, a)
    }
    if (covers.size > 0) shuffle(covers).head

    // If we have no covers, just use any image in landscape format (width > height)
    else {
      val landscapes = {
        for (a <- albums; i <- a.images if !i.isVertical && !i.is_video) yield {
          Cover(i, a)
        }
      }
      shuffle(landscapes).head
    }
  }

  def medianAge: LocalDateTime = {
    val datetimes: Seq[LocalDateTime] = for (a <- albums;
                                             i <- a.images;
                                             dt <- i.datetime;
                                             d <- Try(LocalDateTime.parse(dt)).toOption) yield d
    val sorted_dates: Seq[LocalDateTime] = datetimes.sorted
    if (sorted_dates.length > 0) sorted_dates(datetimes.length / 2) else LocalDateTime.now()
  }
}

object Gallery {

  implicit val dynamoDB: DynamoDB = DynamoDB.at(Region.EU_WEST_1)
  val galleryTable = sys.env("GALLERIES_TABLE")
  val table: Table = dynamoDB.table(galleryTable).get
  var galleries: Option[Seq[Gallery]] = None

  def getAllGalleries: Seq[Gallery] = galleries.getOrElse {
    // Get all albums. We use `updateAll` to make sure that secrets are updated
    // when we run `update`
    val albums = for ((k,a) <- Album.updateAll if a.visible) yield a
    // Group albums by gallery
    val grouped: Map[String, Seq[Album]] = { 
      for (a <- albums; g <- a.galleries) yield (g -> a)
    } groupBy { // Returns type of Seq[String, Array[(String, Album)]]
      case (k,v) => k
    } map { // Simplify touple to Album
      case (k,v) => (k, v.map(_._2).toSeq)
    } map { // Sort all lists of albums by date
      case (k,v) => (k, v.sortBy(_.datetime._2))
    }
    val gals = table.scan(filter = Seq(), limit = 99999).map(galleryFromItem(_, grouped)).collect { case Some(g) => g }
    galleries = Some(gals.sortBy(_.medianAge).reverse)
    galleries.get
  }
  
  def updateGalleries: Seq[Gallery] = {
    galleries = None
    getAllGalleries
  }


  private def galleryFromItem(item: Item, albumMap: Map[String, Seq[Album]]): Option[Gallery] = {
    val attributes: Map[String, AttributeValue] = item.attributes.map { case Attribute(k, v) => (k, v) }.toMap
    val name = attributes("name").s.getOrElse("")
    val description = attributes.get("description").flatMap(_.s).getOrElse("")
    val url = attributes("url").s.get
    val albums = albumMap.getOrElse(attributes("url").s.get, Seq.empty)
    if (albums.length > 0) Some(Gallery(name, description, url, albums)) else None
  }

  def get(url : String) : Gallery = {
    try {
      getAllGalleries.find(g => g.url == url).get
    } catch {
      case (error : java.util.NoSuchElementException) => throw GalleryNotFound(url)
    }
  }

  def getOption(galleryURL : String) : Option[Gallery] = try {
    Some(get(galleryURL))
  } catch {
    case GalleryNotFound(url) => None
  }
}
