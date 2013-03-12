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

  def update : Unit = {

    // Get the images for the frontpage
    val banners_P = SmugmugAPI.getImages("12121179","C3Ks6")

    // Get the categories
    val cats_P = SmugmugAPI.getCategories

    // Get the albums
    val albums_P = SmugmugAPI.getAlbums

    // When these have loaded, then do:
    for (banners <- banners_P; cats <- cats_P; albums <- albums_P) yield {

      // Get all images
      val images_P = for (a <- albums) yield SmugmugAPI.getImages(a.id, a.key)

      // Get all cover images
      val exifs_P = for (a <- albums) yield SmugmugAPI.getEXIF(a.cover.id, a.cover.key)

      // When these have loaded, then do:
      for (images <- Http.promise.all(images_P); 
           exifs <- Http.promise.all(exifs_P)) yield {

        val imageMap = { albums map { _.id } zip images } toMap
        val exifMap = { albums map { _.id } zip exifs } toMap

        // Save this info in db
        save(cats, albums, banners, exifMap, imageMap)
      }
    }

  }

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



  def save(categories : List[Category], albums : List[Album], banners : List[Image], exifMap : Map[String, EXIF], imageMap : Map[String, List[Image]]) : Unit = {

    def addImagesToAlbum(a : Album, images : List[Image]) : Album = {
      Album(a.id, a.key, a.title, a.description, images.map { _.id }, a.url, a.categoryID, a.cover, a.nav)
    }

    def addAlbumIDtoImage(im : Image, albumID : String) : Image = {
      Image(im.id, im.key, albumID, im.caption, im.url, im.size)
    }
      
    def addAlbumIDtoEXIF(exif : EXIF, albumID : String) : EXIF = {
      EXIF(exif.id, exif.key, albumID, exif.aperture, exif.focalLength, exif.iso, exif.model, exif.dateTime)
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

    // Save banners
    println("saving banners")
    Album.putItem("12121179", Album("12121179","none","banner images","banner images", banners.map(_.id), "none", None, Cover("0","0"), Navigation(None,None)))

    // Save exifs
    println("saving exifs")
    for ((albumID, exif) <- exifMap) {
      val e = addAlbumIDtoEXIF(exif, albumID)
      EXIF.putItem(exif.id, e)
    }

    // Save images
    println("saving images")
    for ((id, images) <- imageMap; i <- images) {
      Image.putItem(i.id, addAlbumIDtoImage(i, id))
    }

  }

  def setPrevNext(albums : List[Album], exifMap : Map[String, EXIF]) : List[Album] = {

    val sorted = albums.sortBy { a => exifMap(a.id).dateTime.getMillis } reverse


    val navs = for (index <- (0 to (sorted.size - 1))) yield (index == 0, index == (albums.size -1)) match {
      case (true, true) => Navigation(None, None)
      case (true, _) => Navigation(None, Some(sorted(index+1).url))
      case (_, true) => Navigation(Some(sorted(index-1).url), None)
      case (false, false) => Navigation(Some(sorted(index-1).url), Some(sorted(index+1).url))
    }

    for ((a,nav) <- sorted.zip(navs)) yield {
      Album(a.id, a.key, a.title, a.description, a.imageIDs, a.url, a.categoryID, a.cover, nav)
    }

  }
}
