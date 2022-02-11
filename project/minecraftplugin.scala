package com.joshcough.minecraft

import sbt._, Keys._

object MinecraftPluginPlugin extends AutoPlugin {
  object autoImport {
    val minecraftPluginAuthor       = taskKey[String]("Author")
    val minecraftPluginConfigClass  = taskKey[String]("Plugin config class")
    val minecraftPluginPublishLocal = taskKey[Unit]("Runs publishLocal, and copies jar file to bukkit plugins dir")
  }

  import autoImport._

  def minecraftProject(author: String, pluginConfigClassName: String, bukkitDir: Option[File]) = Seq(
    minecraftPluginAuthor := author,
    minecraftPluginConfigClass := pluginConfigClassName,
    generateYml,
    copyPluginToBukkitSettings(bukkitDir)
  )

  def join(settings: Seq[Def.Setting[_]]*) = settings.flatten

  def generateYml: Def.Setting[Seq[Task[Seq[File]]]] =
    resourceGenerators in Compile += Def.task {
      Run.run(
        "com.joshcough.minecraft.YMLGenerator",
        (productDirectories in Compile).value ++ (dependencyClasspath in Compile).value.map(_.data),
        List(
          name.value,
          minecraftPluginAuthor.value,
          version.value,
          (resourceManaged in Compile).value.getAbsolutePath,
          minecraftPluginConfigClass.value),
        streams.value.log
      )(runner.value)
      Seq((resourceManaged in Compile).value / "plugin.yml")
    }.taskValue

  def copyPluginToBukkitSettings(bukkitDir: Option[File])=
    minecraftPluginPublishLocal := {
      publishLocal.value
      packagedArtifacts.value.foreach{ case (a, f) =>
        if(a.classifier.isEmpty && a.`type` == "jar") {
          val out = bukkitDir match {
            case None => new File("bukkit/plugins/" + f.name)
            case Some(dir) => new File(dir.getAbsolutePath + "/plugins/", f.name)
          }
          println("copying " + f.name + " to bukkit server at " + out.getAbsolutePath)
          IO.copyFile(f, out)
        }
      }
    }
}