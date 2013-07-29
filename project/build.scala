import sbt._
import Project.Setting
import Keys._
import java.io.File

object build extends Build {
  type Sett = Project.Setting[_]

  lazy val standardSettings: Seq[Sett] = Defaults.defaultSettings ++ Seq[Sett](
    organization := "jcdc.pluginfactory",
    version := "0.3.1",
    scalaVersion := "2.10.2",
    libraryDependencies ++= Seq(
      "javax.servlet"      % "servlet-api"           % "2.5" % "provided->default",
      "org.bukkit"         % "craftbukkit"           % "1.5.2-R1.0",
      "org.clojure"        % "clojure"               % "1.4.0",
      "org.scalacheck"    %% "scalacheck"            % "1.10.0" % "test"
    ),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers ++= Seq(
      "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
      "Bukkit"             at "http://repo.bukkit.org/content/repositories/releases"
    ),
    // make publish local also copy jars to my bukkit server :)
    publishLocal <<= (packagedArtifacts, publishLocal) map { case (r, _) =>
      r collectFirst { case (Artifact(_,"jar","jar",_, _, _, _), f) =>
        println("copying " + f.name + " to bukkit server")
        IO.copyFile(f, new File("bukkit/plugins/" + f.name))
      }
    }
  )

  def pluginYmlSettings(author: String): Seq[Sett] = Seq[Sett](
    mappings in (Compile, packageBin) <+=
     (streams, name, productDirectories in Compile, dependencyClasspath in Compile, baseDirectory, version, runner) map {
       (s, name, cp1, cp2, bd, v, r) =>
      Run.run(
        "jcdc.pluginfactory.YMLGenerator", (Attributed.blankSeq(cp1) ++ cp2).map(_.data),
        Seq("jcdc.pluginfactory.examples." + name, author, v, bd.getAbsolutePath),
        s.log)(r)
      bd / "plugin.yml" -> "plugin.yml"
    }
  )

  lazy val scalaMinecraftPlugins = Project(
    id = "scalaMinecraftPlugins",
    base = file("."),
    settings = standardSettings,
    aggregate = Seq(
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

  lazy val coreJava = Project(
    id = "core-java",
    base = file("core-java"),
    settings = standardSettings ++ Seq[Sett](name := "Java Plugin API")
  )

  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = standardSettings ++ Seq[Sett](
      name := "Scala Plugin API",
      libraryDependencies ++= Seq(
        "org.clojure"        % "clojure"               % "1.4.0",
        "org.scalacheck"    %% "scalacheck"            % "1.10.0" % "test"
      )
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

  def exampleProject(exampleProjectName: String) = Project(
    id = exampleProjectName,
    base = file("examples/" + exampleProjectName),
    settings = standardSettings ++ pluginYmlSettings("Josh Cough") ++ Seq[Sett](
      name := exampleProjectName
    ),
    dependencies = Seq(core)
  )

  // ErmineCraft stuff below.
  val repl = InputKey[Unit]("repl", "Run the Ermine read-eval-print loop")
  val allUnmanagedResourceDirectories = SettingKey[Seq[File]]("all-unmanaged-resource-directories", "unmanaged-resource-directories, transitively.")
  /** Multiply a setting across Compile, Test, Runtime. */
  def compileTestRuntime[A](f: Configuration => Setting[A]): SettingsDefinition =
    seq(f(Compile), f(Test), f(Runtime))
  def fromGithub(githubUser: String, project: String, subProject: Option[String] = None, sha: Option[String] = None) = {
    // if a specific commit isnt supplied, just fetch the very latest commit.
    // 'sbt update' doesn't seem to get the latest even though this says that it should
    // http://stackoverflow.com/questions/8864317/how-do-i-refresh-updated-git-dependency-artifacts-in-sbt
    // so instead we have to go to github and get the latest version.
    val shaOrLatest = sha.getOrElse{
      val commitsUrl = "https://api.github.com/repos/"+githubUser+"/"+project+"/commits?sha=master"
      scala.io.Source.fromURL(commitsUrl).takeWhile(_ != ',').mkString.dropWhile(_!=':').drop(2).dropRight(1)
    }
    val projectUri = uri("https://github.com/"+githubUser+"/"+project+".git#" + shaOrLatest)
    subProject match {
      case None => RootProject(projectUri)
      case Some(sub) => ProjectRef(projectUri, sub)
    }
  }

  lazy val ermineLegacy  = fromGithub("ermine-language", "ermine-legacy")

  lazy val ermine = {
    val ermineFileSettings = Defaults.defaultSettings ++ Seq[SettingsDefinition](
      compileTestRuntime(sc => classpathConfiguration in sc := sc)
     ,mainClass in (Compile, run) := Some("com.clarifi.reporting.ermine.session.Console")
     ,compileTestRuntime(sco => allUnmanagedResourceDirectories in sco <<=
        (Defaults.inDependencies(unmanagedResourceDirectories in sco, _ => Seq.empty)
         (_.reverse.flatten)))
      // Usually, resources end up in the classpath by virtue of `compile'
      // copying them into target/scala-*/classes, and from there into jar.  But
      // we want in development p(1) I can edit an Ermine module in src
      // resources, hit reload, and it's seen, and (2) I can edit CSS/JS, reload
      // the HTML, and it's seen.  So we (harmlessly) patch the src resources
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
        //libraryDependencies ++= Seq("com.clarifi" %% "ermine-legacy" % "0.1"),
        fullRunInputTask(repl, Compile, "com.clarifi.reporting.ermine.session.Console")
      ) ++ ermineFileSettings,
      dependencies = Seq(core)
    )
  }.dependsOn(ermineLegacy)
}

// some crap left over from old build.sbt files.

//packageBin in Compile <<= (packageBin in Compile) dependsOn run
//Keys.`package` <<= (Keys.`package` in Compile) dependsOn run
//compile <<= (compile in Compile) map { result =>
//  println("in compile, something")
//  result
//}

//"org.squeryl"       %% "squeryl"               % "0.9.6-SNAPSHOT",
//"mysql"              % "mysql-connector-java"  % "5.1.10",
//"org.apache.derby"   % "derby"                 % "10.7.1.1",
//"ch.spacebase"       % "NPCCreatures"          % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar",
