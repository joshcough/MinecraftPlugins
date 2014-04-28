name := "Block Changer Plugin"

version := "0.3.3"

organization := "examples"

scalaVersion := "2.11.0"

// this specifies where to get the bukkit jar from.
resolvers += "Bukkit" at "http://repo.bukkit.org/content/repositories/releases"

seq(bintrayResolverSettings:_*)

seq(pluginYmlSettings("com.example.BlockChanger", "JoshCough"):_*)

// plugins need to depend on com.joshcough.minecraft, and bukkit
libraryDependencies ++= Seq(
  "com.joshcough"     %% "scala-minecraft-plugin-api" % "0.3.3",
  "org.bukkit"         % "bukkit"                % "1.7.2-R0.2"
)
