name := "JCDC Plugin Factory Examples"

version := "0.1"

organization := "jcdc.pluginfactory"

scalaVersion := "2.9.1-1"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

libraryDependencies += "bukkit" % "craft-bukkit" % "1.2.5-R1" from "http://dl.bukkit.org/latest-rb/craftbukkit.jar"

libraryDependencies += "jcdc.pluginfactory" % "jcdc-plugin-factory_2.9.1-1" % "0.1"

// this is only necessary if you want to do some NPC hacking, like in NPCTest.scala
libraryDependencies += "ch.spacebase" % "NPCCreatures" % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar"

