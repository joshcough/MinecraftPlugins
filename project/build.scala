import sbt._
import Keys._
import java.io.File
import sbtassembly.Plugin._
import AssemblyKeys._

trait Common {

  val projectUrl = "https://github.com/joshcough/MinecraftPlugins"

  lazy val standardSettings = join(
    Defaults.defaultSettings,
    libDeps(
      "javax.servlet"      % "servlet-api" % "2.5"        % "provided->default",
      "org.scalacheck"    %% "scalacheck"  % "1.11.3"     % "test",
      "org.bukkit"         % "bukkit"      % "1.7.2-R0.3"
    ),
    Seq(
      organization := "com.joshcough",
      version := "0.3.4",
      scalaVersion := "2.11.6",
      crossScalaVersions := Seq("2.10.4", "2.11.6"),
      licenses <++= version(v => Seq("MIT" -> url(projectUrl + "/blob/%s/LICENSE".format(v)))),
      publishMavenStyle := true,
      resolvers ++= Seq(
        Resolver.jcenterRepo,
        Resolver.sonatypeRepo("snapshots"),
        "Bukkit" at "https://maven.elmakers.com/repository"
      ),
      traceLevel := 10
      //,logLevel := Level.Warn
    )
  )

  // the core plugin library
  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = join(
      standardSettings,
      copyPluginToBukkitSettings(None),
      named("scala-minecraft-plugin-api"),
      libDeps("org.scalacheck" %% "scalacheck" % "1.10.0" % "test")
    )
  )

  def copyPluginToBukkitSettings(meta: Option[String]) = Seq(
    // make publish local also copy jars to my bukkit server :)
    publishLocal <<= (packagedArtifacts, publishLocal) map { case (r, _) =>
      r collectFirst { case (Artifact(_,"jar","jar", m, _, _, name), f) if m == meta =>
        println("copying " + f.name + " to bukkit server")
        IO.copyFile(f, new File("bukkit/plugins/" + f.name))
      }
    }
  )

  def join(settings: Seq[Def.Setting[_]]*) = settings.flatten
  def named(pname: String) = Seq(name := pname)
  def libDeps(libDeps: sbt.ModuleID*) = Seq(libraryDependencies ++= libDeps)

  def pluginYmlSettings(pluginClassname: String, author: String): Seq[Setting[_]] = Seq[Setting[_]](
    resourceGenerators in Compile <+=
      (resourceManaged in Compile, streams, productDirectories in Compile, dependencyClasspath in Compile, version, compile in Compile, runner) map {
        (dir, s, cp1, cp2, v, _, r) =>
          Run.run(
            "com.joshcough.minecraft.YMLGenerator", (Attributed.blankSeq(cp1) ++ cp2).map(_.data),
            Seq(pluginClassname, author, v, dir.getAbsolutePath),
            s.log)(r)
          Seq(dir / "plugin.yml", dir / "config.yml")
      }
  )

}

object build extends Build
  with Common
  with ExamplesBuild {

  // this is the main project, that builds all subprojects.
  // it doesnt contain any code itself.
  lazy val all = Project(
    id = "all",
    base = file("."),
    settings = standardSettings,
    aggregate = Seq(
      scalaLibPlugin,
      core,
      mineLang,
      //microExample,
      Arena,
      BanArrows,
      BlockChanger,
      BlockChangerGold,
      Farmer,
      GetOffMyLawn,
      God,
      LightningArrows,
      MultiPlayerCommands,
      NoRain,
      PluginCommander,
      Shock,
      TeleportBows,
      Thor,
      TreeDelogger,
      WorldEdit,
      YellowBrickRoad,
      ZombieApocalypse
    )
  )

  // this is just a convenience project
  // for me to easily publish my most used plugins to my bukkit server.
  // > sbt 'project commonPlugins' publishLocal
  lazy val commonPlugins = Project(
    id = "commonPlugins",
    base = file(".commonPlugins"),
    settings = standardSettings,
    aggregate = Seq(
      scalaLibPlugin,
      core,
      MultiPlayerCommands,
      PluginCommander,
      WorldEdit
    )
  )

  // this project supplies the scala language classes.
  // it is needed in the bukkit plugins dir to run any scala plugins.
  lazy val scalaLibPlugin = Project(
    id = "scalaLibPlugin",
    base = file("scala-lib-plugin"),
    settings = join(
      standardSettings,
      named("scala-minecraft-scala-library"),
      assemblySettings,
      copyPluginToBukkitSettings(Some("assembly"))
    )
  )

  // minelang is a plugin that contains a language i wrote that is much like clojure
  // and allows people to easily write plugins without having to deploy lots of crap.
  // however, this has more or less been replaced by erminecraft.
  lazy val mineLang = Project(
    id = "mineLang",
    base = file("other/minelang"),
    settings = join(
      standardSettings,
      pluginYmlSettings("com.joshcough.minecraft.MineLangPlugin", "JoshCough"),
      named("MineLang"),
      libDeps(
        "jline" % "jline"   % "2.11",
        "org.clojure"    % "clojure" % "1.4.0"
      )
    ),
    dependencies = Seq(core)
  )
}

trait ExamplesBuild extends Build with Common {
  // a special example project...
  lazy val microExample = Project(id = "microexample", base = file("microexample"))

  // a whole pile of example projects
  lazy val Arena               = exampleProject("Arena")
  lazy val BanArrows           = exampleProject("BanArrows")
  lazy val BlockChanger        = exampleProject("BlockChanger")
  lazy val BlockChangerGold    = exampleProject("BlockChangerGold")
  lazy val Farmer              = exampleProject("Farmer")
  lazy val GetOffMyLawn        = exampleProject("GetOffMyLawn")
  lazy val God                 = exampleProject("God")
  lazy val LightningArrows     = exampleProject("LightningArrows")
  lazy val MultiPlayerCommands = exampleProject("MultiPlayerCommands")
  lazy val NoRain              = exampleProject("NoRain")
  lazy val PluginCommander     = exampleProject("PluginCommander")
  lazy val Shock               = exampleProject("Shock")
  lazy val Thor                = exampleProject("Thor")
  lazy val TeleportBows        = exampleProject("TeleportBows")
  lazy val TreeDelogger        = exampleProject("TreeDelogger")
  lazy val WorldEdit           = exampleProject("WorldEdit")
  lazy val YellowBrickRoad     = exampleProject("YellowBrickRoad")
  lazy val ZombieApocalypse    = exampleProject("ZombieApocalypse")

  def exampleProject(exampleProjectName: String, deps: sbt.ModuleID*) = {
    val pluginClassname = "com.joshcough.minecraft.examples." + exampleProjectName
    Project(
      id = exampleProjectName,
      base = file("examples/" + exampleProjectName),
      settings = join(
        standardSettings,
        named(exampleProjectName),
        pluginYmlSettings(pluginClassname, "JoshCough"),
        copyPluginToBukkitSettings(None),
        libDeps(deps:_*)
      ),
      dependencies = Seq(core)
    )
  }
}
