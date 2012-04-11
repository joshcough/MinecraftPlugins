package jcdc.pluginfactory

import org.bukkit.GameMode._
import org.bukkit.Material._
import scala.collection.JavaConversions._
import ScalaPlugin._

class MultiPlayerCommands extends CommandsPlugin {
  val commands = Map(
    "goto"     -> p2p((you, them, _) => you.teleportTo(them)),
    "set-time" -> oneArg((p, cmd)    => p.world.setTime(cmd.args.head.toInt)),
    "day"      -> command((p, _)     => p.world.setTime(1)),
    "night"    -> command((p, _)     => p.world.setTime(15000)),
    "entities" -> command((p, _)     => p !* (p.world.entities.map(_.toString) :_*)),
    "feed"     -> opOnly(p2p((you, them, _) => you.doTo(them, them.setFoodLevel(20), "fed"))),
    "starve"   -> opOnly(p2p((you, them, _) => you.doTo(them, them.setFoodLevel(0), "starved"))),
    "shock"    -> opOnly(p2p((you, them, _) => you.doTo(them, them.strike, "shocked"))),
    "gm"       ->
      oneArg((p, c) => c.args.head.toLowerCase match {
        case "c" => p.setGameMode(CREATIVE)
        case "s" => p.setGameMode(SURVIVAL)
        case _   => p.sendUsage(c.cmd)
      }),
    "spawn"    ->
      oneOrMoreArgs((p, c) => foldOption(findEntity(c.args.head))(
        p.sendError("no such creature: " + c.args.head),
        e => p.loc.spawnN(e, (if (c.args.length == 2) c.args.head.toInt else 1))
      )),
    "ban"      ->
      opOnly(oneOrMoreArgs((p, c) => {
        server.findOnlinePlayers (c.args).foreach{ _.ban(p.name + " doesn't like you.") }
        server.findOfflinePlayers(c.args).foreach{ _.setBanned(true) }
      })),
    "kill"     ->
      opOnly(oneOrMoreArgs((killer, c) => c.args.map(_.toLowerCase) match {
        case "player" :: p :: Nil => killer.kill(p)
        case name          :: Nil => foldOption(findEntity(name))(
          killer.sendUsage(c.cmd),
          e => killer.world.entities.filter{ _.isAn(e) }.foreach(_.remove)
        )
        case _ => killer.sendUsage(c.cmd)
      })),
    "drill"    ->
      command((p, _) => {
        for(b <- p.loc.block.blocksBelow.takeWhile(_ isNot BEDROCK)){
          if(b isNot AIR) b.erase
          if(b.blockBelow is BEDROCK) {
            b.blockAbove.blockAbove.blockAbove.blockAbove.setType(getMaterial(11))
            b.blockAbove.blockAbove.setType(getMaterial(9))
          }
        }
      })
  )
}