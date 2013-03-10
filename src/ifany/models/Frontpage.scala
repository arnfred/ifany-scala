package ifany

import dispatch._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.shuffle

case class FrontpageModel(categories : List[Category],
                          albums : List[Album],
                          bannerID : String,
                          exifMap : Map[String, EXIF],
                          imageMap : Map[String, List[Image]])


object FrontpageModel {

  def getBannerID(albums : List[Album]) : String = albums filter(_.id == "12121179") match {
    case Nil    => throw new Exception("The album with the banners hasn't been loaded")
    case list   => list.head.imageIDs match {
      case Nil    => throw new Exception("The banner album doesn't have any images")
      case ids    => shuffle(ids).head
    }
  }


  def get : Future[FrontpageModel] = {

    //println("getting banner album")
    // Get the images for the frontpage
    //val bannerAlbum_F = future { Album.getItem[Album]() }

    println("getting categories")
    // Get the categories
    val cats_F = future { Category.getQuery[Category]() }

    println("getting albums")
    // Get the albums
    val albums_F = future { Album.getQuery[Album]() }

    // Once these load, then we do:
    val model_F = for (cats <- cats_F; albums <- albums_F) yield {

      // Get banner id
      val bannerID = getBannerID(albums)

      println("fetched banner ID: " + bannerID)
      println("fetched " + cats.size + " categories")
      println("fetched " + albums.size + " albums")

      println("getting covers")
      // Get all cover images
      val coverIDs = (albums map { case a => (a.id -> a.cover.id) })toMap
      val exifs_F = future { EXIF.getList[EXIF](coverIDs.values.toList) }

      println("getting images")

      // Get 3 images per album plus it's cover
      val imageIDs = makeImageList(albums, coverIDs, 3) ++ coverIDs.values.toList ++ List(bannerID)
      val images_F = future { Image.getList[Image](imageIDs) }

      // When these have loaded, then do:
      for (images <- images_F; exifs <- exifs_F) yield {


        println("fetched " + images.size + " images")
        println("fetched " + exifs.size + " covers")
        val exifMap = exifs groupBy { _.albumID } map { case (id,exifList) => (id -> exifList.head) }
        val imageMap = images.toList groupBy { _.albumID }
        println(exifs.map{_.dateTime})

        // Get model
        val model = FrontpageModel(cats, albums, bannerID, exifMap, imageMap)

        println(cats.size + " Categories and " + albums.size + " Albums fetched")

        // Return model
        model
      }
    }
    
    // Now flatten the future and return
    model_F.flatMap(f => f)
  }


  def makeImageList(as : List[Album], exclude : Map[String,String], n : Int) : List[String] = {
    var albumImageIDs = for (a <- as) yield a.imageIDs.filter( exclude(a.id) != _ )
    for (imageIDs <- albumImageIDs; id <- shuffle(imageIDs).take(n)) yield id
  }

}
