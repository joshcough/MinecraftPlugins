package com.joshcough.minecraft

import org.bukkit.GameMode._
import scala.collection.JavaConversions._

/**
 * Some common commands that I found myself writing over and over again
 * for various plugins. I wanted to put them all into one location,
 * so that they could be reused elsewhere.
 */
trait CommonCommands {

  import CommandsPlugin._

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
  val gm    =  Command("gm",    "Set your game mode", gamemode){ case (p, gm) => p.setGameMode(gm) }
  val gms   =  Command("gms",   "Set your game mode to survival.")(_.setGameMode(SURVIVAL))
  val gmc   =  Command("gmc",   "Set your game mode to creative.")(_.setGameMode(CREATIVE))
  val kill  =  Command("kill",  "Kill entities.", ("player" ~ player) or entity){
    case (killer, Left(_ ~ deadMan)) => killer.kill(deadMan)
    case (killer, Right(e)) => killer.world.entities.filter { _ isAn e }.foreach(_.remove)
  }

  val allCommonCommands = List(goto, timeCommand, day, night, gm, gms, gmc, kill)
}
