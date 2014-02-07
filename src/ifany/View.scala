package ifany

import org.joda.time.DateTime 
import org.joda.time.format.DateTimeFormat


trait View {

  val name : String
  def getTitle : String

  def getPostfix(d : DateTime) : String = (d.getDayOfMonth % 100, d.getDayOfMonth % 10) match {
    case (11,_) => "th"
    case (_,1)  => "st"
    case (_,2)  => "nd"
    case (_,3)  => "rd"
    case _      => "th"
  }


  def getDateString(images : List[Image], withDay : Boolean) : String = {

    // Get a list of all dates
    val dates : List[DateTime] = {
      (for (i <- images; dt <- i.datetime) yield dt).sortBy(_.getMillis)
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

}
