package ifany

import scala.util.Random.shuffle

case class Gallery(name : String, url : Option[String], description : String, albums : List[Album]) {

  def getCover : Cover = {
    val covers = for (a <- albums; i <- a.images if i.cover) yield {
      Cover(i, a.title, a.url)
    }
    if (covers.size > 0) shuffle(covers).head

    // If we have no covers, just use any image in landscape format (width > height)
    else {
      val landscapes = {
        for (a <- albums; i <- a.images if i.size(0) > i.size( 1 )) yield {
          Cover(i, a.title, a.url)
        }
      }
      shuffle(landscapes).head
    }
  }
}

object Gallery {

  def get(galleryURL : String) : Gallery = {
    try {
      val frontpage : Frontpage = Frontpage.get()
      frontpage.galleries.find(g => g.url == Some(galleryURL)).get
    } catch {
      case (error : java.util.NoSuchElementException) => throw GalleryNotFound(galleryURL)
    }
  }

  def url(name : String) : Option[String] = Some(name.replace(" ", "-").toLowerCase)
}
