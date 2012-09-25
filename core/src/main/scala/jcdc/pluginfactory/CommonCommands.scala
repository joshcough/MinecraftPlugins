package jcdc.pluginfactory

import org.bukkit.GameMode._
import org.bukkit.Material
import Material._
import EnrichmentClasses._
import scala.collection.JavaConversions._

trait CommonCommands extends CommandsPlugin {

  // some simple useful commands
  val goto  = Command("goto",     "Teleport to a player.", args(player or (num ~ num ~ opt(num))){
    case you ~ Left(them) => you.teleportTo(them)
    case you ~ Right(x ~ y ~ Some(z)) => you.teleport(you.world(x, y, z))
    case you ~ Right(x ~ z ~ None) =>
      you.teleportTo(you.world.getHighestBlockAt(you.world(x, 0, z)))
  })
  val time  =  Command("set-time", "Sets the time.", args(num){
    case p ~ n => p.world.setTime(n)
  })
  val day   =  Command("day",   "Sets the time to 1.", noArgs(_.world.setTime(1)))
  val night =  Command("night", "Sets the time to 15000.", noArgs(_.world.setTime(15000)))
  val gm    =  Command("gm",    "Set your game mode", args(gamemode){
    case p ~ gm => p.setGameMode(gm)
  })
  val gms   =  Command("gms",   "Set your game mode to survival.", noArgs(_.setGameMode(SURVIVAL)))
  val gmc   =  Command("gmc",   "Set your game mode to creative.", noArgs(_.setGameMode(CREATIVE)))
  val kill  =  Command("kill",  "Kill entities.",
    args(("player" ~ player) or entity){
      case killer ~ Left(_ ~ deadMan) => killer.kill(deadMan)
      case killer ~ Right(e) => killer.world.entities.filter { _ isAn e }.foreach(_.remove)
    })

  val allCommonCommands = List(goto, time, day, night, gm, gms, gmc, kill)
}

trait WorldEditCommands extends CommandsPlugin with Cubes {
  // some common world editing commands
  val wand  = Command("/wand",  "Get a WorldEdit wand.",   noArgs(_.loc.dropItem(WOOD_AXE)))
  val pos1  = Command("/pos1",  "Set the first position",  noArgs(p => setFirstPosition(p, p.loc)))
  val pos2  = Command("/pos2",  "Set the second position", noArgs(p => setSecondPosition(p, p.loc)))
  val erase = Command("/erase", "Set all the selected blocks to air.", noArgs(run(_)(_.eraseAll)))
  val set   = Command(
    name = "/set",
    desc = "Set all the selected blocks to the given material type.",
    body = args(material) { case p ~ m => run(p)(_ setAll m) }
  )
  val change = Command(
    name = "/change",
    desc = "Change all the selected blocks of the first material type to the second material type.",
    body = args(material ~ material) { case p ~ (oldM ~ newM) => run(p)(_.changeAll(oldM, newM)) }
  )
  val allWorldEditingCommands = List(wand, pos1, pos2, erase, set, change)
}
