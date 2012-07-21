name := "JCDC Plugin Factory Examples"

version := "0.1"

organization := "jcdc.pluginfactory"

scalaVersion := "2.9.1-1"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

libraryDependencies += "bukkit" % "craft-bukkit" % "1.2.5-R4.0" from "http://dl.bukkit.org/downloads/craftbukkit/get/01149_1.2.5-R4.0/craftbukkit.jar"

libraryDependencies += "jcdc.pluginfactory" % "jcdc-plugin-factory_2.9.1-1" % "0.1"

// this is only necessary if you want to do some NPC hacking, like in NPCTest.scala
libraryDependencies += "ch.spacebase" % "NPCCreatures" % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar"

