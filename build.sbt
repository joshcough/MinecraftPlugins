val projectUrl = "https://github.com/joshcough/MinecraftPlugins"

val compileAndCopyToBukkit = taskKey[Unit]("Compile, package, and copy jar file to bukkit plugins dir")

val standardSettings = join(
  libDeps("javax.servlet" % "servlet-api" % "2.5" % "provided->default"),
  Seq(organization := "com.joshcough", version := "0.3.4", scalaVersion := "2.12.7")
)

// the core plugin library
val core = (project in file("core"))
  .settings(standardSettings, name := "scala-minecraft-plugin-api")
  //copyPluginToBukkitSettings(Some("assembly"))

// this project supplies the scala language classes.
// it is needed in the bukkit plugins dir to run any scala plugins.
val scalaLibPlugin = (project in file("scala-lib-plugin"))
  .settings(
    standardSettings,
    name := "scala-minecraft-scala-library",
    copyPluginToBukkitSettings(None)
  )

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
      generateYml("com.joshcough.minecraft.examples." + projectName + "Commands"),
      copyPluginToBukkitSettings(None)
    ).dependsOn(core)
}

def generateYml(className: String) =
  resourceGenerators in Compile += Def.task {
    Run.run(
      className,
      (productDirectories in Compile).value ++ (dependencyClasspath in Compile).value.map(_.data),
      List(name.value, version.value, (resourceManaged in Compile).value.getAbsolutePath),
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

