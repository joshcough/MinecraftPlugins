name := "God Mode Plugin"

version := "0.2.0"

organization := "examples"

scalaVersion := "2.10.0-RC5"

resolvers += "Bukkit" at "http://repo.bukkit.org/content/repositories/releases"

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "jcdc-plugin-factory_2.10" % "0.2.0" from
    // TODO: publish this someplace real, and replace this fake url with the real one.
    "http://localhost/~joshcough/jcdc.pluginfactory-2.10.0-0.2.0-standalone.jar",
  "org.bukkit"         % "craftbukkit"              % "1.4.5-R0.2"
)
