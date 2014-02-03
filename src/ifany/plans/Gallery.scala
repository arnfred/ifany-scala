package ifany

import dispatch._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import net.liftweb.json.JsonDSL._
import scala.concurrent._
import ExecutionContext.Implicits.global

object GalleryPlan extends async.Plan with ServerErrorResponse {

  def intent = {

	//////////////////////////////////////////////
	//                                          //
	//                Frontpage                 //
	//                                          //
	//////////////////////////////////////////////
    case req @ Path(Seg(Nil)) => {

      // Get frontpage model
      val frontpage_F = FrontpageModel.get

      // In case we succeed
      frontpage_F onSuccess {
        case model => {
          val frontpage = getFrontpage(model)
          req.respond(Ok ~> HtmlContent ~> ResponseString(frontpage))
        }
      }

      // In case we fail
      frontpage_F onFailure {
        case InternalError(msg) => {
          println("* INTERNAL ERROR * : " + msg)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("An error occured"))
        }
        case AlbumNotFound(url) => {
          println("* ALBUM NOT FOUND * : " + url)
          req.respond(NotFound ~> HtmlContent ~> ResponseString("Album not found: " + url))
        }
        case error => {
          println("* UNKNOWN ERROR * : " + error.toString)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
    }


	//////////////////////////////////////////////
	//                                          //
	//                  Album                   //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg(albumURL :: whatever)) => {

      try {
        val model = AlbumModel(albumURL)
        val view = AlbumView(model)
        val output = AlbumTemplate(view).toString
        req.respond(HtmlContent ~> ResponseString(output))

      } catch {

        case InternalError(msg) => {
          println("* INTERNAL ERROR * : " + msg)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("An error occured"))
        }
        case AlbumNotFound(url) => {
          println("* ALBUM NOT FOUND * : " + url)
          req.respond(NotFound ~> HtmlContent ~> ResponseString("Album not found: " + url))
        }
        case error : Throwable => {
          println("* UNKNOWN ERROR * : " + error.toString)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
    }



  }




  // Get the frontpage view
  def getFrontpage(model : FrontpageModel) : String = {
    val view = FrontpageView(model)
    FrontpageTemplate(view).toString
  }
}

