package ifany

import dispatch._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.nextInt

object Update {

  def updateAll(withExif : Boolean = true) : Unit = {

    // Get the images for the frontpage
    val banners_P = SmugmugAPI.getImages("12121179","C3Ks6")

    // Get the categories
    val cats_P = SmugmugAPI.getCategories

    // Get the albums
    val albums_P = SmugmugAPI.getAlbums

    // When these have loaded, then do:
    for (banners <- banners_P; cats <- cats_P; albums <- albums_P) yield {

      // Get all images
      val images_P = for (a <- albums) yield {
        SmugmugAPI.getImages(a.id, a.key).map { images =>
          for (image <- images) yield {
            val withID = addAlbumIDtoImage(image, a.id)
            withID
          }
        }
      }

      // When these have loaded, then do:
      for (images <- Http.promise.all(images_P)) yield {

        val imageMap = { albums map { _.id } zip images } toMap

        // get exifs
        val exifs_P = for ((albumID, images) <- imageMap; i <- images) yield {
          Thread.sleep(nextInt(500)) 
          println("Getting exif for image " + i.id)
          SmugmugAPI.getEXIF(i.id, i.key).map { exif =>
            val withID = addAlbumIDtoEXIF(exif, albumID)
            println("Saving exif with id: " + exif.id)
            EXIF.putItem(exif.id, withID)
            withID
          }
        }
        
        for (exifs <- Http.promise.all(exifs_P)) yield {

          val albumList = for ((albumId, images) <- imageMap; i <- images) yield albumId
          val exifMap : Map[String, EXIF] = albumList.zip(exifs).toMap

          // Save this info in db
          save(cats, albums, banners, imageMap, exifMap)
        }

      }
    }
  }


  def setPrevNext(albums : List[Album], exifMap : Map[String, EXIF]) : List[Album] = {

    val sorted = albums.sortBy { a => exifMap(a.id).dateTime.getMillis } reverse

    def getNav(album : Album) : NavElem = NavElem(album.url, album.title)


    val navs = for (index <- (0 to (sorted.size - 1))) yield (index == 0, index == (albums.size -1)) match {
      case (true, true) => Navigation(None, None)
      case (true, _) => Navigation(None, Some(getNav(sorted(index+1))))
      case (_, true) => Navigation(Some(getNav(sorted(index-1))), None)
      case (false, false) => Navigation(Some(getNav(sorted(index-1))), Some(getNav(sorted(index+1))))
    }

    for ((a,nav) <- sorted.zip(navs)) yield {
      Album(a.id, a.key, a.title, a.description, a.imageIDs, a.url, a.categoryID, a.cover, nav)
    }

  }

  def addAlbumIDtoEXIF(exif : EXIF, albumID : String) : EXIF = {
    EXIF(exif.id, exif.key, albumID, exif.aperture, exif.focalLength, exif.iso, exif.model, exif.dateTime)
  }

  def addAlbumIDtoImage(im : Image, albumID : String) : Image = {
    Image(im.id, im.key, albumID, im.caption, im.url, im.size)
  }

  def save(categories : List[Category], albums : List[Album], banners : List[Image], imageMap : Map[String, List[Image]], exifMap : Map[String, EXIF]) : Unit = {

    def addImagesToAlbum(a : Album, images : List[Image]) : Album = {
      Album(a.id, a.key, a.title, a.description, images.map { _.id }, a.url, a.categoryID, a.cover, a.nav)
    }

    // Update albums with prev and next
    val as = setPrevNext(albums, exifMap)

    println("saving categories")
    // Save categories
    for (cat <- categories) Category.putItem(cat.id, cat)

    println("saving albums")
    // Save albums with images
    for (a <- as) {
      Album.putItem(a.id, addImagesToAlbum(a, imageMap(a.id)))
    }
    // Save images
    println("saving images")
    for ((id, images) <- imageMap; i <- images) {
      Image.putItem(i.id, i)
    }

    // Save banners
    println("saving banners")
    Album.putItem("12121179", Album("12121179","none","banner images","banner images", banners.map(_.id), "none", None, Cover("0","0"), Navigation(None,None)))

  }

}
