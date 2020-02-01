package ifany

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId


trait View {

  val name : String
  def getTitle : String

  def getPostfix(d : LocalDateTime) : String = (d.getDayOfMonth % 100, d.getDayOfMonth % 10) match {
    case (11,_) => "th"
    case (_,1)  => "st"
    case (_,2)  => "nd"
    case (_,3)  => "rd"
    case _      => "th"
  }


  def getDateString(images : Seq[Image], withDay : Boolean) : String = {

    // Get a list of all dates
    val zoneId = ZoneId.systemDefault
    val dates : Seq[LocalDateTime] = {
      val unsorted = for (i <- images; dt <- i.datetime) yield LocalDateTime.parse(dt)
      unsorted.sortBy(_.atZone(zoneId).toEpochSecond)
    }

    val monthDayFormatter = DateTimeFormatter.ofPattern("MMMM d")
    val monthOnlyFormatter = DateTimeFormatter.ofPattern("MMMM")
    val yearOnlyFormatter = DateTimeFormatter.ofPattern("Y")
    // Get the day
    def day(dt : LocalDateTime) = withDay match {
      case true => dt.format(monthDayFormatter) + getPostfix(dt)
      case false => dt.format(monthOnlyFormatter)
    }

    // Get the year
    def year(dt : LocalDateTime) = dt.format(yearOnlyFormatter)

    dates match {
      case Nil  => ""
      case list => {
        val first = dates.head
        val last = dates.last
        val sameMonth = first.getMonth == last.getMonth
        val sameYear = first.getYear == last.getYear
        return {
          if (sameMonth) day(first) + ", " + year(first)
          else if (sameYear) day(first) + " to " + day(last) + ", " + year(first)
          else day(first) + " " + year(first) + " to " + day(last) + " " + year(last)
        }
      }
    }
  }

}
