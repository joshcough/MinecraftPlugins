addArtifact(artifact in (Compile, assembly), assembly)

assembly / assemblyExcludedJars := {
  val cp = (assembly / fullClasspath).value
  cp filter {_.data.getName.contains("bukkit")}
}

