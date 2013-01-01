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
  "jcdc.pluginfactory" % "scala-plugin-api_2.10" % "0.2.2",
  "org.bukkit"         % "craftbukkit"              % "1.4.6-R0.3"
)


// TODO: put this back when 2.10 is finally out, i think
//"org.scalacheck"  %% "scalacheck"               % "1.10.0" % "test",

//packageBin in Compile <<= (packageBin in Compile) dependsOn run
//Keys.`package` <<= (Keys.`package` in Compile) dependsOn run
//compile <<= (compile in Compile) map { result =>
//  println("in compile, something")
//  result
//}
