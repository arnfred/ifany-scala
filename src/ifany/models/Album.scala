package ifany

import dispatch._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.shuffle

case class AlbumModel(exifs : List[EXIF],
                      images : List[Image],
                      album : Album)

object AlbumModel {

  def update(url : String) : Unit = {

    for (albums <- SmugmugAPI.getAlbums; 
         a <- albums if (a.url == url)) yield {

      println(a)

      for (images <- SmugmugAPI.getImages(a.id, a.key)) yield {

        val exifs_P = for (i <- images) yield SmugmugAPI.getEXIF(i.id, i.key)

        for (exifs <- Http.promise.all(exifs_P)) yield {

          val exifMap = images.map(_.id).zip(exifs).toMap
          save(images, exifMap, a)
        }
      }
    }
  }


  def get(url : String) : Future[AlbumModel] = {
    
    // Get album
    val albums_F = future { Album.getQuery[Album](Map("url" -> url)) }

    val model_F = for (albums <- albums_F) yield {

      // If we don't have an album of this name, throw an error
      val album = albums match {
        case Nil    => throw new Exception("No album with the url: " + url)
        case list   => list.head
      }

      // Fetch images and exif
      val exifs_F = future { EXIF.getList[EXIF](album.imageIDs) }
      val images_F = future { Image.getList[Image](album.imageIDs) }

      for (exifs <- exifs_F; images <- images_F) yield {

        AlbumModel(exifs.toList, images, album)
      }
    }

    // Flatten the double future to one
    model_F.flatMap(f => f)
  }


  def save(images : List[Image], exifMap : Map[String, EXIF], album : Album) : Unit = {

    val albumID = album.id

    def addAlbumIDtoImage(im : Image) : Image = {
      Image(im.id, im.key, albumID, im.caption, im.url, im.size)
    }
      
    def addAlbumIDtoEXIF(exif : EXIF) : EXIF = {
      EXIF(exif.id, exif.key, albumID, exif.aperture, exif.focalLength, exif.iso, exif.model, exif.dateTime)
    }

    println("saving images")
    for (i <- images) {
      Image.putItem(i.id, addAlbumIDtoImage(i))
    }

    println("saving exifs")
    for ((id, exif) <- exifMap) {
      val e = addAlbumIDtoEXIF(exif)
      EXIF.putItem(e.id, e)
    }
  }
}
