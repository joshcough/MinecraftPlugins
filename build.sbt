val projectUrl = "https://github.com/joshcough/MinecraftPlugins"

val author = taskKey[String]("Author")
val pluginConfigClass = taskKey[String]("Plugin config class")
val compileAndCopyToBukkit = taskKey[Unit]("Runs publishLocal, and copies jar file to bukkit plugins dir")

val standardSettings = join(
  libDeps("javax.servlet" % "servlet-api" % "2.5" % "provided->default"),
  Seq(organization := "com.joshcough", version := "0.3.5-SNAPSHOT", scalaVersion := "2.12.7")
)

// the core plugin library
val core = (project in file("core"))
  .settings(
    standardSettings,
    name := "scala-minecraft-plugin-api",
    copyPluginToBukkitSettings(None)
  )

// this project supplies the scala language classes.
// it is needed in the bukkit plugins dir to run any scala plugins.
val scalaLibPlugin = (project in file("scala-lib-plugin"))
  .settings(standardSettings, name := "scala-minecraft-scala-library")
  //copyPluginToBukkitSettings(Some("assembly"))

val MultiPlayerCommands = exampleProject("MultiPlayerCommands")
val WorldEdit           = exampleProject("WorldEdit")
val Warp                = exampleProject("Warp")
val Danny               = exampleProject("Danny")
val LightningArrows     = exampleProject("LightningArrows")
val NoRain              = exampleProject("NoRain")
val PluginCommander     = exampleProject("PluginCommander")
val Shock               = exampleProject("Shock")

def exampleProject(projectName: String) = {
  Project(projectName, new File("examples/" + projectName))
    .settings(
      standardSettings,
      author := "Josh Cough",
      pluginConfigClass := "com.joshcough.minecraft.examples." + projectName + "Config",
      generateYml,
      copyPluginToBukkitSettings(None)
    ).dependsOn(core)
}

def generateYml =
  resourceGenerators in Compile += Def.task {
    Run.run(
      "com.joshcough.minecraft.YMLGenerator",
      (productDirectories in Compile).value ++ (dependencyClasspath in Compile).value.map(_.data),
      List(name.value, author.value, version.value, (resourceManaged in Compile).value.getAbsolutePath, pluginConfigClass.value),
      streams.value.log
    )(runner.value)
    Seq((resourceManaged in Compile).value / "plugin.yml")
  }.taskValue

def copyPluginToBukkitSettings(bukkitDir: Option[File])= compileAndCopyToBukkit := {
  publishLocal.value
  packagedArtifacts.value.foreach{ case (a, f) =>
    if(a.classifier.isEmpty && a.`type` == "jar") {
      val out = bukkitDir match {
        case None => new File("bukkit/plugins/" + f.name)
        case Some(dir) => new File(dir, f.name)
      }
      println("copying " + f.name + " to bukkit server at " + out.getAbsolutePath)
      IO.copyFile(f, out)
    }
  }
}

def join(settings: Seq[Def.Setting[_]]*) = settings.flatten
def libDeps(libDeps: sbt.ModuleID*) = Seq(libraryDependencies ++= libDeps)

// TODO:
//  lazy val microExample = Project(id = "microexample", base = file("microexample"))

//  // a whole pile of example projects
//  lazy val Arena               = exampleProject("Arena")
//  lazy val BanArrows           = exampleProject("BanArrows")
//  lazy val BlockChanger        = exampleProject("BlockChanger")
//  lazy val BlockChangerGold    = exampleProject("BlockChangerGold")
//  lazy val GetOffMyLawn        = exampleProject("GetOffMyLawn")
//  lazy val God                 = exampleProject("God")
//  lazy val Thor                = exampleProject("Thor")
//  lazy val TeleportBows        = exampleProject("TeleportBows")
//  lazy val TreeDelogger        = exampleProject("TreeDelogger")
//  lazy val YellowBrickRoad     = exampleProject("YellowBrickRoad")
//  lazy val ZombieApocalypse    = exampleProject("ZombieApocalypse")

//  // this is just a convenience project
//  // for me to easily publish my most used plugins to my bukkit server.
//  // > sbt 'project commonPlugins' publishLocal
//  lazy val commonPlugins = Project(
//    id = "commonPlugins",
//    base = file(".commonPlugins"),
//    settings = standardSettings,
//    aggregate = Seq(
//      scalaLibPlugin,
//      core,
//      MultiPlayerCommands,
//      PluginCommander,
//      WorldEdit
//    )
//  )

