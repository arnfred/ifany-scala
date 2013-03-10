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
    //case req @ Path(Seg(albumURL :: Nil)) => serveAlbum(albumURL) map { page =>
    //  req.respond(page)
    //}



	//////////////////////////////////////////////
	//                                          //
	//              Album - Image               //
	//                                          //
	//////////////////////////////////////////////
    // case req @ Path(Seg(albumURL :: image :: Nil)) => serveAlbum(albumURL) map { page =>
    //   req.respond(page)
    // }
  }


  // Serve an album
  // def serveAlbum(url : String) = {
  //   val album : Promise[Option[String]] = try {

  //     Gallery.fetch.flatMap { g =>
  //       g.album(url) match {
  //         case None     => dispatch.Http.promise(None)
  //         case Some(a)  => {
  //           for (ai <- a.getImages; ae <- ai.getExif) yield Some(getAlbum(ae, g))
  //         }
  //       }
  //     }
  //   } 

  //   catch {
  //     case e => { println("Out of memory error"); dispatch.Http.promise(None) }
  //   }

  //   album map {
  //     case None       => Pass
  //     case Some(a)    => HtmlContent ~> ResponseString(a)
  //   }
  // }


  // // get the Album view
  // def getAlbum(a : Album, g : Gallery) : String = {
  //     val view = AlbumView(a, g)
  //     AlbumTemplate(view).toString
  // }


  // Get the frontpage view
  def getFrontpage(model : FrontpageModel) : String = {
    val view = FrontpageView(model)
    FrontpageTemplate(view).toString
  }
}

