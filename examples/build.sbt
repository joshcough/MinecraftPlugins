name := "JCDC Plugin Factory Examples"

version := "0.1"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.0-M7"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

libraryDependencies += "bukkit" % "craft-bukkit" % "1.4.2-R0.1" from "http://dl.bukkit.org/downloads/craftbukkit/get/01489_1.4.2-R0.1/craftbukkit-dev.jar"

libraryDependencies += "jcdc.pluginfactory" % "jcdc-plugin-factory_2.10" % "0.1"

// this is only necessary if you want to do some NPC hacking, like in NPCTest.scala
libraryDependencies += "ch.spacebase" % "NPCCreatures" % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

// TODO: put this back when 2.10 is finally out, i think
//libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.0" % "test"
libraryDependencies += "org.scalacheck" % "scalacheck_2.10.0-M7" % "1.10.0" % "test"

libraryDependencies += "org.scalaz" % "scalaz-core_2.10.0-M7" % "7.0.0-M3"