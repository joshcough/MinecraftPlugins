package jcdc.pluginfactory.examples

import org.nlogo.headless.HeadlessWorkspace
import org.bukkit.entity.Player
import ch.spacebase.npccreatures.npcs.entity.NPC
import org.nlogo.agent.{Patch, Turtle}
import org.bukkit.{Material, World, Location}
import Material._
import jcdc.pluginfactory.{ListenersPlugin, NPCPlugin, CommandsPlugin}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import net.minecraft.server.EntityPlayer
import org.bukkit.craftbukkit.entity.CraftPlayer

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
    workspace.fold(p ! "call open first!"){ ws => f(ws); update(p, ws) }

  def dispose(){
    entities.values.foreach(despawn)
    entities.clear()
    workspace.foreach(_.dispose())
    workspace = None
  }

  // update the turtles map so we know who was alive last tick.
  // todo: this might be able to be folded into usingWorkspace. nbd though.
  def update(p:Player, ws: HeadlessWorkspace): Unit = {
    val turtles: Map[Long, Turtle] =
      ws.world.turtles.toLogoList.scalaIterator.collect{case t: Turtle => t}.map(t => (t.id, t)).toMap

    turtles.foreach{ case (id, t) =>
      val loc = p.world(t.xcor, 4d, t.ycor).loc
      // if new turtles are born, create them.
      if (! entities.contains(id)) entities += (id -> npc(id, loc, t.getBreed.printName))
      // update the positions of existing turtles
      else entities(id).teleport(loc)
      updateHolding(entities(id), t)
    }
    // if turtles die, kill them
    val deadTurtles = entities.filter{ case (id, _) => ! turtles.contains(id) }
    deadTurtles.foreach{ case (id, npc) => despawn(npc); entities.remove(id) }

    // a hack for wolf-sheep that might be useful later in other models.
    val patches: Iterator[Patch] = ws.world.patches.toLogoList.scalaIterator.collect{case p: Patch => p}
    patches.foreach(updatePatchColorMaybe(_, p.world))
  }

  def npc(id:Long, loc: Location, breed: String) = (breed.toLowerCase match {
    case "wolves" => wolf  _
    case "sheep"  => sheep _
    case _        => human _
  })(id.toString, loc)

  // for reference, see:
  // https://github.com/haveric/Wool-Trees/blob/master/src/haveric/woolTrees/Commands.java
  // http://ccl.northwestern.edu/netlogo/docs/programming.html#colors
  /**
   * Yet unhandled Minecraft colors.
   * } else if (args[i].equalsIgnoreCase("lightblue")){
   *   colorArray.add(3);
   * } else if (args[i].equalsIgnoreCase("lightgray") || args[i].equalsIgnoreCase("lightgrey")){
   *   colorArray.add(8);
   **/
  val colors = Map(
    // black
    0d  ->  (WOOL, Some(15:Byte)),
    // grey
    5d  ->  (WOOL, Some(7:Byte)),
    // white
    9.9 ->  (WOOL, Some(0:Byte)),
    // red
    15d ->  (WOOL, Some(14:Byte)),
    // orange
    25d ->  (WOOL, Some(1:Byte)),
    // brown
    35d ->  (DIRT, None),  // 12b for wool
    // yellow
    45d ->  (WOOL, Some(4:Byte)),
    // green
    55d ->  (GRASS, None), // 13b for wool
    // lime
    65d ->  (WOOL, Some(5:Byte)), // light green in Minecraft.
    // turquoise??
    // 75d ->  (WOOL, ??)
    // cyan
    85d ->  (WOOL, Some(9:Byte)),
    // sky??
    // 95d -> (WOOL, ??),
    // blue
    105d -> (WOOL, Some(11:Byte)),
    // violet
    115d -> (WOOL, Some(10:Byte)), // purple in Minecraft, i think.
    // magenta
    125d -> (WOOL, Some(2:Byte)),
    // pink
    135d -> (WOOL, Some(6:Byte))
  )

  /**
   * A couple of things are going on here...
   *
   * In the simple case, the patch is just on the ground, and we update its
   * color, if its color is a valid minecraft color (from the color map above)
   *
   * However, if the patch has a z variable, then we do some other crap
   * // TODO: explain that other crap
   */
  def updatePatchColorMaybe(patch: Patch, world: World): Unit = {
    colors.get(patch.pcolorDouble).foreach{ case (m, color) =>
      val z = 3 + tryO(patch.variables(5).asInstanceOf[Double].toInt).getOrElse(0)
      val bottomBlock = world(patch.pxcor, z, patch.pycor)
      val allBlocks = bottomBlock.andBlocksAbove.take(z - 3)
      allBlocks.foreach{ b =>
        if (! (b is m)) b changeTo m
        if (b is WOOL) color.foreach(b.setData)
      }
      if (z == 3) bottomBlock.blocksAbove.takeWhile(_ isNot AIR).foreach(_ changeTo AIR)
    }
  }

  /**
   * Some special minecraft-netlogo integration code.
   * If t happens to have a 'holding' variable, and it happens to be a double
   * then update what the player has in his hands.
   */
  def updateHolding(npc: NPC, t:Turtle): Unit = tryO(
    entities(t.id).asInstanceOf[CraftPlayer].setItemInHand(
      t.variables(t.LAST_PREDEFINED_VAR + 1) match {
        case d: java.lang.Double => colors.get(d).fold(new ItemStack(AIR)){ case (m, oc) =>
          oc.fold(new ItemStack(m))(new ItemStack(m, 1, 0:Short, _))
        }
        case _ => new ItemStack(AIR)
      }
    )
  )
}
