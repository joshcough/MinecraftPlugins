name := "Java Examples"

version := "0.3.0"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "java-plugin-api_2.10"  % "0.3.0",
  "org.bukkit"         % "craftbukkit"              % "1.4.7-R0.1"
)
