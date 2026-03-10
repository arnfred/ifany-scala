// Needed for `sbt compile stage` together with the `enablePlugins` line in `build.sbt`
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

// Auto-restart server on source changes (use ~reStart in sbt shell)
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")
