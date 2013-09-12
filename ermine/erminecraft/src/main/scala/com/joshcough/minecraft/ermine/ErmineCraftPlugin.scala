package com.joshcough.minecraft.ermine

import com.clarifi.reporting.ermine._
import com.joshcough.minecraft.ScalaPlugin
import org.bukkit.command.{CommandSender, Command => BukkitCommand}
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import com.clarifi.reporting.ermine.session.SessionEnv
import scalaz.{-\/, \/, \/-, Show}
import scalaz.std.string._
import scalaz.syntax.std._
import java.io.File
import com.clarifi.reporting.ermine.session.Session.{SourceFile, Filesystem, Resource}
import java.net.URL
import ReportsCache.ModuleExpr

class ErmineCraftPlugin extends ScalaPlugin {

  lazy val moduleName = this.name
  lazy val ermineModule = runErmine(moduleName, "plugin")()

  override def yml(author: String, version: String): String =
    runMC("yml")(ermineModule, name, getClass.getName, author, version).extract[String]

  override def onEnable{
    super.onEnable()
    cache
    // register all the listeners.
    runMC("listeners")(ermineModule).extract[List[Listener]].foreach(registerListener)
  }

  override def onCommand(sender: CommandSender, cmd: BukkitCommand, commandName: String, args: Array[String]) = {
    println(s"$name handling $commandName [${args.mkString(",")}]")
    runIO(runMC("onCommand")(ermineModule, sender.asInstanceOf[Player], cmd, commandName, args.toList))
    true
  }

  /** Load from Java classloader, rooted at `root`. */
  def classloader()(module: String): Option[SourceFile] = {
    val path = ("modules" :: module.split('.').toList).mkString("/") + ".e"
    Option(classOf[Resource].getClassLoader.getResource(path)).orElse(
    Option(classOf[ErmineCraft].getClassLoader.getResource(path))).orElse(
    Option(this.getClass.getClassLoader.getResource(path))).map(Resource(module, _))
  }

  // code for running ermine
  lazy val cache = new LoaderReportsCache[String](classloader()) {
    import ReportsCache.ModuleExpr
    type Report = ModuleExpr[String]
    lazy val loadPaths : List[String] = Nil
    override protected def initialEnv = {
      val e = new SessionEnv
      e.loadFile = classloader()
      e
    }
    override def preloads: List[String] = List("Minecraft.Minecraft")
    def showReport = Show[Report]
    protected def toME(r: Report) = r
  }

  def runErmine(module: String, expr: String)(args: AnyRef*): Runtime =
    cache.getReport(ModuleExpr(module, expr)).toEither match {
      case Right(r) => r(args.map(Prim(_)):_*)
      case Left(e)  => throw e
    }
  def runIO(r: Runtime) = {
    val g = Global("IO.Unsafe","unsafePerformIO")
    cache.baseEnv.termNames.get(g).flatMap(cache.baseEnv.env.get).map(_(r).extract[Any])
  }
  def runMC(function: String)(args: AnyRef*): Runtime = {
    val g = Global("Minecraft.Minecraft",function)
    cache.baseEnv.termNames.get(g).flatMap(cache.baseEnv.env.get).map(_(args.map(Prim(_)):_*)).get
  }
}

