package ifany

import scala.language.implicitConversions

trait Template {

  // The view associated with the template
  val view : View
}

object Template {

  given Conversion[Template, String] = _.toString

  def apply(s : String)(using v : View) : Template = new Template {
    val view = v
    override def toString : String = s
  }
}
