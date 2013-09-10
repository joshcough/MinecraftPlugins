package com.joshcough.minecraft.ermine

import com.clarifi.reporting.ermine._
import com.joshcough.minecraft.{ScalaPlugin}
import org.bukkit.command.{CommandSender, Command => BukkitCommand}
import org.bukkit.event.Listener
import org.bukkit.entity.Player
import com.clarifi.reporting.ermine.session.FilesystemReportsCache

class ErmineCraftPlugin extends ScalaPlugin {

  lazy val moduleName = this.name
  lazy val ermineModule = runErmine(moduleName, "plugin")
  lazy val listeners: List[Listener] =
    runErmine("Minecraft.Minecraft", "listeners", ermineModule).extract[List[Listener]]

  override def onEnable{ super.onEnable(); cache; listeners.foreach(registerListener) }

  override def onCommand(sender: CommandSender, cmd: BukkitCommand,
                         commandName: String, args: Array[String]) = {
    println(s"$name handling $commandName [${args.mkString(",")}]")
    //onCommand : ErminePlugin -> Player -> BukkitCommand -> String -> List String -> IO ()
    runIO(runErmine(
      "Minecraft.Minecraft",
      "onCommand",
      ermineModule,
      Prim(sender.asInstanceOf[Player]),
      Prim(cmd),
      Prim(commandName),
      Prim(args.toList)
    ))
    true
  }

  override def dependencies = List("ErmineLibPlugin")

  // code for running ermine
  val ermineDir = "/Users/joshcough/work/MinecraftPlugins/ermine/src/main/resources/modules"
  lazy val cache = new FilesystemReportsCache(List(ermineDir))
  def runErmine(module: String, function: String, args: Runtime*): Runtime =
    cache.getReport(module, function).toEither match {
      case Right(r) => r(args:_*)
      case Left(e)  => throw new RuntimeException(s"Couldn't find $moduleName.plugin", e)
    }
  def runIO(r: Runtime) = {
    val g = Global("IO.Unsafe","unsafePerformIO")
    cache.baseEnv.termNames.get(g).flatMap(cache.baseEnv.env.get) map { unsafePerformIO =>
      try unsafePerformIO(r) match {
        case Bottom(msg) => println(msg.apply.toString)
        case x           => x.extract[Any]
      }
      catch { case t: Throwable => println(t.getMessage) }
    }
  }
}

