package jcdc.pluginfactory

import org.bukkit.{GameMode, ChatColor}
import org.bukkit.command.Command
import org.bukkit.entity._
import scala.collection.JavaConversions._
import ChatColor._
import ScalaPlugin._

class MultiPlayerCommands extends ManyCommandsPlugin {

  val commands = Map(
    "goto"     -> oneArg(p2p((sender, receiver, _, _) => sender.teleport(receiver))),
    "gm"       -> gameModeChanger,
    "kill"     -> opOnly(killHandler),
    "set-time" -> oneArg((p:Player, _, args) => p.world.setTime(args(0).toInt)),
    "day"      -> command((p, _, _) => p.world.setTime(1)),
    "night"    -> command((p, _, _) => p.world.setTime(15000)),
    "spawn"    -> spawner,
    "entities" -> command((p, _, _) => p.world.entities.foreach(e => p.sendMessage(e.toString))),
    "feed"     -> opOnly(feedHandler),
    "starve"   -> opOnly(starveHandler),
    "shock"    -> opOnly(shockHandler),
    "ban"      -> opOnly(banHandler))

  lazy val gameModeChanger = oneArg((p, c, args) =>
    if(! List("c", "s").contains(args(0))) p.sendUsage(c)
    else p.setGameMode(if(args(0) == "c") GameMode.CREATIVE else GameMode.SURVIVAL))

  lazy val killHandler = oneArg((killer, c, args) => {
    val entities = killer.world.entities
    def usage(){ killer.sendUsage(c) }
    def removeAll(es:Seq[Entity]) { es.foreach(_.remove()) }
    args(0).toLowerCase match {
      case "player" => if(args.length == 2) killer.kill(args(1)) else usage()
      case "items" => removeAll(entities.collect{ case i: Item => i })
      case "chickens" => removeAll(entities.collect{ case i: Chicken => i })
      case _ => usage()
    }
  })

  lazy val feedHandler = oneArg(p2p((feeder, receiver, _, _) => {
    receiver.messageAfter(GREEN + "you have been fed by " + feeder.name){ receiver.setFoodLevel(20) }
    feeder.sendMessage(GREEN + "you have fed" + feeder.name)
  }))

  lazy val starveHandler = oneArg(p2p((feeder, receiver, _, _) => {
    receiver.messageAfter(GREEN + "you have been starved by " + feeder.name){ receiver.setFoodLevel(0) }
    feeder.sendMessage(GREEN + "you have starved " + receiver.name)
  }))

  lazy val shockHandler = oneArg(p2p((shocker:Player, shockee:Player, _, _) => {
    shockee.messageAfter(GREEN + "you have been shocked by " + shocker.name){ shockee.strike }
    shocker.sendMessage(GREEN + "you have shocked " + shockee.name)
  }))

  lazy val banHandler = oneOrMoreArgs((p, _, args) => {
    for(p<-args.map(p.server.getPlayer); if(p!=null)){ p.ban(p.name + " doesn't like you.") }
    for(p<-args.map(p.server.getOfflinePlayer); if(p!=null)){ p.setBanned(true) }
  })

  lazy val spawner = oneArg((p: Player, c: Command, args: Array[String]) => {
    val nrToSpawn = (if (args.length == 2) args(1).toInt else 1)
    Spawner.spawn(entityType=args(0), number=nrToSpawn, p.loc, p.sendError(_))
  })
}