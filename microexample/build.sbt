name := "Block Changer Plugin"

version := "0.3.1"

organization := "examples"

scalaVersion := "2.10.2"

// this specifies where to get the bukkit jar from.
resolvers += "Bukkit" at "http://repo.bukkit.org/content/repositories/releases"

// plugins need to depend on com.joshcough.minecraft, and bukkit
libraryDependencies ++= Seq(
  "com.joshcough"      % "scala-plugin-api_2.10.2" % "0.3.0" from
    "http://dev.bukkit.org/media/files/666/690/scala-plugin-api_2.10-0.3.0.jar",
  "org.bukkit"         % "craftbukkit"                 % "1.5.2-R1.0"
)
