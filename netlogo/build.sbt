name := "NetLogo Minecraft Plugin"

version := "0.1"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.0-M7"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

libraryDependencies += "bukkit" % "craft-bukkit" % "1.3.1-R2.0" from "http://dl.bukkit.org/downloads/craftbukkit/get/01371_1.3.1-R2.0/craftbukkit.jar"

libraryDependencies += "ch.spacebase" % "NPCCreatures" % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar"

libraryDependencies += "jcdc.pluginfactory" % "jcdc-plugin-factory_2.10" % "0.1"

libraryDependencies += "org.nlogo" % "NetLogo" % "5.0.1" from "http://ccl.northwestern.edu/netlogo/5.0.1/NetLogo.jar"
