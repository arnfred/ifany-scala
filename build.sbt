name := "Ifany"

version := "1.0"

scalaVersion := "2.12.9"

Compile / scalaSource := baseDirectory.value / "src"

scalacOptions ++= Seq("-unchecked", "-Ywarn-dead-code", "-deprecation")

libraryDependencies  ++= Seq(
    "ws.unfiltered" %% "unfiltered-netty-server" % "0.9.1",
    "ws.unfiltered" %% "unfiltered-filter" % "0.9.1",
    "joda-time" % "joda-time" % "2.2",
    "org.joda" % "joda-convert" % "1.2",
    "com.github.seratch" %% "awscala-dynamodb" % "0.8.+",
    "com.github.seratch" %% "awscala-s3" % "0.8.+",
    "com.sun.activation" % "javax.activation" % "1.2.0"
    )

resolvers ++= Seq(
    // other resolvers here
    // if you want to use snapshot builds (currently 0.2-SNAPSHOT), use this.
    "akr4 release" at "http://akr4.github.com/mvn-repo/releases",
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    )

// The main class
mainClass in (Compile, run) := Some("ifany.Main")
