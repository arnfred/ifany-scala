name := "Ifany"

version := "1.0"

scalaVersion := "3.6.4"

// Needed for `sbt compile stage` together with `project/plugins.sbt`
enablePlugins(JavaAppPackaging)

Compile / scalaSource := baseDirectory.value / "src"

scalacOptions ++= Seq("-unchecked", "-deprecation")

libraryDependencies  ++= Seq(
    "ws.unfiltered" %% "unfiltered-netty-server" % "0.10.4",
    "ws.unfiltered" %% "unfiltered-filter" % "0.10.4",
    "org.json4s" %% "json4s-native" % "4.0.7",
    "com.github.seratch" %% "awscala-dynamodb" % "0.9.2",
    "com.github.seratch" %% "awscala-s3" % "0.9.2",
    "com.github.jwt-scala" %% "jwt-json4s-native" % "10.0.4",
    "com.lihaoyi" %% "scalatags" % "0.13.1",
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
Compile / run / mainClass := Some("ifany.Main")
