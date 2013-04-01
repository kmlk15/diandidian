import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "diandidian"
  val appVersion = "0.1.0"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.mongodb" % "casbah_2.10" % "2.5.0",
    "net.debasishg" % "sjson_2.10" % "0.19",
    "com.novus" % "salat-core_2.10" % "1.9.2-SNAPSHOT"
  )

  def commonResolvers = Seq(
    "sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers ++= commonResolvers
  )

}
