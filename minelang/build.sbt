name := "JCDC Plugin Factory Examples"

version := "0.1"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.0-RC5"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "jcdc-plugin-factory_2.10" % "0.1",
  "javax.servlet"      % "servlet-api"              % "2.5" % "provided->default",
  "org.bukkit"         % "craftbukkit"              % "1.4.5-R0.2",
  "org.scala-lang"     % "jline"                    % "2.10.0-RC5",
  "org.clojure"        % "clojure"                  % "1.4.0",
  "org.scalacheck"     % "scalacheck_2.10.0-RC5"    % "1.10.0" % "test"
)
