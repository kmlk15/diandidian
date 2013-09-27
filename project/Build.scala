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
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "com.itextpdf" % "itext-asian" % "5.2.0",
    "com.itextpdf" % "itextpdf" % "5.4.4" ,
    "com.itextpdf.tool" % "xmlworker" % "5.4.3",
    "net.debasishg" %% "sjson" % "0.19",
    "com.novus" %% "salat" % "1.9.2" ,
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
