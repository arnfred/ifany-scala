package ifany

import dispatch._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import net.liftweb.json.JsonDSL._

object SmugmugPlan extends async.Plan with ServerErrorResponse {

  def intent = {


	//////////////////////////////////////////////
	//                                          //
	//                Frontpage                 //
	//                                          //
	//////////////////////////////////////////////
    case req @ Path(Seg(Nil)) => {
      val gallery = Gallery.fetch
      val frontpageImages = Image.getAll(id = "12121179", key = "C3Ks6")

      try {

        // Map the promises
        val frontpage = for (f <- frontpageImages;
                             g <- gallery;
                             gi <- g.getImages) yield getFrontpage(f, gi)

      } catch {
        case e => { println("Out of memory error"); dispatch.Http.promise("Error") }
      } 

      // Respond to request
      frontpage.map { f => req.respond(HtmlContent ~> ResponseString(f)) }
    }


	//////////////////////////////////////////////
	//                                          //
	//                  Album                   //
	//                                          //
	//////////////////////////////////////////////
    case req @ Path(Seg(albumURL :: Nil)) => serveAlbum(albumURL) map { page =>
      req.respond(page)
    }



	//////////////////////////////////////////////
	//                                          //
	//              Album - Image               //
	//                                          //
	//////////////////////////////////////////////
    case req @ Path(Seg(albumURL :: image :: Nil)) => serveAlbum(albumURL) map { page =>
      req.respond(page)
    }
  }


  // Serve an album
  def serveAlbum(url : String) = {
    val album : Promise[Option[String]] = {

      try {

        Gallery.fetch.flatMap { g =>
          g.album(url) match {
            case None     => dispatch.Http.promise(None)
            case Some(a)  => {
              for (ai <- a.getImages; ae <- ai.getExif) yield Some(getAlbum(ae, g))
            }
          }
        }
      } 

      catch {
        case e => { println("Out of memory error"); dispatch.Http.promise(None) }
      }
    }

    album map {
      case None       => Pass
      case Some(a)    => HtmlContent ~> ResponseString(a)
    }
  }


  // get the Album view
  def getAlbum(a : Album, g : Gallery) : String = {
      val view = AlbumView(a, g)
      AlbumTemplate(view).toString
  }


  // Get the frontpage view
  def getFrontpage(f : List[Image], g : Gallery) : String = {
    val view = FrontpageView(f, g)
    FrontpageTemplate(view).toString
  }
}

