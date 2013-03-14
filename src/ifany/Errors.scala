package ifany

case class InternalError(msg : String) extends Exception
case class AlbumNotFound(url : String) extends Exception
