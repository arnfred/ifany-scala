package ifany

import unfiltered.response._
import awscala._, s3._
import scala.concurrent._

object S3Photo {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val s3 = S3.at(Region.EU_WEST_1)
  val bucket = Bucket(sys.env("IMAGES_BUCKET"))

  def generatePresignedUrls(album: String): Map[String, String] = {
    val albumKey = s"albums/$album/"
    val expiration = DateTime.now.plusMinutes(10).toDate
    // Why no `s3.generatePresignedUrl(bucketName, key, expiration)` ???
    // The presigned links are formatted as
    // `https://<bucket>.s3.<region>.amazonaws.com/<key>?...` which confuses
    // ssl certificates because the domain of the bucket doesn't match the
    // certicate given by amazon. Amazon's advice is to change the name of the
    // bucket to avoid using dots...
    //
    // However, it turns out that concatenating the bucket name with the key
    // generates valid urls with no ssl issues. I came across this idea while
    // noticing the stack overflow answer here:
    // https://stackoverflow.com/a/53617069/1722504
    s3.keys(bucket, albumKey)
      .map(key => (key, s3.generatePresignedUrl("", s"${bucket.name}/$key", expiration).toURI.toString))
      .toMap
  }

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


