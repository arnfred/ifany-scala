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
          req.respond(HtmlContent ~> ResponseString(frontpage))
        }
      }

      // In case we fail
      frontpage_F onFailure {
        case error => {
          req.respond(HtmlContent ~> ResponseString("Error occured: " + error.toString))
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

