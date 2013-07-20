package jcdc.pluginfactory.ermine

import com.clarifi.reporting.ermine._
import com.clarifi.reporting.ermine.session.FilesystemReportsCache
import jcdc.pluginfactory.CommandsPlugin
import org.bukkit.event.Listener
import com.clarifi.reporting.ermine.Global
import org.bukkit.entity.Player

class ErminePlugin extends CommandsPlugin {

  // code for running ermine
  val ermineDir = "/Users/joshcough/work/MinecraftPlugins/ermine/src/main/resources"
  val cache = new FilesystemReportsCache(List(ermineDir))
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
  def runErmine(p: Player, module: String, function: String)(pf: PartialFunction[Runtime, Unit]) =
    cache.getReport(module, function).toEither match {
      case Right(r) => pf.applyOrElse(r, (r:Runtime) => p.sendError(s"something went wrong: $r"))
      case Left(e)  => p.sendError(e.getMessage)
    }

  // parsers
  val module   = anyString.filter(_.head.isUpper) named "module"
  val function = anyString.filter(_.head.isLower) named "function"

  def commands: List[Command] = List(
    Command(
      name = "runErmine",
      desc = "Run an Ermine module command",
      args = module ~ function ~ remainingArgs){
        case (p, m ~ f ~ as) => runErmine(p, m, f) { case f:Fun => runIO(f(Prim(p))(Prim(as))) }
      },
    Command(
      name = "registerErmineListener",
      desc = "Call to Ermine code to get a listener, and register it.",
      args = module ~ function){
        case (p, m ~ f) => runErmine(p, m, f) { case p:Prim => registerListener(p.extract[Listener]) }
      }
  )
}

