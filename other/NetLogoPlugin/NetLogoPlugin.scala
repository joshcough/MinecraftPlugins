package com.joshcough.minecraft

import java.io.File
import java.util.UUID.randomUUID

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.npc.NPCRegistry

import org.bukkit.Server
import org.nlogo.agent.{Agent, Patch, Turtle}
import org.nlogo.headless.HeadlessWorkspace
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.{Location, Material, World}

import scala.collection.JavaConverters._
import scala.language.postfixOps

class NetLogoPluginConfig extends PluginConfig[NetLogoPlugin] {
  val pluginClass = classOf[NetLogoPlugin]
  override val commands = NetLogoPluginCommands.commands(null, null)
  override val hardDependencies = List("Citizens")
}

case class NetLogoEvent(player:Player, ticksRemaining: Int, sleepTime: Option[Long]){
  def tick = this.copy(ticksRemaining = ticksRemaining - 1)
}

class NetLogoPlugin extends CommandsPlugin {
  System.setProperty("org.nlogo.noGenerator", "true")
  override val commands = NetLogoPluginCommands.commands(server, this)
}

object NetLogoPluginCommands {
  import CommandsPlugin._
  import Material._


  lazy val registry: NPCRegistry = CitizensAPI.getNPCRegistry
  var workspace: Option[HeadlessWorkspace] = None
  // whether or not to check all the patches for updates each tick
  var updatePatches = true
  var setupComplete = false

  def commands(implicit server: Server, plugin: ScalaPlugin) = {
    val TaskManager = new TaskManager(server, plugin)
    import TaskManager._
    lazy val tasks = new PlayerTasks
    import tasks._
    List(
      Command("open", "Open a model", existingFile ~ boolOrTrue) {
        case (p, model ~ updatePatches) =>
          openModel(p, model, updatePatches)
          // call setup on load, because that just makes sense.
          setup(p)
      },
      Command("open-no-setup", "Open a model without setting up.", existingFile ~ boolOrTrue) {
        case (p, model ~ updatePatches) => openModel(p, model, updatePatches)
      },
      Command("setup", "Call the setup proc.")(setup),
      Command("go",    "Call the go proc once.")(go),
      Command("call",  "Call a NetLogo proc.", anyString+){
        case (p, proc) => callProc(p, proc.mkString(" "))
      },
      Command("loop",  "Call go until it is finished.", int ~ long.?) {
        case (p, n ~ sleepTime) =>
          p.scheduleSyncTask {
            for (_ <- 0 to n) {
              p.scheduleSyncDelayedTask(sleepTime.getOrElse(0)) {
                callProc(p, "go")
              }
            }
          }
          p ! ("looped " + n + " times.")
      },
      Command("dispose", "Start over."){ p =>
        // todo: what if we are running the go loop in a new thread here?
        // we probably should shut it down...
        dispose()
      },
      Command("report", "Report something...", anyString+){ case (p, reporter) =>
        usingWorkspace(p)(ws => p ! ws.report(reporter.mkString(" ")).toString)
      },
    )
  }

  def openModel(p: Player, model: File, updatePatches: Boolean) = try {
    dispose() // implicitly start over.
    workspace = Some(HeadlessWorkspace.newInstance)
    this.updatePatches = updatePatches
    // model must be a file on the server....
    // maybe it could somehow be a url that we pull down?
    workspace.get.open(model.getAbsolutePath)
    p ! ("loaded " + model)
  } catch {
    case e: Throwable =>
      e.printStackTrace()
      e.getCause.printStackTrace()
      throw e
  }

  def callProc(p: Player, proc:String) = usingWorkspace(p)(_.command(proc))
  def go(p: Player): Unit = callProc(p, "go")
  def setup(p: Player): Unit = {
    callProc(p, "setup")
    setupComplete = true
  }

  def usingWorkspace(p: Player)(f: HeadlessWorkspace => Unit) =
    workspace.fold(p ! "call 'open' first!"){ ws => f(ws); update(p.world, ws) }


  def dispose(){
    registry.deregisterAll()
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

      Option(registry.getById(id.toInt)) match {
        /**
          * if new turtles are born in netlogo, create an npc for them in minecraft.
          * npc plugin handles the breedname automatically, spawning a human if the breedname is unknown.
          */
        case None => spawn(id, loc, t.getBreed.printName)
        /* update the positions of existing npcs based on their turtles position. */
        case Some(npc) => npc.teleport(loc, TeleportCause.PLUGIN)
      }
    }

    def spawn(id: Long, loc: Location, breedName: String) = {
      // TODO: deal with breedName here
      // TODO: this toInt is suspicious. It means that netlogo could have an id larger than an Int,
      // and unknown behavior could happen. lets hope it doesn't
      val npc: NPC = registry.createNPC(EntityType.PLAYER, randomUUID(), id.toInt, id.toString)
      npc.teleport(loc, TeleportCause.PLUGIN)
      // TODO: hold code had to do this:
      // e setStationary true
      npc
    }

    /* if turtles die in netlogo, kill them in minecraft */
    val deadTurtles = registry.sorted.asScala.filter{ npc => ! turtles.contains(npc.getId) }
    deadTurtles.foreach(registry.deregister)

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
  def colors(netLogoColor: Double): Material = netLogoColor match {
    case x if x % 10 < 2           => BLACK_WOOL
    case x if x >= 2   && x <= 5   => GRAY_WOOL
    case x if x >  5   && x <  8   => LIGHT_GRAY_WOOL
    case x if x >= 8   && x <  12  => WHITE_WOOL
    case x if x >= 12  && x <= 18  => RED_WOOL
    case x if x >= 22  && x <= 28  => ORANGE_WOOL
    case x if x >= 32  && x <= 38  => DIRT  // choosing not to use brown, but dirt instead
    case x if x >= 42  && x <= 48  => YELLOW_WOOL
    case x if x >= 52  && x <= 58  => GRASS // choosing not to use green, but grass instead
    case x if x >= 62  && x <= 68  => LIME_WOOL
    // there doesnt seem to be a turquoise in MineCraft...using lime instead.
    case x if x >= 72  && x <= 78  => LIME_WOOL
    case x if x >= 82  && x <= 88  => CYAN_WOOL
    case x if x >= 92  && x <= 98  => LIGHT_BLUE_WOOL // sky in NetLogo
    case x if x >= 102 && x <= 108 => BLUE_WOOL
    case x if x >= 112 && x <= 118 => PURPLE_WOOL
    case x if x >= 122 && x <= 128 => MAGENTA_WOOL
    case x if x >= 132 && x <= 138 => PINK_WOOL
    case x if x % 10 > 8           => WHITE_WOOL
    // this could only happen if we are given a double outside the color range
    // i could imagine a few scenarios where that would happen, but it doesn't much matter.
    case _    => WHITE_WOOL
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
    Cube(lowBlock.point, topBlock.point)(w(_)).toStream.foreach { b =>
      val m = colors(p.pcolor.asInstanceOf[Double])
      if (b.getType != m) b setType m
    }
    topBlock.blocksAbove.takeWhile(_ isNot AIR).foreach(_ changeTo AIR)
  }

  def agentVar(ws: HeadlessWorkspace, a:Agent, varName:String): Option[Object] = {
    val index = ws.world.indexOfVariable(a, varName)
    if (index == -1) None else Some(a.variables(index))
  }
}
