package ifany

import org.joda.time.DateTime 
import org.joda.time.format.DateTimeFormat


trait View {

  val name : String
  def getTitle : String

  def getPostfix(d : DateTime) : String = d.getDayOfMonth.toString.last match {
    case '1' => "st"
    case '2' => "nd"
    case '3' => "rd"
    case _ => "th"
  }

  def getDate(exifs : List[EXIF], withDay : Boolean) : String = {

    // Get a list of all dates
    val dates : List[DateTime] = {
      (for (exif <- exifs) yield exif.dateTime).sortBy(_.getMillis)
    }

    // Get the day
    def day(dt : DateTime) = withDay match {
      case true => dt.toString("MMMM d") + getPostfix(dt)
      case false => dt.toString("MMMM")
    }

    // Get the year
    def year(dt : DateTime) = dt.toString("Y")

    dates match {
      case Nil  => ""
      case list => {
        val first = dates.head
        val last = dates.last
        val sameMonth = first.monthOfYear == last.monthOfYear
        val sameYear = first.getYear == last.getYear
        return {
          if (sameMonth) day(first) + ", " + year(first)
          else if (sameYear) day(first) + " to " + day(last) + ", " + year(first)
          else day(first) + " " + year(first) + " to " + day(last) + " " + year(last)
        }
      }
    }
  }

  def getAlbumCoverEXIF(album : Album, exifMap : Map[String, EXIF]) : EXIF = {
    exifMap(album.id) 
  }

  def getAlbumCoverImage(album : Album, imageMap : Map[String, List[Image]]) : Image = {
    imageMap(album.id) filter { _.id == album.cover.id } match {
      case Nil => throw new Exception("Cover image requested in view, but no cover image was set")
      case list => list.head
    }
  }
}
