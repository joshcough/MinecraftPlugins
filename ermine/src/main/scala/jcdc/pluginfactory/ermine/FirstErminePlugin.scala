package jcdc.pluginfactory.ermine

import com.clarifi.reporting.ermine.{Prim, Fun}
import com.clarifi.reporting.ermine.session.ResourcesReportsCache
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.command.CommandSender

class FirstErminePlugin extends JavaPlugin {

  val cache = new ResourcesReportsCache(this.getClass.getClassLoader)

  override def onCommand(sender: CommandSender, cmd: org.bukkit.command.Command,
                         commandName: String, args: Array[String]): Boolean = {
    cache.getReport("Hello", "onCommand").toEither match {
      case Right(f:Fun) => f(Prim(sender))(Prim(commandName))(Prim(args.toList))
      case Left(e)      => sender.sendMessage(e.getMessage)
      case bad          => sender.sendMessage(s"something went wrong: $bad")
    }
    true
  }
}

//  def commands: List[Command] = List(
//    Command(name = "hello", desc = "Say hello."){ p =>
//      com.clarifi.reporting.ermine.session.Evaluator().eval(ermineCode, "hello") match {
//        case Right(f:Fun) => f(Prim(p))
//        case Left(e) => p.sendError(e.getMessage)
//        case bad => p.sendError(s"something went wrong: $bad")
//      }
//    }
//  )
