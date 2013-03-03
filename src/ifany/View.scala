package ifany

import java.util.Date

trait View {

  val name : String
  def getTitle : String

  def getMonth(d : java.util.Date) : String = d.getMonth match {
    case 0 => "January"
    case 1 => "February"
    case 2 => "March"
    case 3 => "April"
    case 4 => "May"
    case 5 => "June"
    case 6 => "July"
    case 7 => "August"
    case 8 => "September"
    case 9 => "October"
    case 10 => "November"
    case 11 => "December"
    case n => throw new Exception("Unrecognized month: " + n)
  }

  def getDay(d : java.util.Date) : String = d.getDay match {
    case 0 => "Sunday"
    case 1 => "Monday"
    case 2 => "Tuesday"
    case 3 => "Wednesday"
    case 4 => "Thursday"
    case 5 => "Friday"
    case 6 => "Saturday"
    case n => throw new Exception("Unrecognized weekday: " + n)
  }

  def getYear(d : java.util.Date) : String = (1900 + d.getYear).toString

  def getPostfix(d : java.util.Date) : String = d.getDate.toString.last.toString.toInt match {
    case 1 => "st"
    case 2 => "nd"
    case 3 => "rd"
    case _ => "th"
  }

  def getDate(images : List[Image], withDay : Boolean) : String = {

    // Get a list of all dates
    val dates : List[Date] = for (optionExif <- images.map(_.exif);
                                  exif <- optionExif) yield exif.dateTime

    dates match {
      case Nil  => ""
      case list => {
        val minDate = if (withDay) getMonth(dates.min) + " " + dates.min.getDate + getPostfix(dates.min) else getMonth(dates.min)
        val maxDate = if (withDay) getMonth(dates.max) + " " + dates.max.getDate + getPostfix(dates.max) else getMonth(dates.max)
        return {
          if (dates.min.getMonth == dates.max.getMonth) minDate + ", " + getYear(dates.min)
          else if (dates.min.getYear == dates.max.getYear) minDate + " to " + maxDate + ", " + getYear(dates.min)
          else minDate + ", " + getYear(dates.min) + " to " + maxDate + ", " + getYear(dates.min)
        }
      }
    }
  }

  def getAlbumCover(album : Album) : Image = album.cover match {
    case Left(id)   => throw new Exception("Cover image requested in view, but no cover image was set")
    case Right(im)  => im
  }
}
