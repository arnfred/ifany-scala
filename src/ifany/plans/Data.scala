package ifany

import dispatch._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import net.liftweb.json._
import scala.concurrent._
import ExecutionContext.Implicits.global

object DataPlan extends async.Plan with ServerErrorResponse {

  def intent = {

    case req @ Path(Seg("album" :: id :: Nil)) => {

      // get images
      val images_F = future { Image.getQuery[Image](Map("albumID" -> id)) }

      // In case we succeed
      images_F onSuccess {
        case images => {
          req.respond(JsonContent ~> ResponseString {
            toJSON((images.map { i => i.id -> i }) toMap)
          })
        }
      }

      // In case we fail
      images_F onFailure {
        case error => {
          req.respond(HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
      
      images_F
    }
  }

  def toJSON(obj : AnyRef) : String = {
    implicit val formats = DefaultFormats
    Serialization.write(obj)
  }
}
