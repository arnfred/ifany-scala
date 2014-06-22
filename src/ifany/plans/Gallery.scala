package ifany

import dispatch._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.io.BufferedInputStream
import java.io.FileInputStream
import scala.Stream

object GalleryPlan extends async.Plan with ServerErrorResponse {

  def intent = {

	//////////////////////////////////////////////
	//                                          //
	//                Frontpage                 //
	//                                          //
	//////////////////////////////////////////////
    case req @ Path(Seg(Nil)) => {

      try {
        val frontpage : Frontpage = Frontpage.get()
        val view = FrontpageView(frontpage)
        val output = FrontpageTemplate(view).toString
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


	//////////////////////////////////////////////
	//                                          //
	//                  Update                  //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("update" :: Nil)) => {
        
      try {
        val frontpage : Frontpage = Frontpage.update()
        val nav : Map[String, Navigation] = Navigation.update
        val view = FrontpageView(frontpage)
        val output = FrontpageTemplate(view).toString
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


	//////////////////////////////////////////////
	//                                          //
	//                  cover                   //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("cover" :: str :: size :: Nil)) => {
        
      try {
        val frontpage : Frontpage = Frontpage.get()
        val covers : List[Cover] = frontpage.covers
        val n : Int = str.map(_+0).reduce({ (a,b) => (a + 1000003 * (b + 1)) % covers.length }) % covers.length
        val img : Image = covers(n).image
        val album : Album = covers(n).album
        val path : String = "resources" + img.url(size, album.url)
        val bis = new BufferedInputStream(new FileInputStream(path))
        val data = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
        req.respond(Ok ~> ContentType("image/jpg") ~> 
                          ContentLength(data.length.toString) ~>
                          ResponseBytes(data))
      } catch {
        case InternalError(msg) => {
          println("* INTERNAL ERROR * : " + msg)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString(msg))
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

	//////////////////////////////////////////////
	//                                          //
	//                 Gallery                  //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg(galleryURL :: Nil)) => {

      // Piece together the album data
      try {
        val gallery = Gallery.get(galleryURL)
        val nav : Navigation = Navigation.getGallery(galleryURL)
        val view = GalleryView(gallery, nav)
        val output = GalleryTemplate(view).toString
        req.respond(HtmlContent ~> ResponseString(output))

      // Respond to errors that might occur
      } catch {
        case InternalError(msg) => {
          println("* INTERNAL ERROR * : " + msg)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("An error occured"))
        }
        case GalleryNotFound(url) => {
          println("* GALLERY NOT FOUND * : " + url)
          req.respond(NotFound ~> HtmlContent ~> ResponseString("Gallery not found: " + url))
        }
        case error : Throwable => {
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

    case req @ Path(Seg(galleryURL :: albumURL :: whatever)) => {

      // Piece together the album data
      try {
        val album : Album = Album.get(albumURL)
        val nav : Navigation = Navigation.getAlbum(albumURL)
        val view = AlbumView(album, nav)
        val output = AlbumTemplate(view).toString
        req.respond(HtmlContent ~> ResponseString(output))

      // Respond to errors that might occur
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
}

