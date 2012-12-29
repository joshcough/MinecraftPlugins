name := "Block Changer Plugin"

version := "0.2.0"

organization := "examples"

scalaVersion := "2.10.0-RC5"

// this specifies where to get the bukkit jar from.
resolvers += "Bukkit" at "http://repo.bukkit.org/content/repositories/releases"

// plugins need to depend on jcdc.pluginfactory, and bukkit
libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "scala-plugin-api_2.10.0-RC5" % "0.2.0" from
    "http://dev.bukkit.org/media/files/660/216/scala-plugin-api_2.10.0-RC5-0.2.0.jar",
  "org.bukkit"         % "craftbukkit"              % "1.4.5-R0.2"
)
