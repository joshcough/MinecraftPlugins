package jcdc.pluginfactory.examples

import org.bukkit.GameMode._
import org.bukkit.Material._
import org.bukkit.entity.EntityType._
import scala.collection.JavaConversions._
import jcdc.pluginfactory.CommandsPlugin

/**
 * Classic MultiPlayerCommands plugin, done in Scala.
 * Gives a whole pile of useful commands.
 * Their descriptions serve well as documentation.
 */
class MultiPlayerCommands extends CommandsPlugin {

  val commands = List(

    Command("goto",     "Teleport!", player or location){ case (you, e) =>
      e.fold(them => you teleportTo them, loc => you.teleport(loc of you.world))
    },

    Command("up",       "Go up to the surface.")(_.surface),

    Command("set-time", "Sets the time.", time){ case (p, n) => p.world setTime n },

    Command("day",      "Sets the time to day (1000).")   (_.world setTime 1000),

    Command("night",    "Sets the time to night (15000).")(_.world setTime 15000),

    Command("gms",      "Set your game mode to survival.")(_ setGameMode SURVIVAL),

    Command("gmc",      "Set your game mode to creative.")(_ setGameMode CREATIVE),

    Command("gm",       "Set your game mode.", gamemode){ case (p, gm) => p setGameMode gm },

    Command("entities", "Display all the entities.")(p => p !* (p.world.entities.map(_.toString): _*)),

    Command("spawn",    "Spawn some mobs.", entity ~ int.?.named("number to spawn")){
      case (p, e ~ n) => p.loc.spawnN(e, n.fold(1)(id))
    },

    Command("ban",      "Ban some players.", anyString+){ case (you, them) =>
      server.findOnlinePlayers (them) foreach { _ ban s"{you.name} doesn't like you." }
      server.findOfflinePlayers(them) foreach { _ setBanned true }
    },

    Command("box",      "Put a box around yourself, made of any material.", material){ case (p,m) =>
      p.blocksAround.foreach(_ changeTo m)
    },

    Command("safe",     "Put yourself in a box made of bedrock.")(
      _.blocksAround.foreach(_ changeTo BEDROCK)
    ),

    Command("drill",    "Drill down to bedrock immediately.")(p =>
      for (b <- p.blockOn.blocksBelow.takeWhile(_ isNot BEDROCK); if (b isNot AIR)) {
        b.erase
        if (b.blockBelow is BEDROCK) b nthBlockAbove 2 changeTo STATIONARY_WATER
      }
    ),

    Command("kill",     "Kill entities.", ("player" ~ player) or entity){
      case (killer, Left(_ ~ deadMan)) => killer kill deadMan
      case (killer, Right(e)) => killer.world.entities filter { _ isAn e } foreach (_.remove)
    },

    OpOnly(P2P("creeper-kill", "Surround a player with creepers")((_, them) => {
      them setGameMode SURVIVAL
      them.loc.block.neighbors8 foreach (_.loc spawn CREEPER)
    })),

    OpOnly(P2P("feed",     "Fill a players hunger bar.")((you, them) =>
      you.doTo(them, them setFoodLevel 20, "fed"))
    ),

    OpOnly(P2P("starve",   "Drain a players hunger bar.")((you, them) =>
      you.doTo(them, them setFoodLevel 0, "fed"))
    ),

    OpOnly(P2P("shock",    "Shock a player.")((you, them) => you.doTo(them, them.shock, "shocked")))
  )
}