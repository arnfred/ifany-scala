package ifany

case class InternalError(val message : String) extends Exception
case class AlbumNotFound(url : String) extends Exception {
  override def toString : String = "Album not found: " + url
}
case class GalleryNotFound(url : String) extends Exception {
  override def toString : String = "Gallery not found: " + url
}
