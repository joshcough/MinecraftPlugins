name := "JCDC Plugin Factory Examples"

version := "0.3.0"

organization := "jcdc.pluginfactory"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
  "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "jcdc.pluginfactory" % "scala-plugin-api_2.10" % "0.3.0",
  "org.bukkit"         % "craftbukkit"              % "1.4.7-R0.1"
)

//packageBin in Compile <<= (packageBin in Compile) dependsOn run
//Keys.`package` <<= (Keys.`package` in Compile) dependsOn run
//compile <<= (compile in Compile) map { result =>
//  println("in compile, something")
//  result
//}
