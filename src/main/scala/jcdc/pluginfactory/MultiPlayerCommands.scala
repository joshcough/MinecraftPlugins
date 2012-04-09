package jcdc.pluginfactory

import org.bukkit.{GameMode, ChatColor}
import org.bukkit.entity._
import scala.collection.JavaConversions._
import ChatColor._
import ScalaPlugin._

class MultiPlayerCommands extends CommandsPlugin {

  val commands = Map(
    "goto"     -> oneArg(p2p((sender, receiver, _) => sender.teleport(receiver))),
    "gm"       -> gameModeChanger,
    "spawn"    -> spawner,
    "kill"     -> opOnly(killHandler),
    "set-time" -> oneArg((p, cmd) => p.world.setTime(cmd.args.head.toInt)),
    "day"      -> command((p, _)  => p.world.setTime(1)),
    "night"    -> command((p, _)  => p.world.setTime(15000)),
    "entities" -> command((p, _)  => p.world.entities.foreach(e => p.sendMessage(e.toString))),
    "feed"     -> opOnly(feedHandler),
    "starve"   -> opOnly(starveHandler),
    "shock"    -> opOnly(shockHandler),
    "ban"      -> opOnly(banHandler))

  lazy val gameModeChanger = oneArg((p, c) =>
    if(! List("c", "s").contains(c.args.head.toLowerCase)) p.sendUsage(c.cmd)
    else p.setGameMode(if(c.args.head.toLowerCase == "c") GameMode.CREATIVE else GameMode.SURVIVAL)
  )

  lazy val killHandler = oneOrMoreArgs((killer, c) => {
    val entities = killer.world.entities
    c.args.map(_.toLowerCase) match {
      case "player" :: p :: Nil => if(c.args.length == 2) killer.kill(p) else killer.sendUsage(c.cmd)
      case "items"  :: Nil      => entities.collect{ case i: Item => i }.foreach(_.remove)
      case name     :: Nil => findEntity(name) match {
        case Some(e) => entities.filter{ _.getType == e  }.foreach(_.remove)
        case _ => killer.sendUsage(c.cmd)
      }
      case _ => killer.sendUsage(c.cmd)
    }
  })

  lazy val feedHandler = p2p((feeder, eater, _) => {
    eater.messageAfter(GREEN + "you have been fed by " + feeder.name){ eater.setFoodLevel(20) }
    feeder.sendMessage(GREEN + "you have fed" + feeder.name)
  })

  lazy val starveHandler = p2p((badGuy, starver, _) => {
    starver.messageAfter(GREEN + "you have been starved by " + badGuy.name){ starver.setFoodLevel(0) }
    badGuy.sendMessage  (GREEN + "you have starved " + starver.name)
  })

  lazy val shockHandler = p2p((shocker, shockee, _) => {
    shockee.messageAfter(GREEN + "you have been shocked by " + shocker.name){ shockee.strike }
    shocker.sendMessage (GREEN + "you have shocked " + shockee.name)
  })

  lazy val banHandler = oneOrMoreArgs((p, c) => {
    p.server.findOnlinePlayers (c.args).foreach{ _.ban(p.name + " doesn't like you.") }
    p.server.findOfflinePlayers(c.args).foreach{ _.setBanned(true) }
  })

  lazy val spawner = oneOrMoreArgs((p, c) => findEntity(c.args.head) match {
    case Some(creature) => p.loc.spawnN(creature, (if (c.args.length == 2) c.args(1).toInt else 1))
    case _ => p.sendError("no such creature: " + c.args.head)
  })
}