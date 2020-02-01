package ifany

import java.time.LocalDateTime


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
    val dates : Seq[LocalDateTime] = {
      val unsorted = for (i <- images; dt <- i.datetime) yield new LocalDateTime(dt)
      unsorted.sortBy(_.getMillis)
    }

    // Get the day
    def day(dt : LocalDateTime) = withDay match {
      case true => dt.toString("MMMM d") + getPostfix(dt)
      case false => dt.toString("MMMM")
    }

    // Get the year
    def year(dt : LocalDateTime) = dt.toString("Y")

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

}
