import sbt._
import Project.Setting
import Keys._
import java.io.File
//import jcdc.pluginfactory.Plugin._
import sbtassembly.Plugin._
import AssemblyKeys._

object build extends Build {
  type Sett = Project.Setting[_]

  lazy val standardSettings: Seq[Sett] = Defaults.defaultSettings ++ bintray.Plugin.bintraySettings ++ Seq[Sett](
    organization := "jcdc.pluginfactory",
    version := "0.3.1",
    scalaVersion := "2.10.2",
    licenses <++= (version)(v => Seq("MIT" -> url(
      "https://github.com/joshcough/MinecraftPlugins/blob/%s/LICENSE".format(v)))),
    publishMavenStyle := true,
    libraryDependencies ++= Seq(
      "javax.servlet"      % "servlet-api"           % "2.5" % "provided->default",
      "org.bukkit"         % "craftbukkit"           % "1.5.2-R1.0",
      "org.scalacheck"    %% "scalacheck"            % "1.10.0" % "test"
    ),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers ++= Seq(
      "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases",
      "joshcough bintray maven" at "http://dl.bintray.com/joshcough/maven/",
      "runarorama bintray maven" at "http://dl.bintray.com/runarorama/maven/"
    ),
    // make publish local also copy jars to my bukkit server :)
    publishLocal <<= (packagedArtifacts, publishLocal) map { case (r, _) =>
      r collectFirst { case (Artifact(_,"jar","jar",_, _, _, _), f) =>
        println("copying " + f.name + " to bukkit server")
        IO.copyFile(f, new File("bukkit/plugins/" + f.name))
      }
    },
    traceLevel := 10
    //,logLevel := Level.Warn
  )

  def pluginYmlSettings(pluginClassname: String, author: String): Seq[Sett] = Seq[Sett](
    resourceGenerators in Compile <+=
      (resourceManaged in Compile, streams, productDirectories in Compile, dependencyClasspath in Compile, version, compile in Compile, runner) map {
        (dir, s, cp1, cp2, v, _, r) =>
          Run.run(
            "jcdc.pluginfactory.YMLGenerator", (Attributed.blankSeq(cp1) ++ cp2).map(_.data),
            Seq(pluginClassname, author, v, dir.getAbsolutePath),
            s.log)(r)
          Seq(dir / "plugin.yml", dir / "config.yml")
      }
  )

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

  lazy val scalaLibPlugin = Project(
    id = "scalaLibPlugin",
    base = file("scala-lib-plugin"),
    settings = standardSettings ++ assemblySettings ++ Seq[Sett](
      name := "Scala Library Plugin"
    )
  )

  lazy val ermineLibPlugin = Project(
    id = "ermineLibPlugin",
    base = file("ermine-lib-plugin"),
    settings = standardSettings ++ assemblySettings ++ Seq[Sett](
      name := "Ermine Library Plugin",
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

  lazy val coreJava = Project(
    id = "core-java",
    base = file("core-java"),
    settings = standardSettings ++ Seq[Sett](name := "Java Minecraft Plugin API")
  )

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = standardSettings ++ Seq[Sett](
      name := "Scala Minecraft Plugin API",
      libraryDependencies ++= Seq("org.scalacheck" %% "scalacheck" % "1.10.0" % "test")
    ),
    dependencies = Seq(coreJava)
  )

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

  lazy val examplesJava = Project(
    id = "examplesJava",
    base = file("examples-java"),
    settings = standardSettings ++ Seq[Sett](
      name := "JCDC Plugin Factory Java Examples"
    ),
    dependencies = Seq(coreJava)
  )

  lazy val mineLang = Project(
    id = "mineLang",
    base = file("minelang"),
    settings = standardSettings ++ pluginYmlSettings("jcdc.pluginfactory.MineLangPlugin", "JoshCough") ++ Seq[Sett](
      name := "MineLang",
      libraryDependencies ++= Seq(
        "org.scala-lang"     % "jline"                    % "2.10.2",
        "org.clojure"        % "clojure"                  % "1.4.0"
      )
    ),
    dependencies = Seq(core)
  )

  def exampleProject(exampleProjectName: String) = {
    val pluginClassname = "jcdc.pluginfactory.examples." + exampleProjectName
    Project(
      id = exampleProjectName,
      base = file("examples/" + exampleProjectName),
      settings = standardSettings ++ pluginYmlSettings(pluginClassname, "JoshCough") ++ Seq[Sett](
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
    val pluginClassname = "jcdc.pluginfactory.ermine.ErminePlugin"
    val ermineFileSettings = Defaults.defaultSettings ++ pluginYmlSettings(pluginClassname, "JoshCough") ++ Seq[SettingsDefinition](
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
        fullClasspath in sco <<= (allUnmanagedResourceDirectories in sco,
                                  fullClasspath in sco) map {
          (urd, fc) => Attributed.blankSeq(urd) ++ fc
      })
    ) flatMap (_.settings)
    Project(
      id = "ermine-plugins",
      base = file("ermine"),
      settings = standardSettings ++ Seq[Sett](
        name := "Ermine Plugin API",
        libraryDependencies ++= Seq("com.clarifi" %% "ermine-legacy" % "0.1"),
        fullRunInputTask(repl, Compile, "com.clarifi.reporting.ermine.session.Console")
      ) ++ ermineFileSettings,
      dependencies = Seq(core)
    )
  }
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
