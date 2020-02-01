package ifany

trait Template {

  implicit def templateToString(t : Template) : String = t.toString

  // The view associated with the template
  implicit val view : View
}

object Template {

  def apply(s : String)(implicit v : View) : Template = new Template {
    val view = v
    override def toString : String = s
  }
}
