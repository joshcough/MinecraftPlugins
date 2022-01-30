val projectUrl = "https://github.com/joshcough/MinecraftPlugins"

import com.joshcough.minecraft.MinecraftPluginPlugin._

val standardSettings = join(
  libDeps(
    "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
    "org.bukkit" % "bukkit" % "1.16.1-R0.1-SNAPSHOT"
  ),
  Seq(organization  := "com.joshcough"
    , version       := "0.3.5-SNAPSHOT"
    , scalaVersion  := "2.13.6"
    , scalacOptions := Seq("-unchecked", "-deprecation")),
  resolvers += "Bukkit" at "https://maven.elmakers.com/repository"
)

// the core plugin library
val core = (project in file("core"))
  .settings(
    standardSettings,
    name := "scala-minecraft-plugin-api",
    copyPluginToBukkitSettings(Some(new File("bukkit")))
  )

// this project supplies the scala language classes.
// it is needed in the bukkit plugins dir to run any scala plugins.
val scalaLibPlugin = (project in file("scala-lib-plugin"))
  .settings(standardSettings, name := "scala-minecraft-scala-library")

val MultiPlayerCommands = exampleProject("MultiPlayerCommands")
val WorldEdit           = exampleProject("WorldEdit")
val Warp                = exampleProject("Warp")
val LightningArrows     = exampleProject("LightningArrows")
val NoRain              = exampleProject("NoRain")
val PluginCommander     = exampleProject("PluginCommander")
val Shock               = exampleProject("Shock")

def exampleProject(projectName: String) = {
  Project(projectName, new File("examples/" + projectName))
    .settings(
      standardSettings,
      minecraftProject(
        author = "Josh Cough",
        pluginConfigClassName = "com.joshcough.minecraft.examples." + projectName + "Config",
        bukkitDir = Some(new File("bukkit"))
      ),
    ).dependsOn(core)
}

// this is just a convenience project
// for me to easily publish my most used plugins to my bukkit server.
val commonPlugins = (project in file(".commonPlugins"))
  .settings(standardSettings)
  .aggregate(scalaLibPlugin, core, MultiPlayerCommands, PluginCommander, Warp, WorldEdit)

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
