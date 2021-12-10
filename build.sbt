ThisBuild / organization := "teamblue"
ThisBuild / scalaVersion := "2.13.6"

// lazy val master = (project in file(".")).
//   settings(
//     name := "master",
//     assembly / mainClass := Some("network.Master"),
//     assembly / assemblyJarName := "master.jar",
//     /* Netty deduplicate error -  https://github.com/sbt/sbt-assembly/issues/362 */
//     assembly / assemblyMergeStrategy := {
//       case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
//       case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
//       case x => MergeStrategy.first
//     } 
//   )

lazy val worker = (project in file(".")).
  settings(
    name := "worker",
    assembly / mainClass := Some("network.Worker"),
    assembly / assemblyJarName := "worker.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
      case x => MergeStrategy.first
    }
  )

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "org.scalamock" %% "scalamock" % "5.1.0" % Test,
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-json4s" % "0.11.1"
)
