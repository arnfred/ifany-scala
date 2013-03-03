package ifany

trait Template {

  // Implicit conversion 
  import com.dongxiguo.fastring.Fastring.Implicits._

  implicit def fastToString(f : Fastring) : String = f.toString
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
