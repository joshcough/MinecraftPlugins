package jcdc.pluginfactory.commandsalt

import org.bukkit.GameMode._
import org.bukkit.Material
import Material._
import jcdc.pluginfactory.EnrichmentClasses._
import scala.collection.JavaConversions._
import jcdc.pluginfactory.Cubes

trait CommonCommands extends CommandsPlugin {

  // some simple useful commands
  val goto = Command("goto", "Teleport to a player.", player or location){
    case (you, Left(them)) => you.teleportTo(them)
    case (you, Right(loc)) => you.teleport(loc of you.world)
  }
  val timeCommand = Command("set-time", "Sets the time.", time){
    case (p, n) => p.world.setTime(n)
  }
  val day   =  Command("day",   "Sets the time to 1."    )(_.world.setTime(1))
  val night =  Command("night", "Sets the time to 15000.")(_.world.setTime(15000))
  val gm    =  Command("gm",    "Set your game mode", gamemode){
    case (p, gm) => p.setGameMode(gm)
  }
  val gms   =  Command("gms",   "Set your game mode to survival.")(_.setGameMode(SURVIVAL))
  val gmc   =  Command("gmc",   "Set your game mode to creative.")(_.setGameMode(CREATIVE))
  val kill  =  Command("kill",  "Kill entities.", ("player" ~ player) or entity){
    case (killer, Left(_ ~ deadMan)) => killer.kill(deadMan)
    case (killer, Right(e)) => killer.world.entities.filter { _ isAn e }.foreach(_.remove)
  }

  val allCommonCommands = List(goto, timeCommand, day, night, gm, gms, gmc, kill)
}

trait WorldEditCommands extends CommandsPlugin with Cubes {
  // some common world editing commands
  val wand  = Command("/wand",  "Get a WorldEdit wand.")  (_.loc.dropItem(WOOD_AXE))
  val pos1  = Command("/pos1",  "Set the first position") (p => setFirstPosition(p, p.loc))
  val pos2  = Command("/pos2",  "Set the second position")(p => setSecondPosition(p, p.loc))
  val erase = Command("/erase", "Set all the selected blocks to air.")((run(_)(_.eraseAll)))
  val set   = Command(
    name = "/set",
    desc = "Set all the selected blocks to the given material type.",
    args = material)(
    body =  { case (p, m) => run(p)(_ setAll m) }
  )
  val change = Command(
    name = "/change",
    desc = "Change all the selected blocks of the first material type to the second material type.",
    args = material ~ material)(
    body = { case (p, oldM ~ newM) => run(p)(_.changeAll(oldM, newM)) }
  )
  val allWorldEditingCommands = List(wand, pos1, pos2, erase, set, change)
}
