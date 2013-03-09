package ifany

// Unfiltered
//import dispatch._
import unfiltered.netty._
//import unfiltered.request._
//import unfiltered.response._

object Main {

  def main(args : Array[String]): Unit= {

    // Fetch the server
    val srv = Ifany.init

    // Run the server
    srv.run
  }

}



object Ifany {

  import util.Properties

  def init = {

    // Where files for the web server are located
    val resourceDir = new java.io.File("resources/")

    // The default port used for testing. This will have to change for deployment
    val testPort = Properties.envOrElse("PORT", "8000").toInt
    println("starting on port: " + testPort)

    // Initialize Server
    var srv = unfiltered.netty.Http(testPort).chunked(1048576).resources(resourceDir.toURI.toURL);
    //val srv = unfiltered.jetty.Http(testPort).resources(resourceDir.toURI.toURL)

    // Run server
    srv
    //srv.handler(SmugmugPlan).handler(DataPlan)
  }

}


