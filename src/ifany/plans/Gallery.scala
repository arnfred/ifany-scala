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
	//                 Update                   //
	//                                          //
	//////////////////////////////////////////////
    case req @ Path(Seg(albumURL :: "update" :: Nil)) => {

      // Just update
      AlbumModel.update(albumURL)

      // Respond, just so we know the server hasn't crashed
      req.respond(HtmlContent ~> ResponseString("<body><p>Updating ... </p></body>"))
    }

    case req @ Path(Seg("update" :: Nil)) => {

      // Just update
      FrontpageModel.update

      // Respond, just so we know the server hasn't crashed
      req.respond(HtmlContent ~> ResponseString("<body><p>Updating ... </p></body>"))
    }

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
      
      // Get frontpage model
      val album_F = AlbumModel.get(albumURL)

      // In case we succeed
      album_F onSuccess {
        case model => {
          val album = getAlbum(model)
          req.respond(HtmlContent ~> ResponseString(album))
        }
      }

      // In case we fail
      album_F onFailure {
        case error => {
          req.respond(HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
    }



  }



  // get the Album view
  def getAlbum(model : AlbumModel) : String = {
      val view = AlbumView(model)
      AlbumTemplate(view).toString
  }


  // Get the frontpage view
  def getFrontpage(model : FrontpageModel) : String = {
    val view = FrontpageView(model)
    FrontpageTemplate(view).toString
  }
}

