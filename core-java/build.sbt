name := "Java Plugin API"

version := "0.3.0"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.0"

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "javax.servlet"     % "servlet-api"            % "2.5" % "provided->default",
  "org.bukkit"        % "craftbukkit"            % "1.4.7-R0.1"
)
