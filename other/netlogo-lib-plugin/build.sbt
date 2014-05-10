import AssemblyKeys._

artifact in (Compile, assembly) ~= { art => art.copy(`classifier` = Some("assembly")) }

addArtifact(artifact in (Compile, assembly), assembly)

// TODO: plugin doc says "To exclude some jar file, first consider using "provided" dependency."
// Figure out what that means...
excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter { x =>
    x.data.getName.contains("bukkit") ||
    x.data.getName.contains("scala-library")
  }
}