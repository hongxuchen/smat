import Dependencies._

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)

ThisBuild / scalaVersion := "2.13.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "sg.edu.ntu"
ThisBuild / organizationName := "NTUCSL"

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.bintrayRepo("shiftleft", "maven"),
  Resolver.bintrayRepo("mpollmeier", "maven"),
  "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/public",
)

lazy val root = (project in file("."))
  .settings(
    name := "smat",
    libraryDependencies += scalaTest % Test
  )

scalacOptions ++= Seq("-unchecked", "-deprecation")

val ScoptVersion = "3.7.1"
val BetterFilesVersion = "3.8.0"
val CirceVersion = "0.12.2"
val ScalatestVersion = "3.0.8"
val ZeroturnaroundVersion = "1.13"
val CpgVersion = "0.11.356"
val Fuzzyc2cpgVersion = "1.1.73"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % ScoptVersion,
  "com.github.pathikrit" %% "better-files" % BetterFilesVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-parser" % CirceVersion,
  "org.zeroturnaround" % "zt-zip" % ZeroturnaroundVersion,
  "io.shiftleft" %% "codepropertygraph" % CpgVersion,
  "io.shiftleft" %% "semanticcpg" % CpgVersion,
  "io.shiftleft" %% "console" % CpgVersion,
  "io.shiftleft" %% "dataflowengineoss" % CpgVersion,
  "io.shiftleft" %% "fuzzyc2cpg" % Fuzzyc2cpgVersion,
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.9.0",
  "org.apache.commons" % "commons-text" % "1.9",
  "com.github.haifengl" %% "smile-scala" % "2.5.2",
  "org.scalatest" %% "scalatest" % ScalatestVersion % Test
)
