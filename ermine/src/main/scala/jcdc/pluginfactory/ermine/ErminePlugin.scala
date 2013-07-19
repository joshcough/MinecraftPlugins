package jcdc.pluginfactory.ermine

import com.clarifi.reporting.ermine.{Prim, Fun}
import com.clarifi.reporting.ermine.session.FilesystemReportsCache
import jcdc.pluginfactory.CommandsPlugin
import org.bukkit.event.Listener

class ErminePlugin extends CommandsPlugin {

  val slurpList: Parser[List[String]] = slurp.map(_.split(" ").filterNot(_.isEmpty).toList)
  val ermineDir = "/Users/joshcough/work/MinecraftPlugins/ermine/src/main/resources"
  val cache = new FilesystemReportsCache(List(ermineDir))
  val module   = anyString named "module"
  val function = anyString named "command"
  val args     = slurpList

  def commands: List[Command] = List(
    Command(
      name = "runErmine",
      desc = "Run an Ermine module command",
      args = module ~ function ~ args){ case (p, m ~ f ~ as) =>
      cache.getReport(m, f).toEither match {
        case Right(f:Fun) => f(Prim(p))(Prim(as))
        case Left(e)      => p.sendError(e.getMessage)
        case bad          => p.sendError(s"something went wrong: $bad")
      }
    },
    Command(
      name = "registerErmineListener",
      desc = "Call to Ermine code to get a listener, and register it.",
      args = module ~ function){ case (p, m ~ f) =>
      cache.getReport(m, f).toEither match {
        case Right(p:Prim) => registerListener(p.extract[Listener])
        case Left(e)       => p.sendError(e.getMessage)
        case bad           => p.sendError(s"something went wrong: $bad")
      }
    }
  )
}

