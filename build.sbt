name := "JCDC Plugin Factory"

version := "0.1"

organization := "jcdc.pluginfactory"

scalaVersion := "2.9.1-1"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided->default"

//libraryDependencies += "bukkit" % "craft-bukkit" % "1.0.1-R1" from "http://ci.bukkit.org/job/dev-CraftBukkit/1597/artifact/target/craftbukkit-1.0.1-R1.jar"
//craftbukkit-1.2.5-R1.0.jar

libraryDependencies += "bukkit" % "craft-bukkit" % "1.2.5-R1" from "http://dl.bukkit.org/latest-rb/craftbukkit.jar"
