package ifany

import unfiltered.response._
import awscala._, s3._

case class S3Photo(album: String, filename: String) extends ResponseStreamer {
  def stream(os: java.io.OutputStream): Unit = {
    val key = s"albums/$album/$filename"
    val photo = S3Photo.get(key) match {
      case Some(obj) => obj.content
      case None => throw new InternalError(s"S3 key not found: $key")
    }
    try { photo.transferTo(os) } finally { photo.close() }
  }
}

object S3Photo {
  implicit val s3 = S3.at(Region.EU_WEST_1)
  val bucket = Bucket(sys.env("IMAGES_BUCKET"))
  def get(key: String) = bucket.get(key)
}


