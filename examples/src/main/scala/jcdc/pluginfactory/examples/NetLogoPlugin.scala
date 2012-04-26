package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{NPCPlugin, CommandsPlugin}
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.nlogo.headless.HeadlessWorkspace
import org.bukkit.entity.Player

class NetLogoPlugin extends CommandsPlugin with NPCPlugin {

  /**
   * This map holds the state of the NetLogo world on the last tick (or the last call to go).
   * It gets populated during nl:setup, and then gets updated after each call to go.
   * The Long key is the turtle id.
   */
  var turtles = Map[Long, CraftPlayer]()
  var workspace: Option[HeadlessWorkspace] = None

  val commands = List(
    Command("load", "Load a model", args(existingFile) { case p ~ model =>
      // implicitly start over.
      dispose()
      workspace = Some(HeadlessWorkspace.newInstance)
      // model must be a file on the server....
      // maybe it could somehow be a url that we pull down?
      workspace.foreach(_.open(model.getAbsolutePath))
    }),
    Command("setup", "Call the setup proc.",    noArgs { callProc(_, "setup") }),
    Command("go",    "Call the go proc once.",  noArgs { callProc(_, "go") }),
    Command("call",  "Call a NetLogo proc.",    args(anyString) {
      case p ~ proc => callProc(p, proc)
    }),
    Command("loop",  "Call go until it is finished.", noArgs { p =>
      // do the same thing as go, but in a loop.
      // we probably have to start a new thread here to do that
      // i need to be able to detect the stop condition
    }),
    Command("dispose", "Start over.", noArgs{ p =>
      // todo: what if we are running the go loop in a new thread here?
      // we probably should shut it down...
      dispose()
    }),
    Command("report", "Report something...", args(anyString*){ case p ~ reporter =>
      usingWorkspace(p)(ws => p ! (ws.report(reporter.mkString(" ")).toString))
    })
  )

  def callProc(p: Player, proc:String) = usingWorkspace(p)(_.command(proc))

  def usingWorkspace(p: Player)(f: HeadlessWorkspace => Unit) =
    workspace.fold(p ! "call nl:load first!"){ ws =>
      f(ws)
      updateTurtlesInMinecraftWorld(p)
    }

  def dispose(){
    turtles.values.foreach(_.die)
    workspace.foreach(_.dispose())
    workspace = None
  }

  // if new turtles are born, create them.
  // if turtles die, kill them?
  // update the positions of all living turtles
  // update the turtles map so we know who was alive last tick.
  // todo: this might be able to be folded into usingWorkspace. nbd though.
  def updateTurtlesInMinecraftWorld(p:Player): Unit = {

  }
}
