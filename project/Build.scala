import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "diandidian"
  val appVersion = "0.1.0"

  javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
    
  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.twitter4j" % "twitter4j-core" % "3.0.3" ,
    "org.seleniumhq.selenium" % "selenium-java" % "2.33.0" % "test",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "org.mongodb" % "casbah_2.10" % "2.5.0",
    "net.debasishg" % "sjson_2.10" % "0.19",
    "com.novus" % "salat-core_2.10" % "1.9.2-SNAPSHOT",
    "com.amazonaws" % "aws-java-sdk" % "1.5.3"
  )

  def commonResolvers = Seq(
    "sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= commonResolvers
  )

}
