package ifany

import org.joda.time.DateTime 
import scala.io.Source
import java.io.FileNotFoundException
import net.liftweb.json._
import java.io.File

case class Navigation(next : Option[NavElem], 
                      prev : Option[NavElem], 
                      gallery : Option[NavElem])

case class NavElem(url : String, title : String)

object Navigation {

  var data : Option[Map[String, Navigation]] = None

  def update : Map[String, Navigation] = {

    println("updating")

    // Get all directories in resources/photos folder
    val albums : List[Album] = Album.getAll

    // get album order
    val albums_sorted : List[Album] = albums.sortBy(_.datetime._2.getMillis)

    // Build list of Navigation elements
    val nav : Map[String, Navigation] = scan(albums_sorted, None)

    // Update data and return
    data = Some(nav)
    nav
  }


  // Returns Navigation from cache if available and exists
  def get(albumURL : String, galleryURL : Option[String] = None) : Navigation = data match {
    case None       => update.getOrElse(albumURL, Navigation(None, None, None))
    case Some(d)    => d.getOrElse(albumURL, Navigation(None, None, None))
  }


  // Now for each album find the two neighbors. We build map tail recursively
  private def scan(albums : List[Album], 
           prev : Option[Album]) : Map[String, Navigation] = albums match {

    // In case we have two or more items left
    case current::next::rest =>
      scan(next::rest, Some(current)) + getNavPair(prev, current, Some(next))

    // In case the current item is the last
    case current::Nil => 
      Map(getNavPair(prev, current, None))
  }


  // Constructs a pair of String -> Navigation based on elements
  private def getNavPair(prev : Option[Album], current : Album, next : Option[Album]) : (String, Navigation) = {
    val navPrev = for (p <- prev) yield {
      val gURL : String = { for (g <- p.getGallery) yield Gallery.url(g) } getOrElse("")
      NavElem(gURL + "/" + p.url, p.title)
    }
    val navNext = for (n <- next) yield {
      val gURL : String = { for (g <- n.getGallery) yield Gallery.url(g) } getOrElse("")
      NavElem(gURL + "/" + n.url, n.title)
    }
    val navGal = for (g <- current.getGallery) yield {
      NavElem(Gallery.url(g), g)
    }
    (current.url -> Navigation(navNext, navPrev, navGal))
  }
}
