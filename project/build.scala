import sbt._
import Project.Setting
import Keys._
import java.io.File
import com.joshcough.minecraft.Plugin._
import sbtassembly.Plugin._
import AssemblyKeys._

object build extends Build {
  type Sett = Project.Setting[_]

  lazy val standardSettings: Seq[Sett] = Defaults.defaultSettings ++ bintray.Plugin.bintraySettings ++ Seq[Sett](
    organization := "com.joshcough",
    version := "0.3.1",
    scalaVersion := "2.10.2",
    licenses <++= version(v =>
      Seq("MIT" -> url("https://github.com/joshcough/MinecraftPlugins/blob/%s/LICENSE".format(v)))
    ),
    publishMavenStyle := true,
    libraryDependencies ++= Seq(
      "javax.servlet"      % "servlet-api" % "2.5"        % "provided->default",
      "org.scalacheck"    %% "scalacheck"  % "1.10.0"     % "test",
      "org.bukkit"         % "craftbukkit" % "1.5.2-R1.0"
    ),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers ++= Seq(
      "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
    ),
    traceLevel := 10
    //,logLevel := Level.Warn
  )

  def copyPluginToBukkitSettings(meta: Option[String]): Seq[Sett] = Seq[Sett](
    // make publish local also copy jars to my bukkit server :)
    publishLocal <<= (packagedArtifacts, publishLocal) map { case (r, _) =>
      r collectFirst { case (Artifact(_,"jar","jar", m, _, _, name), f) if m == meta =>
        println("copying " + f.name + " to bukkit server")
        IO.copyFile(f, new File("bukkit/plugins/" + f.name))
      }
    }
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
      ermineLibPlugin,
      core,
      ermine,
      MultiPlayerCommands,
      PluginCommander,
      WorldEdit)
  )

  // this is the main project, that builds all subprojects.
  // it doesnt contain any code itself.
  lazy val scalaMinecraftPlugins = Project(
    id = "scalaMinecraftPlugins",
    base = file("."),
    settings = standardSettings,
    aggregate = Seq(
      scalaLibPlugin,
      ermineLibPlugin,
      //mineLang,
      coreJava,
      core,
      ermine,
      examplesJava,
      microExample,
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
      Thor,
      TreeDelogger,
      WorldEdit,
      YellowBrickRoad,
      ZombieApocalypse)
  )

  // this project supplies the scala language classes.
  // it is needed in the bukkit plugins dir to run any scala plugins.
  lazy val scalaLibPlugin = Project(
    id = "scalaLibPlugin",
    base = file("scala/scala-lib-plugin"),
    settings = standardSettings ++ assemblySettings ++ copyPluginToBukkitSettings(Some("assembly")) ++ Seq[Sett](
      name := "scala-minecraft-scala-library"
    )
  )

  // the core plugin library
  lazy val core = Project(
    id = "core",
    base = file("scala/core"),
    settings = standardSettings ++ copyPluginToBukkitSettings(None) ++ Seq[Sett](
      name := "scala-minecraft-plugin-api",
      libraryDependencies ++= Seq("org.scalacheck" %% "scalacheck" % "1.10.0" % "test")
    )
  )

  // a special example project...
  lazy val microExample = Project(id = "microexample", base = file("scala/microexample"))

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
  lazy val TreeDelogger        = exampleProject("TreeDelogger")
  lazy val WorldEdit           = exampleProject("WorldEdit")
  lazy val YellowBrickRoad     = exampleProject("YellowBrickRoad")
  lazy val ZombieApocalypse    = exampleProject("ZombieApocalypse")

  def exampleProject(exampleProjectName: String) = {
    val pluginClassname = "com.joshcough.minecraft.examples." + exampleProjectName
    Project(
      id = exampleProjectName,
      base = file("scala/examples/" + exampleProjectName),
      settings = standardSettings ++ pluginYmlSettings(pluginClassname, "JoshCough") ++ copyPluginToBukkitSettings(None) ++ Seq[Sett](
        name := exampleProjectName
      ),
      dependencies = Seq(core)
    )
  }

  // ErmineCraft stuff below.
  val repl = InputKey[Unit]("repl", "Run the Ermine read-eval-print loop")
  val allUnmanagedResourceDirectories = SettingKey[Seq[File]]("all-unmanaged-resource-directories", "unmanaged-resource-directories, transitively.")
  /** Multiply a setting across Compile, Test, Runtime. */
  def compileTestRuntime[A](f: Configuration => Setting[A]): SettingsDefinition =
    seq(f(Compile), f(Test), f(Runtime))

  lazy val ermine = {
    val pluginClassname = "com.joshcough.minecraft.ermine.ErmineCraft"
    val ermineFileSettings = Defaults.defaultSettings ++ Seq[SettingsDefinition](
      compileTestRuntime(sc => classpathConfiguration in sc := sc)
     ,mainClass in (Compile, run) := Some("com.clarifi.reporting.ermine.session.Console")
     ,compileTestRuntime(sco => allUnmanagedResourceDirectories in sco <<=
        Defaults.inDependencies(unmanagedResourceDirectories in sco, _ => Seq.empty)
         (_.reverse.flatten))
      // Usually, resources end up in the classpath by virtue of `compile'
      // copying them into target/scala-*/classes, and from there into jar.  But
      // we want in development p(1) I can edit an Ermine module in src
      // resources, hit reload, and it's seen. So we (harmlessly) patch the src resources
      // dirs in *before* the classes dirs, so they will win in the classloader
      // lookup.
     ,compileTestRuntime(sco =>
        fullClasspath in sco <<= (allUnmanagedResourceDirectories in sco, fullClasspath in sco) map {
          (urd, fc) => Attributed.blankSeq(urd) ++ fc
      })
    ) flatMap (_.settings)
    Project(
      id = "erminecraft",
      base = file("ermine/erminecraft"),
      settings = standardSettings ++ copyPluginToBukkitSettings(None) ++ pluginYmlSettings(pluginClassname, "JoshCough") ++ Seq[Sett](
        name := "erminecraft-plugin-api",
        libraryDependencies ++= Seq("com.clarifi" %% "ermine-legacy" % "0.1"),
        fullRunInputTask(repl, Compile, "com.clarifi.reporting.ermine.session.Console")
      ) ++ ermineFileSettings,
      dependencies = Seq(core)
    )
  }

  // this project supplies the ermine language classes, and classes for all of ermine's dependencies.
  // it is needed in the bukkit plugins dir to run any ermine plugins.
  lazy val ermineLibPlugin = Project(
    id = "ermineLibPlugin",
    base = file("ermine/ermine-lib-plugin"),
    settings = standardSettings ++ assemblySettings ++ copyPluginToBukkitSettings(Some("assembly")) ++ Seq[Sett](
      name := "erminecraft-ermine-library",
      libraryDependencies ++= Seq(
        "com.clarifi" %% "ermine-legacy"     % "0.1",
        "org.scalaz"  %% "scalaz-core"       % "7.0.2",
        "org.scalaz"  %% "scalaz-concurrent" % "7.0.2",
        "org.scalaz"  %% "scalaz-effect"     % "7.0.2",
        "org.scalaz"  %% "scalaz-iterv"      % "7.0.2",
        "log4j"       %  "log4j"             % "1.2.14"
      )
    )
  )

  // two relatively unimportant projects
  // that show how to do all this scala stuff in java.
  // or, how the bukkit api should have been written (in java).
  // this backports most of my interesting features from scala to java.
  lazy val coreJava = Project(
    id = "core-java",
    base = file("other/core-java"),
    settings = standardSettings ++ Seq[Sett](name := "java-minecraft-plugin-api")
  )

  lazy val examplesJava = Project(
    id = "examplesJava",
    base = file("other/examples-java"),
    settings = standardSettings ++ Seq[Sett](name := "JCDC Plugin Factory Java Examples"),
    dependencies = Seq(coreJava)
  )

  // minelang is a plugin that contains a language i wrote that is much like clojure
  // and allows people to easily write plugins without having to deploy lots of crap.
  // however, this has more or less been replaced by erminecraft.
  lazy val mineLang = Project(
    id = "mineLang",
    base = file("other/minelang"),
    settings = standardSettings ++ pluginYmlSettings("com.joshcough.minecraft.MineLangPlugin", "JoshCough") ++ Seq[Sett](
      name := "MineLang",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "jline"   % "2.10.2",
        "org.clojure"    % "clojure" % "1.4.0"
      )
    ),
    dependencies = Seq(core)
  )

  lazy val netlogoPlugin = Project(
    id = "netLogoPlugin",
    base = file("other/netlogo"),
    settings =
      standardSettings ++
      copyPluginToBukkitSettings(None) ++
      pluginYmlSettings("com.joshcough.minecraft.NetLogoPlugin", "JoshCough") ++ Seq[Sett](
      resolvers ++= Seq(
        "remoteentities-repo" at "http://repo.infinityblade.de/remoteentities/releases"
      ),
      libraryDependencies ++= Seq(
        "org.nlogo" % "NetLogoHeadless"  % "5.1.0-M2" from "http://ccl.northwestern.edu/netlogo/5.1.0-M2/NetLogoHeadless.jar",
        "de.kumpelblase2" % "remoteentities" % "1.6" from "http://dev.bukkit.org/media/files/700/586/remoteentities-1.6.jar"
      )
    ),
    dependencies = Seq(core)
  )

  lazy val netlogoLibPlugin = Project(
    id = "netLogoLibPlugin",
    base = file("other/netlogo-lib-plugin"),
    settings = standardSettings ++ assemblySettings ++ copyPluginToBukkitSettings(Some("assembly")) ++ Seq[Sett](
      name := "netlogo-lib-plugin",
      libraryDependencies ++= Seq(
        "org.nlogo" % "NetLogoHeadless"  % "5.1.0-M2" from "http://ccl.northwestern.edu/netlogo/5.1.0-M2/NetLogoHeadless.jar",
        "asm" % "asm-all" % "3.3.1",
        "org.picocontainer" % "picocontainer" % "2.13.6"
      )
    )
  )
}

// some crap left over from old build.sbt files.

//packageBin in Compile <<= (packageBin in Compile) dependsOn run
//Keys.`package` <<= (Keys.`package` in Compile) dependsOn run
//compile <<= (compile in Compile) map { result =>
//  println("in compile, something")
//  result
//}

//"org.clojure"        % "clojure"               % "1.4.0",
//"org.squeryl"       %% "squeryl"               % "0.9.6-SNAPSHOT",
//"mysql"              % "mysql-connector-java"  % "5.1.10",
//"org.apache.derby"   % "derby"                 % "10.7.1.1",
//"ch.spacebase"       % "NPCCreatures"          % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar",
