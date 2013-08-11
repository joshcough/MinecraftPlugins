import AssemblyKeys._

artifact in (Compile, assembly) ~= { art => art.copy(`classifier` = Some("assembly")) }

addArtifact(artifact in (Compile, assembly), assembly)

// TODO: fix me, maybe ask people here: https://github.com/sbt/sbt-assembly
//jarName in (assembly, normalizedName, scalaVersion) ~= { case (_, n, v) => n + v + ".jar" }

// TODO: this works when I run 'assemble' but not when I publish.
// I opened a ticket here: https://github.com/sbt/sbt-assembly/issues/88
jarName in assembly := "scala-minecraft-scala-library_2.10.2.jar"

// TODO: plugin doc says "To exclude some jar file, first consider using "provided" dependency."
// Figure out what that means...
excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {_.data.getName.contains("craftbukkit")}
}