package ifany

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.io.BufferedInputStream
import java.io.FileInputStream
import scala.util.Random
import scala.util.{Success, Failure}
import awscala.*, s3.*
import java.time.LocalDateTime

@io.netty.channel.ChannelHandler.Sharable
object GalleryPlan extends async.Plan with ServerErrorResponse {

  private val aiCrawlers = Seq(
    "GPTBot", "ChatGPT-User", "CCBot", "Google-Extended",
    "anthropic-ai", "ClaudeBot", "Bytespider", "FacebookBot",
    "cohere-ai", "Diffbot", "ImagesiftBot", "Omgili", "PerplexityBot"
  )

  private def isAiCrawler(userAgent: String): Boolean = {
    val ua = userAgent.toLowerCase
    aiCrawlers.exists(bot => ua.contains(bot.toLowerCase))
  }

  private val authCutoff = LocalDateTime.of(2020, 11, 25, 0, 0)

  private def isProtectedAlbum(album: Album): Boolean =
    album.datetime._2.isAfter(authCutoff) || album.datetime._2.isEqual(authCutoff)

  def intent = {

    case req if req.headers("User-Agent").exists(isAiCrawler) =>
      req.respond(Forbidden ~> ResponseString("Forbidden"))


	//////////////////////////////////////////////
	//                                          //
	//                  Auth                    //
	//                                          //
	//////////////////////////////////////////////

    // Login page at /login
    case req @ Path(Seg("login" :: Nil)) => {
      val session = SessionCookie.fromRequest(req)
      session match {
        case Some(_) => req.respond(Redirect("/"))
        case None =>
          val output = LoginRequiredTemplate("/")
          req.respond(HtmlContent ~> ResponseString(output))
      }
    }

    // Login: redirect to Kinde with return URL
    case req @ Path(Seg("auth" :: "login" :: Nil)) => {
      val returnTo = req.parameterNames
        .find(_ == "return_to")
        .flatMap(n => req.parameterValues(n).headOption)
        .getOrElse("/")
      val url = KindeClient.authorizationUrl(returnTo)
      req.respond(Redirect(url))
    }

    // Callback: exchange code for tokens, set session cookie
    case req @ Path(Seg("auth" :: "callback" :: Nil)) => {
      val code = req.parameterValues("code").headOption
      val state = req.parameterValues("state").headOption.getOrElse("")
      val returnTo = KindeClient.returnToFromState(state)

      code.flatMap(KindeClient.exchangeCode) match {
        case Some(session) =>
          val cookie = SessionCookie.setCookieHeader(session)
          req.respond(
            Found ~> Location(returnTo) ~> ResponseHeader("Set-Cookie", List(cookie))
          )
        case None =>
          req.respond(
            Redirect(s"/auth/login?return_to=${java.net.URLEncoder.encode(returnTo, "UTF-8")}")
          )
      }
    }

    // Logout: clear cookie and redirect to Kinde logout
    case req @ Path(Seg("auth" :: "logout" :: Nil)) => {
      req.respond(
        Found ~> Location(KindeClient.logoutUrl) ~> ResponseHeader("Set-Cookie", List(SessionCookie.clearCookieHeader))
      )
    }

	//////////////////////////////////////////////
	//                                          //
	//                Frontpage                 //
	//                                          //
	//////////////////////////////////////////////
    case req @ Path(Seg(Nil)) => {

      try {
        val session = SessionCookie.fromRequest(req)
        val frontpage : Frontpage = Frontpage.get()
        val view = FrontpageView(frontpage)
        val output = FrontpageTemplate(view, session)
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
        val session = SessionCookie.fromRequest(req)
        if (!session.exists(_.isAdmin)) {
          val output = LoginRequiredTemplate("/update")
          req.respond(HtmlContent ~> ResponseString(output))
        } else {
        val frontpage : Frontpage = Frontpage.update()
        val nav : Map[String, Navigation] = Navigation.update
        val view = FrontpageView(frontpage)
        val output = FrontpageTemplate(view, session)
        req.respond(HtmlContent ~> ResponseString(output))
        }

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
	//                  covers                  //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("covers" :: _)) => {

      try {
        val session = SessionCookie.fromRequest(req)
        val frontpage : Frontpage = Frontpage.get()
        val images : Seq[Image] = Random.shuffle(frontpage.covers.map(_.makeImage)).take(100).toSeq
        val title : String = "Cover Images"
        val desc : String = """For each album I take I note the photos that I particularly like and add them to the list of covers. These images are used for the cover image on <a href="/">the frontpage</a>. They are also my usual go to images when I want new prints on my walls. In this album I'm showing 100 randomly shuffled cover images. Reload the page to see a different selection."""
        val album : Album = Album.dynamic(title, desc, images, Album.datetimeFromImages(images, "covers"))
        val nav : Navigation = Navigation(None, None, None)
        val view = AlbumView(album, nav, "metaAlbum")
        val output = AlbumTemplate(view, session)
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
    //                  all                     //
    //                                          //
    //////////////////////////////////////////////

    case req @ Path(Seg("all" :: pageStr :: _)) => {

      try {
        val session = SessionCookie.fromRequest(req)
        val frontpage : Frontpage = Frontpage.get()
        val images = for {
          gallery <- frontpage.galleries
          album <- gallery.albums
          image <- album.images
        } yield image.copy(file = album.url + "/" + image.file)
        val page = pageStr.toInt
        val index = page - 1
        val imageSets = images.sortBy(_.datetime).grouped(100).toSeq
        val pages = imageSets.length
        val title : String = "All Images"
        val desc : String = s"""Images and videos published on <a href="/">ifany.org</a> in rough chronological order according to the image metadata. Page $page of $pages"""
        val date = Album.datetimeFromImages(images, "all")
        val album : Album = Album.dynamic(title, desc, imageSets(index).toSeq.reverse, date)
        val next = if (page < pages) Some(NavElem(s"all/${page + 1}", s"Page ${page + 1}")) else None
        val prev = if (page > 1) Some(NavElem(s"all/${page - 1}", s"Page ${page - 1}")) else None
        val nav : Navigation = Navigation(next, prev, None)
        val view = AlbumView(album, nav, "metaAlbum")
        val output = AlbumTemplate(view, session)
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
	//                videos                    //
	//                                          //
	//////////////////////////////////////////////

    case req @ Path(Seg("videos" :: _)) => {

      try {
        val session = SessionCookie.fromRequest(req)
        val frontpage : Frontpage = Frontpage.get()
        val videos = for {
          gallery <- frontpage.galleries
          album <- gallery.albums
          image <- album.images if image.is_video
        } yield image.copy(file = album.url + "/" + image.file)
        val title : String = "All videos"
        val desc : String = """All videos gathered on one page."""
        val album : Album = Album.dynamic(title, desc, videos.sortBy(_.datetime).toSeq, Album.datetimeFromImages(videos, "videos"))
        val nav : Navigation = Navigation(None, None, None)
        val view = AlbumView(album, nav, "metaAlbum")
        val output = AlbumTemplate(view, session)
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
        val session = SessionCookie.fromRequest(req)
        val frontpage : Frontpage = Frontpage.get()
        val images = for {
          gallery <- frontpage.galleries
          album <- gallery.albums
          image <- album.images
        } yield image.copy(file = album.url + "/" + image.file)
        val title : String = "100 Random Images"
        val desc : String = """A random pick of 100 images from <a href="/">ifany.org</a> (reload to reshuffle). I was browsing through random images the other day and thought it would be neat with a way to scroll through random moments and memories from my past. I suspect this will mostly be useful for my own nostalgic cravings, but still... here you go."""
        val album : Album = Album.dynamic(title, desc, Random.shuffle(images).take(100).toSeq, Album.datetimeFromImages(images, "random"))
        val nav : Navigation = Navigation(None, None, None)
        val view = AlbumView(album, nav, "metaAlbum")
        val output = AlbumTemplate(view, session)
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
        val session = SessionCookie.fromRequest(req)
        val gallery = Gallery.get(galleryURL)
        val nav : Navigation = Navigation.getGallery(galleryURL)
        val view = GalleryView(gallery, nav)
        val output = GalleryTemplate(view, session)
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

    case req @ Path(Seg(galleryURL :: albumURL :: rest)) => {

      println(s"[DEBUG] Album route matched: uri='${req.uri}' gallery='$galleryURL' album='$albumURL' rest='$rest'")

      // Piece together the album data
      try {
        val password = rest.headOption
        val album : Album = Album.get(albumURL, password)

        // Auth check for albums published on or after Nov 25, 2020
        if (isProtectedAlbum(album)) {
          val session = SessionCookie.fromRequest(req)
          val albumPath = s"/$galleryURL/$albumURL/"
          session match {
            case None =>
              val output = LoginRequiredTemplate(albumPath)
              req.respond(HtmlContent ~> ResponseString(output))
            case Some(s) if !s.isTrusted =>
              val output = PendingApprovalTemplate(s.email)
              req.respond(HtmlContent ~> ResponseString(output))
            case Some(s) => // authorized, render album
              val nav : Navigation = Navigation.getAlbum(albumURL)
              val view = AlbumView(album, nav)
              val output = AlbumTemplate(view, session)
              req.respond(HtmlContent ~> ResponseString(output))
          }
        } else {
          val session = SessionCookie.fromRequest(req)
          val nav : Navigation = Navigation.getAlbum(albumURL)
          val view = AlbumView(album, nav)
          val output = AlbumTemplate(view, session)
          req.respond(HtmlContent ~> ResponseString(output))
        }

      // Respond to errors that might occur
      } catch {

        case InternalError(msg) => {
          println("* INTERNAL ERROR * : " + msg)
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString("An error occured"))
        }
        case AlbumNotFound(url) => {
          println("* ALBUM NOT FOUND * : " + url)
          req.respond(NotFound ~> HtmlContent ~> ResponseString(
            s"Album not found: $url | uri: ${req.uri} | gallery: $galleryURL | album: $albumURL | rest: $rest"
          ))
        }
        case error : Throwable => {
          println("* UNKNOWN ERROR * : " + error.toString)
          error.printStackTrace()
          req.respond(InternalServerError ~> HtmlContent ~> ResponseString(
            s"Error: ${error.toString} | uri: ${req.uri} | gallery: $galleryURL | album: $albumURL"
          ))
        }
      }
    }
  }
}
