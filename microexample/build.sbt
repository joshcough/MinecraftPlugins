name := "Block Changer Plugin"

version := "0.3.4"

organization := "examples"

scalaVersion := "2.13.6"

// this specifies where to get the bukkit jar from.
resolvers += "Bukkit" at "https://maven.elmakers.com/repository"

resolvers += Resolver.jcenterRepo

seq(pluginYmlSettings("com.example.BlockChanger", "JoshCough"):_*)

// plugins need to depend on com.joshcough.minecraft, and bukkit
libraryDependencies ++= Seq(
  "com.joshcough"     %% "scala-minecraft-plugin-api" % "0.3.4",
  "org.bukkit"         % "bukkit"                     % "1.16.1-R0.1" % "SNAPSHOT"
)
