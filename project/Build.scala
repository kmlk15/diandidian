import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "diandidian"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "commons-pool" % "commons-pool" % "1.6",
      "log4j" % "log4j" % "1.2.16",
      "com.google.inject" % "guice" % "3.0",
      "org.mongodb" % "mongo-java-driver" % "2.10.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
    )

}
