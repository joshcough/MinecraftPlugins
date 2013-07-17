package jcdc.pluginfactory.ermine

import jcdc.pluginfactory.CommandsPlugin
import com.clarifi.reporting.ermine.{Prim, Fun}

class FirstErminePlugin extends CommandsPlugin {

  def commands: List[Command] = List(
    Command(name = "hello", desc = "Say hello."){ p =>
      com.clarifi.reporting.ermine.session.Evaluator().eval(ermineCode, "hello") match {
        case Right(f:Fun) => f(Prim(p))
        case Left(e) => p.sendError(e.getMessage)
        case bad => p.sendError(s"something went wrong: $bad")
      }
    }
  )

  val ermineCode =
    """
      |module Minecraft where
      |
      |import IO.Unsafe
      |
      |hello p = unsafePerformIO (sendMessage p "hello back")
      |
      |foreign
      |  data "org.bukkit.entity.Player" Player
      |  method "sendMessage" sendMessage : Player -> String -> IO ()
    """.stripMargin
}
