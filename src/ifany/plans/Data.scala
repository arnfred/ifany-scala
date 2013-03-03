package ifany

import dispatch._
import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import net.liftweb.json._

object DataPlan extends async.Plan with ServerErrorResponse {

  def intent = {

    case req @ Path(Seg("data" :: "thumbnails.js" :: Nil)) => {

      // Map the promises
      val gallery = for (g <- Gallery.fetch;
                         gi <- g.getImages) yield {
        val thumbnails = (for (a <- gi.albums) yield (a.id -> a.thumbnails)).toMap
        toJSON(thumbnails)
      }

      // The response wrapped as an async js module
      def response(g : String)  : String = "define(function() { return " + g + " });"

      // Respond in an asynchronious manner
      gallery.map { (g : String) => 
        req.respond(JsonContent ~> ResponseString(response(g))) 
      }
    }


    case req @ Path(Seg("album" :: id :: key :: Nil)) => {

      // Map the promises
      val album : Promise[Map[String, Image]] = for (images <- Image.getAll(id,key)) yield {
          images.map { i => i.id -> i }
      }.toMap

      album.map { (a : Map[String, Image]) =>
        req.respond(JsonContent ~> ResponseString(toJSON(a))) 
      }
    }
  }

  def toJSON(obj : AnyRef) : String = {
    implicit val formats = DefaultFormats
    Serialization.write(obj)
  }
}
