name := "God Mode Plugin"

version := "0.2.0"

organization := "examples"

scalaVersion := "2.10.0-RC5"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "jcdc-plugin-factory_2.10" % "0.2.0",
  "org.bukkit"         % "craftbukkit"              % "1.4.5-R0.2"
)
