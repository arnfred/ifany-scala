package ifany

import scala.util.Random.shuffle

case class Gallery(name : String, description : String, albums : List[Album]) {

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

  val url : String = Gallery.url(name)
}

object Gallery {

  def get(galleryURL : String) : Gallery = {
    try {
      val frontpage : Frontpage = Frontpage.get()
      frontpage.galleries.find(g => g.url == galleryURL).get
    } catch {
      case (error : java.util.NoSuchElementException) => throw GalleryNotFound(galleryURL)
    }
  }

  def getOption(galleryURL : String) : Option[Gallery] = try {
    Some(get(galleryURL))
  } catch {
    case GalleryNotFound(url) => None
  }

  def url(name : String) : String = name.replace(" ", "-").toLowerCase
}
