name := "JCDC Plugin Factory Examples"

version := "0.3.0"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "scala-plugin-api_2.10" % "0.3.0",
  //"org.squeryl"       %% "squeryl"               % "0.9.6-SNAPSHOT",
  //"mysql"              % "mysql-connector-java"  % "5.1.10",
  //"org.apache.derby"   % "derby"                 % "10.7.1.1",
  "cglib"              % "cglib-nodep"           % "2.2",
  "org.bukkit"         % "craftbukkit"           % "1.5.2-R1.0",
  "org.scalacheck"    %% "scalacheck"            % "1.10.0" % "test"
)

//packageBin in Compile <<= (packageBin in Compile) dependsOn run
//Keys.`package` <<= (Keys.`package` in Compile) dependsOn run
//compile <<= (compile in Compile) map { result =>
//  println("in compile, something")
//  result
//}
