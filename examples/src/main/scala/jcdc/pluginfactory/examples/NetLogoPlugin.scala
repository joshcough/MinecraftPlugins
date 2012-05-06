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
  // whether or not to check all the patches for updates each tick
  var updatePatches = true
  var setupComplete = false

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
    Command("open", "Open a model", args(existingFile ~ boolOrTrue) {
      case p ~ (model ~ updatePatches) =>
        openModel(p, model, updatePatches)
        // call setup on load, because that just makes sense.
        callProc(p, "setup")
    }),
    Command("open-no-setup", "Open a model without setting up.", args(existingFile ~ boolOrTrue) {
      case p ~ (model ~ updatePatches) => openModel(p, model, updatePatches)
    }),
    Command("setup", "Call the setup proc.",    noArgs { callProc(_, "setup") }),
    Command("go",    "Call the go proc once.",  noArgs { go(_) }),
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

  def openModel(p: Player, model: java.io.File, updatePatches: Boolean){
    // implicitly start over.
    dispose()
    workspace = Some(HeadlessWorkspace.newInstance)
    this.updatePatches = updatePatches
    // model must be a file on the server....
    // maybe it could somehow be a url that we pull down?
    workspace.get.open(model.getAbsolutePath)
    p ! ("loaded " + model)
  }

  def callProc(p: Player, proc:String) = usingWorkspace(p)(_.command(proc))
  def go(p: Player): Unit = callProc(p, "go")
  def setup(p: Player): Unit = {
    callProc(p, "setup")
    setupComplete = true
  }

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

    /**
     * update minecraft blocks based on netlogo patch colors
     * currently, this is always done at setup, but then can be skipped.
     */
    if (updatePatches || ! setupComplete){
      val patches = ws.world.patches.toLogoList.scalaIterator.collect{case p: Patch => p}
      patches.foreach(updateBlockColorMaybe(ws, _, world))
    }
  }

  /**
   * for reference, see:
   * https://github.com/haveric/Wool-Trees/blob/master/src/haveric/woolTrees/Commands.java
   * http://ccl.northwestern.edu/netlogo/docs/programming.html#colors
   **/
  import Color._
  def colors(netLogoColor: Double): MaterialAndData = netLogoColor match {
    case x if x % 10 < 2           => BLACK.wool
    case x if x >= 2   && x <= 5   => GREY.wool
    case x if x >  5   && x <  8   => LIGHT_GREY.wool
    case x if x >= 8   && x <  12  => WHITE.wool
    case x if x >= 12  && x <= 18  => RED.wool
    case x if x >= 22  && x <= 28  => ORANGE.wool
    case x if x >= 32  && x <= 38  => DIRT  // choosing not to use brown, but dirt instead
    case x if x >= 42  && x <= 48  => YELLOW.wool
    case x if x >= 52  && x <= 58  => GRASS // choosing not to use green, but grass instead
    case x if x >= 62  && x <= 68  => LIGHT_GREEN.wool // lime in NetLogo
    // there doesnt seem to be a turquoise in MineCraft...using light green instead.
    case x if x >= 72  && x <= 78  => LIGHT_GREEN.wool
    case x if x >= 82  && x <= 88  => CYAN.wool
    case x if x >= 92  && x <= 98  => LIGHT_BLUE.wool // sky in NetLogo
    case x if x >= 102 && x <= 108 => BLUE.wool
    case x if x >= 112 && x <= 118 => VIOLET.wool
    case x if x >= 122 && x <= 128 => MAGENTA.wool
    case x if x >= 132 && x <= 138 => PINK.wool
    case x if x % 10 > 8           => WHITE.wool
    // this could only happen if we are given a double outside the color range
    // i could imagine a few scenarios where that would happen, but it doesn't much matter.
    case _    => WHITE.wool
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
    Cube(lowBlock, topBlock).blocks.foreach(b => colors(p.pcolorDouble).update(b))
    topBlock.blocksAbove.takeWhile(_ isNot AIR).foreach(_ changeTo AIR)
  }

  /**
   * Some special minecraft-netlogo integration code.
   * If t happens to have a 'holding' variable, and it happens to be a double
   * then update what the player has in his hands.
   */
  def updateItemInHand(npc: NPC, ws: HeadlessWorkspace, t:Turtle): Unit = tryO{
    val player =  entities(t.id).asInstanceOf[CraftPlayer]
    player.setItemInHand(colors(agentVar(ws, t, "holding").asInstanceOf[Double]))
  }

  def agentVar(ws: HeadlessWorkspace, a:Agent, varName:String): Option[Object] = {
    val index = ws.world.indexOfVariable(a, varName)
    if (index == -1) None else Some(a.variables(index))
  }
}
