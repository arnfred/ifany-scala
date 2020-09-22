package ifany

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.io.BufferedInputStream
import java.io.FileInputStream
import scala.Stream
import scala.util.Random
import scala.util.{Success, Failure}
import awscala._, s3._

@io.netty.channel.ChannelHandler.Sharable
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

        case error @ InternalError(msg) => {
          println("* INTERNAL ERROR * : " + msg)
          error.printStackTrace()
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("An error occured"))
        }
        case error @ AlbumNotFound(url) => {
          println("* ALBUM NOT FOUND * : " + url)
          error.printStackTrace()
          req.respond(NotFound ~> HtmlContent ~> ResponseString("Album not found: " + url))
        }
        case error: java.io.IOException => {
          println("IOException thrown for frontpage (presumably due to rapid reload")
        }
        case error : Throwable => {
          println("* UNKNOWN ERROR * : " + error.toString)
          error.printStackTrace()
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
          error.printStackTrace()
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
        val covers : Seq[Cover] = frontpage.covers
        val n : Int = str.map(_+0).reduce({ (a,b) => (a + 1000003 * (b + 1)) % covers.length }) % covers.length
        val img : Image = covers(n).image
        val album : Album = covers(n).album
        val path : String = img.url(size, album.url)
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
	//                  covers                  //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("covers" :: _)) => {

      try {
        val frontpage : Frontpage = Frontpage.get()
        val images : Seq[Image] = Random.shuffle(frontpage.covers.map(_.makeImage)).toSeq
        val title : String = "Cover Images"
        val desc : String = """For each album I take I note the photos that I particularly like and add them to the list of covers. These images are used for the cover image on <a href="/">the frontpage</a>. They are also my usual go to images when I want new prints on my walls."""
        val album : Album = Album(title, desc, "", Seq(), None, images, Album.datetimeFromImages(images, "covers"))
        val nav : Navigation = Navigation(None, None, None)
        val view = AlbumView(album, nav, "metaAlbum")
        val output = MetaAlbumTemplate(view).toString
        req.respond(HtmlContent ~> ResponseString(output))
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
          error.printStackTrace()
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
    }

	//////////////////////////////////////////////
	//                                          //
	//                photos                    //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("photos" :: album :: filename :: Nil)) => {
      S3Photo.stream(album, filename).onComplete {
        case Success(response) => req.respond(response)
        case Failure(InternalError(msg)) => {
          println("* INTERNAL ERROR * : " + msg)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString(msg))
        }
        case Failure(AlbumNotFound(url)) => {
          println("* ALBUM NOT FOUND * : " + url)
          req.respond(NotFound ~> HtmlContent ~> ResponseString("Album not found: " + url))
        }
        case Failure(error: java.net.SocketException) => {
          println(s"Connection Reset while sending photo $album/$filename")
        }
        case Failure(error: java.io.IOException) => {
          println(s"Broken pipe while sending photo $album/$filename")
        }
        case Failure(error : Throwable) => {
          println("* UNKNOWN ERROR * : " + error.toString)
          error.printStackTrace()
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
    }

	//////////////////////////////////////////////
	//                                          //
	//                  all                     //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("all" :: _)) => {

      try {
        val frontpage : Frontpage = Frontpage.get()
        val images = for {
          gallery <- frontpage.galleries
          album <- gallery.albums
          image <- album.images
        } yield image.copy(file = album.url + "/" + image.file)
        val title : String = "All Images"
        val desc : String = """A long list of all the images published on <a href="/">ifany.org</a> in rough chronological order according to the image metadata."""
        val album : Album = Album(title, desc, "", Seq(), None, images.sortBy(_.datetime).toSeq, Album.datetimeFromImages(images, "all"))
        val nav : Navigation = Navigation(None, None, None)
        val view = AlbumView(album, nav, "metaAlbum")
        val output = MetaAlbumTemplate(view).toString
        req.respond(HtmlContent ~> ResponseString(output))
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
          error.printStackTrace()
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
    }

	//////////////////////////////////////////////
	//                                          //
	//                random                    //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("random" :: _)) => {

      try {
        val frontpage : Frontpage = Frontpage.get()
        val images = for {
          gallery <- frontpage.galleries
          album <- gallery.albums
          image <- album.images
        } yield image.copy(file = album.url + "/" + image.file)
        val title : String = "All Images"
        val desc : String = """Every single image on <a href="/">ifany.org</a> in random order (in fact they'll be re-randomised every time you reload). I was browsing through random images the other day and thought it would be neat with a way to scroll through random moments and memories from my past. I suspect this will mostly be useful for my own nostalgic cravings, but still... here you go."""
        val album : Album = Album(title, desc, "", Seq(), None, Random.shuffle(images).toSeq, Album.datetimeFromImages(images, "random"))
        val nav : Navigation = Navigation(None, None, None)
        val view = AlbumView(album, nav, "metaAlbum")
        val output = MetaAlbumTemplate(view).toString
        req.respond(HtmlContent ~> ResponseString(output))
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
          error.printStackTrace()
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
          error.printStackTrace()
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
          error.printStackTrace()
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("Error occured: " + error.toString))
        }
      }
    }
  }
}

