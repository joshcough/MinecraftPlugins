
val projectUrl = "https://github.com/joshcough/MinecraftPlugins"

def join(settings: Seq[Def.Setting[_]]*) = settings.flatten
def libDeps(libDeps: sbt.ModuleID*) = Seq(libraryDependencies ++= libDeps)

lazy val standardSettings = join(
  libDeps(
    "javax.servlet" % "servlet-api" % "2.5" % "provided->default"
  ),
  Seq(
    organization := "com.joshcough",
    version := "0.3.4",
    scalaVersion := "2.12.7",
    traceLevel := 10
  )
)

// the core plugin library
lazy val core = (project in file("core"))
  .settings(
      standardSettings
    , name := "scala-minecraft-plugin-api"
  )

// this project supplies the scala language classes.
// it is needed in the bukkit plugins dir to run any scala plugins.
lazy val scalaLibPlugin = (project in file("scala-lib-plugin"))
  .settings(
    standardSettings,
    name := "scala-minecraft-scala-library",
    //      copyPluginToBukkitSettings(Some("assembly"))
  )

val WorldEdit = (project in file("examples/WorldEdit"))
  .settings(standardSettings, name := "WorldEdit").dependsOn(core)

val MultiPlayerCommands = (project in file("examples/MultiPlayerCommands"))
  .settings(standardSettings, name := "MultiPlayerCommands").dependsOn(core)

val Warp = (project in file("examples/Warp"))
  .settings(standardSettings, name := "Warp").dependsOn(core)

//object build extends Build
//  with Common {
////  with ExamplesBuild {
//
//  // this is the main project, that builds all subprojects.
//  // it doesnt contain any code itself.
//  lazy val all = Project(
//    id = "all",
//    base = file("."),
//    settings = standardSettings,
//    aggregate = Seq(
////      Arena,
////      BanArrows,
////      BlockChanger,
////      BlockChangerGold,
////      Farmer,
////      GetOffMyLawn,
////      God,
////      LightningArrows,
////      MultiPlayerCommands,
////      NoRain,
////      PluginCommander,
////      Shock,
////      TeleportBows,
////      Thor,
////      TreeDelogger,
////      WorldEdit,
////      YellowBrickRoad,
////      ZombieApocalypse
//    )
//  )
//}
//
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
//


/*
from scalaLibPlugin old build.sbt
import AssemblyKeys._
artifact in (Compile, assembly) ~= { art => art.copy(`classifier` = Some("assembly")) }
addArtifact(artifact in (Compile, assembly), assembly)
// TODO: plugin doc says "To exclude some jar file, first consider using "provided" dependency."
// Figure out what that means...
excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {_.data.getName.contains("craftbukkit")}
}
 */

//
//trait ExamplesBuild extends Build with Common {
//  // a special example project...
//  //lazy val microExample = Project(id = "microexample", base = file("microexample"))
//
//  // a whole pile of example projects
//  lazy val Arena               = exampleProject("Arena")
//  lazy val BanArrows           = exampleProject("BanArrows")
//  lazy val BlockChanger        = exampleProject("BlockChanger")
//  lazy val BlockChangerGold    = exampleProject("BlockChangerGold")
//  lazy val Farmer              = exampleProject("Farmer")
//  lazy val GetOffMyLawn        = exampleProject("GetOffMyLawn")
//  lazy val God                 = exampleProject("God")
//  lazy val LightningArrows     = exampleProject("LightningArrows")
//  lazy val MultiPlayerCommands = exampleProject("MultiPlayerCommands")
//  lazy val NoRain              = exampleProject("NoRain")
//  lazy val PluginCommander     = exampleProject("PluginCommander")
//  lazy val Shock               = exampleProject("Shock")
//  lazy val Thor                = exampleProject("Thor")
//  lazy val TeleportBows        = exampleProject("TeleportBows")
//  lazy val TreeDelogger        = exampleProject("TreeDelogger")
//  lazy val WorldEdit           = exampleProject("WorldEdit")
//  lazy val YellowBrickRoad     = exampleProject("YellowBrickRoad")
//  lazy val ZombieApocalypse    = exampleProject("ZombieApocalypse")
//
//}

//def copyPluginToBukkitSettings(meta: Option[String]) = Seq(
//  // make publish local also copy jars to my bukkit server :)
//  publishLocal <<= (packagedArtifacts, publishLocal) map { case (r, _) =>
//    r collectFirst { case (Artifact(_,"jar","jar", m, _, _, name), f) if m == meta =>
//      println("copying " + f.name + " to bukkit server")
//      IO.copyFile(f, new File("bukkit/plugins/" + f.name))
//    }
//  }
//)

//def exampleProject(exampleProjectName: String, deps: sbt.ModuleID*) = {
//  val pluginClassname = "com.joshcough.minecraft.examples." + exampleProjectName
//  (project in file("examples/" + exampleProjectName))
//      .settings(
//        standardSettings,
//        named(exampleProjectName),
//        pluginYmlSettings(pluginClassname, "JoshCough"),
//        copyPluginToBukkitSettings(None),
//        libDeps(deps:_*)
//      )

//    id = exampleProjectName,
//    base = file(),
//    settings = join(
//
//    ),
//    dependencies = Seq(core)
//}

//  def pluginYmlSettings(pluginClassname: String, author: String): Seq[Setting[_]] = Seq[Setting[_]](
//    resourceGenerators in Compile <+=
//      (resourceManaged in Compile, streams, productDirectories in Compile, dependencyClasspath in Compile, version, compile in Compile, runner) map {
//        (dir, s, cp1, cp2, v, _, r) =>
//          Run.run(
//            "com.joshcough.minecraft.YMLGenerator", (Attributed.blankSeq(cp1) ++ cp2).map(_.data),
//            Seq(pluginClassname, author, v, dir.getAbsolutePath),
//            s.log)(r)
//          Seq(dir / "plugin.yml", dir / "config.yml")
//      }
//  )
