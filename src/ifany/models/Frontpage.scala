package ifany

import dispatch._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random.shuffle

case class FrontpageModel(categories : List[Category],
                          albums : List[Album],
                          banners : List[Image],
                          exifMap : Map[String, EXIF],
                          imageMap : Map[String, List[Image]])


object FrontpageModel {

  def update : dispatch.Promise[FrontpageModel] = {

    // Get the images for the frontpage
    val banners_P = SmugmugAPI.getImages("12121179","C3Ks6")

    // Get the categories
    val cats_P = SmugmugAPI.getCategories

    // Get the albums
    val albums_P = SmugmugAPI.getAlbums

    // When these have loaded, then do:
    val model_P = for (banners <- banners_P; cats <- cats_P; albums <- albums_P) yield {

      // Get all images
      val images_P = for (a <- albums) yield SmugmugAPI.getImages(a.id, a.key)

      // Get all cover images
      val coverEXIFs_P = for (a <- albums) yield SmugmugAPI.getEXIF(a.cover.id, a.cover.key)

      // When these have loaded, then do:
      for (images <- Http.promise.all(images_P); 
           covers <- Http.promise.all(coverEXIFs_P)) yield {

        val imageMap = { albums map { _.id } zip images } toMap
        val exifMap = { albums map { _.id } zip covers } toMap

        // Get model
        val model = FrontpageModel(cats, albums, banners, exifMap, imageMap)

        // Save model
        save(model)

        // Return model
        model
      }
    }

    // Flatten model to one layer of promies
    model_P.flatten
  }


  def get : Future[FrontpageModel] = {

    // Get the images for the frontpage
    val banners_F = future { Image.getAlbumImages("12121179") }

    // Get the categories
    val cats_F = future { Category.getQuery[Category]() }

    // Get the albums
    val albums_F = future { Album.getQuery[Album]() }

    // Once these load, then we do:
    val model_F = for (banners <- banners_F; cats <- cats_F; albums <- albums_F) yield {

      // Get all cover images
      val coverIDs = albums map { _.cover.id }
      val coverEXIFs_F = future { EXIF.getList[EXIF](coverIDs) }

      // Get 3 images per album plus it's cover
      val imageIDs = makeImageList(albums, 3) ++ coverIDs
      val images_F = future { Image.getList[Image](imageIDs) }

      // When these have loaded, then do:
      for (images <- images_F; covers <- coverEXIFs_F) yield {

        val exifMap = { albums map { _.id } zip covers } toMap
        val imageMap = images.toList groupBy { _.albumID }

        // Get model
        val model = FrontpageModel(cats.toList, albums.toList, banners.toList, exifMap, imageMap)

        println(cats.size + " Categories and " + albums.size + " Albums fetched")

        // Return model
        model
      }
    }
    
    // Now flatten the future and return
    model_F.flatMap(f => f)
  }


  def makeImageList(as : Iterator[Album], n : Int) : Iterator[String] = {
    for (a <- as; i <- shuffle(a.imageIDs).take(n)) yield i
  }
    


  def save(model : FrontpageModel) : Unit = {

    def addImagesToAlbum(a : Album, images : List[Image]) : Album = {
      Album(a.id, a.key, a.title, a.description, images.map { _.id }, a.url, a.categoryID, a.cover)
    }


    println("saving categories")
    // Save categories
    for (cat <- model.categories) Category.putItem(cat.id, cat)

    println("saving albums")
    // Save albums with images
    for (a <- model.albums) {
      Album.putItem(a.id, addImagesToAlbum(a, model.imageMap(a.id)))
    }

    println("saving banners")
    // Save coves
    for (b <- model.banners) Image.putItem(b.id, b)

    println("saving exifs")
    // Save exifs
    for ((id,exif) <- model.exifMap) EXIF.putItem(exif.id, exif)

    println("saving images")
    // Save images
    for ((id,images) <- model.imageMap; i <- images) Image.putItem(i.id, i)

  }
}
