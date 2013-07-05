name := "Scala Plugin API"

version := "0.3.0"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.2"

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "java-plugin-api_2.10"  % "0.3.0",
  //"org.squeryl"       %% "squeryl"               % "0.9.6-SNAPSHOT",
  //"mysql"              % "mysql-connector-java"  % "5.1.10",
  //"org.apache.derby"   % "derby"                 % "10.7.1.1",
  "javax.servlet"      % "servlet-api"           % "2.5" % "provided->default",
  "org.bukkit"         % "craftbukkit"           % "1.4.7-R0.1",
  "ch.spacebase"       % "NPCCreatures"          % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar",
  "org.clojure"        % "clojure"               % "1.4.0",
  "org.scalacheck"    %% "scalacheck"            % "1.10.0" % "test"
)
