package jcdc.pluginfactory.examples

import org.bukkit.GameMode._
import org.bukkit.Material._
import scala.collection.JavaConversions._

class MultiPlayerCommandsV2 extends jcdc.pluginfactory.CommandsV2 {
  val commands = Map(
    "goto"     -> p2p((you, them) => you.teleportTo(them)),
    "set-time" -> args(num){ case p ~ n => p.world.setTime(n) },
    "day"      -> noArgs(_.world.setTime(1)),
    "night"    -> noArgs(_.world.setTime(15000)),
    "entities" -> noArgs(p => p !* (p.world.entities.map(_.toString): _*)),
    "feed"     -> opOnly(p2p((you, them) => you.doTo(them, them.setFoodLevel(20), "fed"))),
    "starve"   -> opOnly(p2p((you, them) => you.doTo(them, them.setFoodLevel(0), "starved"))),
    "shock"    -> opOnly(p2p((you, them) => you.doTo(them, them.strike, "shocked"))),
    "gm"       ->
      args("c" | "s"){ case p ~ s => s match {
        case "c" => p.setGameMode(CREATIVE)
        case "s" => p.setGameMode(SURVIVAL)
      }},
    "gms"      -> noArgs(_.setGameMode(SURVIVAL)),
    "gmc"      -> noArgs(_.setGameMode(CREATIVE)),
    "spawn"    -> args(entity ~ opt(num)){ case p ~ (e ~ n) => p.loc.spawnN(e, n.fold(1)(id)) },
    "ban"      ->
      args(anyString+){ case you ~ them =>
        server.findOnlinePlayers (them).foreach { _.ban(you.name + " doesn't like you.") }
        server.findOfflinePlayers(them).foreach {  _.setBanned(true) }
      },
    "box"      -> args(material){ case p ~ m  => p.blocksAround.foreach(_ changeTo m) },
    "safe"     -> noArgs(_.blocksAround.foreach(_ changeTo BEDROCK)),
    "up"       -> noArgs(p => p.teleportTo(p.world.getHighestBlockAt(p.loc))),
    "drill"    ->
      noArgs(p =>
        for (b <- p.loc.block.blocksBelow.takeWhile(_ isNot BEDROCK)) {
          if (b isNot AIR) b.erase
          if (b.blockBelow is BEDROCK) {
            b.nthBlockAbove(4) changeTo STATIONARY_LAVA
            b.nthBlockAbove(2) changeTo STATIONARY_WATER
          }
      }),
    "kill"     ->
      args(anyString+){ case killer ~ args =>
        args.map(_.toLowerCase) match {
          case "player" :: dead :: Nil => killer.kill(dead)
          case name :: Nil => findEntity(name).fold(
            killer.sendError("no such entity: " + name))(
            e => killer.world.entities.filter { _.isAn(e) }.foreach(_.remove)
          )
          // TODO: i really want this here: killer.sendUsage(c.cmd)
          case _ => killer ! (org.bukkit.ChatColor.RED + "kill player playerName or kill entityType")
        }
      }
  )
}