package jcdc.pluginfactory.examples

import ch.spacebase.npccreatures.npcs.entity.NPC
import jcdc.pluginfactory.{Cube, ListenersPlugin, NPCPlugin, CommandsPlugin}
import org.nlogo.agent.{Agent, Patch, Turtle}
import org.nlogo.headless.HeadlessWorkspace
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Material, World}
import Material._

object NetLogoEvent{
  implicit def toJ(e: NetLogoEvent)  = new NetLogoEventJ(e)
  implicit def toS(e: NetLogoEventJ) = e.event
}
case class NetLogoEvent(player:Player, ticksRemaining: Int, sleepTime: Option[Long]){
  def tick = this.copy(ticksRemaining = ticksRemaining - 1)
}
import NetLogoEvent._

class NetLogoPlugin extends CommandsPlugin with ListenersPlugin with NPCPlugin {

  /**
   * This map holds the state of the NetLogo world on the last tick (or the last call to go).
   * It gets populated during nl:setup, and then gets updated after each call to go.
   * The Long key is the turtle id.
   */
  val entities = collection.mutable.Map[Long, NPC]()
  var workspace: Option[HeadlessWorkspace] = None

  object NetLogoEventListener extends Listener {
    @EventHandler def on(e:NetLogoEventJ): Unit =
      if(e.ticksRemaining > 0) {
        go(e.player)
        e.sleepTime.foreach(Thread.sleep)
        fire(e.tick)
      }
  }

  val listeners = List(NetLogoEventListener)

  val commands = List(
    Command("open", "Open a model", args(existingFile) { case p ~ model =>
      openModel(p, model)
      // call setup on load, because that just makes sense.
      callProc(p, "setup")
    }),
    Command("open-no-setup", "Open a model without setting up.", args(existingFile) {
      case p ~ model => openModel(p, model)
    }),
    Command("setup", "Call the setup proc.",    noArgs { callProc(_, "setup") }),
    Command("go",    "Call the go proc once.",  noArgs { callProc(_, "go") }),
    Command("call",  "Call a NetLogo proc.",    args(anyString+) {
      case p ~ proc => callProc(p, proc.mkString(" "))
    }),
    Command("loop",  "Call go until it is finished.", args(num ~ opt(long)) {
      case p ~ (n ~ sleepTime) =>
        new Thread(new Runnable() { def run() {
          for (_ <- 0 to n) { callProc(p, "go"); sleepTime.foreach(Thread.sleep) }
          p ! ("looped " + n + " times.")
        }}).start()
      }),
    Command("loop-new",  "Call go until it is finished.", args(num ~ opt(long)) {
      case p ~ (n ~ sleepTime) => fire(NetLogoEvent(p, n, sleepTime))
    }),
    Command("dispose", "Start over.", noArgs{ p =>
      // todo: what if we are running the go loop in a new thread here?
      // we probably should shut it down...
      dispose()
    }),
    Command("report", "Report something...", args(anyString+){ case p ~ reporter =>
      usingWorkspace(p)(ws => p ! (ws.report(reporter.mkString(" ")).toString))
    }),
    Command("count-entities", "Show the number of entities", noArgs{ p =>
      p ! (entities.size + " entities.")
    })
  )

  def openModel(p: Player, model: java.io.File){
    // implicitly start over.
    dispose()
    workspace = Some(HeadlessWorkspace.newInstance)
    // model must be a file on the server....
    // maybe it could somehow be a url that we pull down?
    workspace.get.open(model.getAbsolutePath)
    p ! ("loaded " + model)
  }

  def callProc(p: Player, proc:String) = usingWorkspace(p)(_.command(proc))
  def go(p: Player): Unit = callProc(p, "go")

  def usingWorkspace(p: Player)(f: HeadlessWorkspace => Unit) =
    workspace.fold(p ! "call open first!"){ ws => f(ws); update(p.world, ws) }

  def dispose(){
    entities.values.foreach(despawn)
    entities.clear()
    workspace.foreach(_.dispose())
    workspace = None
  }

  /**
   * Update the Minecraft world based on the NetLogo world.
   */
  def update(world: World, ws: HeadlessWorkspace): Unit = {
    val turtles: Map[Long, Turtle] =
      ws.world.turtles.toLogoList.scalaIterator.collect{case t: Turtle => t}.map(t => (t.id, t)).toMap

    turtles.foreach{ case (id, t) =>
      val loc = world(t.xcor, 4d, t.ycor).loc
      /**
       * if new turtles are born in netlogo, create an npc for them in minecraft.
       * npc plugin handles the breedname automatically, spawning a human if the breedname is unknown.
       */
      if (! entities.contains(id)) entities += (id -> spawn(id, loc, t.getBreed.printName))
      /* update the positions of existing npcs based on their turtles position. */
      else entities(id).teleport(loc)
      updateItemInHand(entities(id), ws, t)
    }

    /* if turtles die in netlogo, kill them in minecraft */
    val deadTurtles = entities.filter{ case (id, _) => ! turtles.contains(id) }
    deadTurtles.foreach{ case (id, npc) => despawn(npc); entities.remove(id) }

    /* update minecraft blocks based on netlogo patch colors */
    val patches: Iterator[Patch] = ws.world.patches.toLogoList.scalaIterator.collect{case p: Patch => p}
    patches.foreach(updateBlockColorMaybe(ws, _, world))
  }

  /**
   * for reference, see:
   * https://github.com/haveric/Wool-Trees/blob/master/src/haveric/woolTrees/Commands.java
   * http://ccl.northwestern.edu/netlogo/docs/programming.html#colors
   *
   * Yet unhandled Minecraft colors: lightblue -> 3, lightgrey -> 8
   **/
  import Color._
  def colors(netLogoColor: Double, default: MaterialAndData): MaterialAndData = netLogoColor match {
    case 0d   => BLACK.wool
    case 5d   => GREY.wool
    case 9.9  => WHITE.wool
    case 15d  => RED.wool
    case 25d  => ORANGE.wool
    case 35d  => DIRT // brown
    case 45d  => YELLOW.wool
    case 55d  => GRASS // green
    case 65d  => LIGHT_GREEN.wool // lime in NetLogo
    // 75d    => ?? turquoise
    case 85d  => CYAN.wool
    // 95d    => ?? sky
    case 105d => BLUE.wool
    case 115d => VIOLET.wool
    case 125d => MAGENTA.wool
    case 135d => PINK.wool
    case _    => default
  }

  /**
   * A couple of things are going on here...
   *
   * In the simple case, the patch is just on the ground, and we update its
   * color, if its color is a valid minecraft color (from the color map above)
   *
   * However, if the patch has a z variable, then we do some other crap
   * // TODO: explain that other crap
   */
  def updateBlockColorMaybe(ws: HeadlessWorkspace, p: Patch, w: World): Unit = {
    val lowZ = 3
    val topZ = lowZ + tryO(agentVar(ws, p, "z").asInstanceOf[Double].toInt).getOrElse(0)
    val (lowBlock,topBlock) = (w(p.pxcor, lowZ, p.pycor), w(p.pxcor, topZ, p.pycor))
    Cube(lowBlock, topBlock).blocks.foreach(b => colors(p.pcolorDouble, b).update(b))
    topBlock.blocksAbove.takeWhile(_ isNot AIR).foreach(_ changeTo AIR)
  }

  /**
   * Some special minecraft-netlogo integration code.
   * If t happens to have a 'holding' variable, and it happens to be a double
   * then update what the player has in his hands.
   */
  def updateItemInHand(npc: NPC, ws: HeadlessWorkspace, t:Turtle): Unit = tryO{
    val player =  entities(t.id).asInstanceOf[CraftPlayer]
    player.setItemInHand(colors(agentVar(ws, t, "holding").asInstanceOf[Double], player.holding))
  }

  def agentVar(ws: HeadlessWorkspace, a:Agent, varName:String): Option[Object] = {
    val index = ws.world.indexOfVariable(a, varName)
    if (index == -1) None else Some(a.variables(index))
  }
}
