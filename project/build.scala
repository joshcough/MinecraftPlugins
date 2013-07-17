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
    )
  )

  lazy val scalaMinecraftPlugins = Project(
    id = "scalaMinecraftPlugins",
    base = file("."),
    settings = standardSettings,
    aggregate = Seq(
      coreJava,
      core,
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

  lazy val ermine = Project(
    id = "ermine-plugins",
    base = file("ermine"),
    settings = standardSettings ++ Seq[Sett](
      name := "Ermine Plugin API",
      libraryDependencies ++= Seq(
        "com.clarifi" %% "ermine-legacy" % "0.1"
      )
    ),
    dependencies = Seq(core)
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
    settings = standardSettings ++ Seq[Sett](name := exampleProjectName),
    dependencies = Seq(core)
  )
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
