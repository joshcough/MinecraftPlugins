name := "Block Changer Plugin"

version := "0.3.2"

organization := "examples"

scalaVersion := "2.10.2"

// this specifies where to get the bukkit jar from.
resolvers += "Bukkit" at "http://repo.bukkit.org/content/repositories/releases"

seq(bintrayResolverSettings:_*)

seq(pluginYmlSettings("com.example.BlockChanger", "JoshCough"):_*)

// plugins need to depend on com.joshcough.minecraft, and bukkit
libraryDependencies ++= Seq(
  "com.joshcough"     %% "scala-minecraft-plugin-api" % "0.3.2",
  "org.bukkit"         % "craftbukkit"                % "1.5.2-R1.0"
)
