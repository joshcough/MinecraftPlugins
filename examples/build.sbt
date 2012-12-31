name := "JCDC Plugin Factory Examples"

version := "0.2.2"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.0-RC5"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "jcdc-plugin-factory_2.10" % "0.2.2",
  "javax.servlet"      % "servlet-api"              % "2.5" % "provided->default",
  "org.bukkit"         % "craftbukkit"              % "1.4.5-R0.2",
  // this is only necessary if you want to do some NPC hacking, like in NPCTest.scala
  "ch.spacebase"       % "NPCCreatures"             % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar",
  "org.scala-lang"     % "jline"                    % "2.10.0-RC5",
  "org.clojure"        % "clojure"                  % "1.4.0",
  // TODO: put this back when 2.10 is finally out, i think
  //"org.scalacheck"  %% "scalacheck"               % "1.10.0" % "test",
  "org.scalacheck"     % "scalacheck_2.10.0-RC5"    % "1.10.0" % "test"
)


//packageBin in Compile <<= (packageBin in Compile) dependsOn run
//Keys.`package` <<= (Keys.`package` in Compile) dependsOn run
//compile <<= (compile in Compile) map { result =>
//  println("in compile, something")
//  result
//}
