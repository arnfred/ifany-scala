name := "Ifany"

version := "1.0"

scalaVersion := "2.13.7"

// Needed for `sbt compile stage` together with `project/plugins.sbt`
enablePlugins(JavaAppPackaging)

Compile / scalaSource := baseDirectory.value / "src"

scalacOptions ++= Seq("-unchecked", "-Ywarn-dead-code", "-deprecation")

libraryDependencies  ++= Seq(
    "ws.unfiltered" %% "unfiltered-netty-server" % "0.10.+",
    "ws.unfiltered" %% "unfiltered-filter" % "0.10.+",
    "org.json4s" %% "json4s-native" % "4.+",
    "com.github.seratch" %% "awscala-dynamodb" % "0.9.+",
    "com.github.seratch" %% "awscala-s3" % "0.9.+",
    )

resolvers ++= Seq(
    // other resolvers here
    // if you want to use snapshot builds (currently 0.2-SNAPSHOT), use this.
    "akr4 release" at "https://akr4.github.com/mvn-repo/releases",
    "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/",
    "Typesafe Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    )

// The main class
mainClass in (Compile, run) := Some("ifany.Main")
