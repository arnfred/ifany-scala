import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

name := "Ifany"

version := "1.0"

scalaVersion := "2.10.0"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalacOptions ++= Seq("-unchecked", "-Ywarn-dead-code", "-deprecation")

libraryDependencies  ++= Seq(
            // other dependencies here
            // pick and choose:
			"net.databinder" %% "unfiltered-netty-server" % "0.6.7",
			//"net.databinder" %% "unfiltered-spec" % "0.6.7" % "test",
			"net.databinder" %% "unfiltered-filter" % "0.6.7",
			"net.databinder" %% "unfiltered-json" % "0.6.7",
			"net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
			"com.dongxiguo" %% "fastring" % "0.2.1",
			"org.mongodb" %% "casbah" % "2.5.0",
			"com.novus" %% "salat" % "1.9.2-SNAPSHOT",
			"joda-time" % "joda-time" % "2.2",
			"org.joda" % "joda-convert" % "1.2"
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
//mainClass in (Compile, run) := Some("paper.Main")

// The sources to be watched
// watchSources <+= baseDirectory map { _ / "paper" }
// watchSources <+= baseDirectory map { _ / "web" }
// watchSources <+= baseDirectory map { _ / "paper/Linking" }
// watchSources <+= baseDirectory map { _ / "paper/Loading" }
// watchSources <+= baseDirectory map { _ / "paper/Parsing" }
