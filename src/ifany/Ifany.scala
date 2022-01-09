package ifany

import unfiltered.netty._

object Main {

  def main(args : Array[String]): Unit= {

    // Fetch the server
    val srv = Ifany.init

    // Run the server
    srv.run()
  }
}



object Ifany {

  import util.Properties

  val photoDir : String = "/photos/"

  def init = {

    // Where files for the web server are located
    val resourceDir = new java.io.File("resources/")

    // The default port used for testing. This will have to change for deployment
    val testPort = Properties.envOrElse("PORT", "8000").toInt
    println("starting on port: " + testPort)

    // Initialize Server
    var srv = unfiltered.netty.Server.http(testPort).resources(resourceDir.toURI.toURL);

    // Run server
    //srv.handler(DataPlan).handler(GalleryPlan)
    srv.handler(GalleryPlan)
  }

}


