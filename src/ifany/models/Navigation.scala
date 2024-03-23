package ifany

import scala.io.Source
import java.io.FileNotFoundException
import java.io.File

case class Navigation(next : Option[NavElem], 
                      prev : Option[NavElem], 
                      gallery : Option[NavElem])

case class NavElem(url : String, title : String)

object Navigation {

  var albumData : Option[Map[String, Navigation]] = None
  var galleryData : Option[Map[String, Navigation]] = None

  def update : Map[String, Navigation] = {

    val galleries : Seq[Gallery] = Gallery.getAllGalleries
    val albums : Seq[Album] = for (g <- galleries; a <- g.albums) yield a

    // get album order
    val albums_sorted : Seq[Album] = albums.sortBy(_.datetime._2)

    // Build list of Navigation elements
    val navGalleries : Map[String, Navigation] = scan(galleries.reverse, None, galleryNav)
    val navAlbums : Map[String, Navigation] = scan(albums_sorted, None, albumNav)

    // Update data and return
    albumData = Some(navAlbums)
    galleryData = Some(navGalleries)
    navAlbums ++ navGalleries
  }

  // Returns Navigation from cache if available and exists
  def getGallery(galleryURL : String) : Navigation = galleryData match {
    case None       => update.getOrElse(galleryURL, Navigation(None, None, None))
    case Some(d)    => d.getOrElse(galleryURL, Navigation(None, None, None))
  }


  // Returns Navigation from cache if available and exists
  def getAlbum(albumURL : String) : Navigation = albumData match {
    case None       => update.getOrElse(albumURL, Navigation(None, None, None))
    case Some(d)    => d.getOrElse(albumURL, Navigation(None, None, None))
  }

  // Now for each album find the two neighbors. We build map tail recursively
  private def scan[A](galleries : Seq[A], 
                      prev : Option[A],
                      getNavPair : (Option[A], A, Option[A]) => (String, Navigation))
                        : Map[String, Navigation] = galleries match {

    // In case we have two or more items left
    case current +: next +: rest => {
      scan(next +:rest, Some(current), getNavPair) + getNavPair(prev, current, Some(next))
    }

    // In case the current item is the last
    case current +: Nil => 
      Map(getNavPair(prev, current, None))
  }

  // Constructs a pair of String -> Navigation based on elements
  private def galleryNav(prev : Option[Gallery], current : Gallery, next : Option[Gallery]) : (String, Navigation) = {
    val navPrev = for (p <- prev) yield {
      NavElem(p.url, p.name)
    }
    val navNext = for (n <- next) yield {
      NavElem(n.url, n.name)
    }
    val navGal = Some(NavElem("/", "Home"))

    (current.url -> Navigation(navNext, navPrev, navGal))
  }

  // Constructs a pair of String -> Navigation based on elements
  private def albumNav(prev : Option[Album], current : Album, next : Option[Album]) : (String, Navigation) = {
    val navPrev = for (p <- prev) yield {
      val gURL : String = { for (g <- p.getGallery) yield Gallery.get(g).url } getOrElse("album")
      NavElem(s"$gURL/${p.url}", p.title)
    }
    val navNext = for (n <- next) yield {
      val gURL : String = { for (g <- n.getGallery) yield Gallery.get(g).url } getOrElse("album")
      NavElem(s"$gURL/${n.url}", n.title)
    }
    val navGal = for (g <- current.getGallery) yield {
      NavElem(Gallery.get(g).url, g)
    }
    (current.url -> Navigation(navNext, navPrev, navGal))
  }
}
