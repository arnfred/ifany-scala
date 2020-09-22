package ifany

import unfiltered.response._
import awscala._, s3._
import scala.concurrent._

object S3Photo {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val s3 = S3.at(Region.EU_WEST_1)
  val bucket = Bucket(sys.env("IMAGES_BUCKET"))

  def stream(album: String, filename: String): Future[ResponseStreamer] = {
    val key = s"albums/$album/$filename"
    Future(bucket.get(key)).map { s3Response => 
      s3Response match {
        case None => throw new InternalError(s"S3 key not found: $key")
        case Some(photo) => new ResponseStreamer {
          override def stream(os: java.io.OutputStream): Unit = {
            try { photo.content.transferTo(os) } finally { photo.content.close() }
          }
        }
      }
    }
  }
}


