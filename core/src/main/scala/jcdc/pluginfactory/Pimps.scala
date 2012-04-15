package jcdc.pluginfactory

import org.bukkit.{ChatColor, Effect, Location, Material, OfflinePlayer, Server, World}
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.entity.{Entity, EntityType, Player}
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import ChatColor._
import Effect._
import Material._

object Pimps extends Pimps

trait Pimps {

  implicit def pimpedEntity(e:Entity)           = new PimpedEntity(e)
  implicit def pimpedPlayer(player:Player)      = new PimpedPlayer(player)
  implicit def pimpedLocaton(l:Location)        = new PimpedLocation(l)
  implicit def pimpedWorld(w:World)             = new PimpedWorld(w)
  implicit def pimpedServer(s:Server)           = new PimpedServer(s)
  implicit def pimpedBlock(b:Block)             = new PimpedBlock(b)
  implicit def pimpedItemStack(i:ItemStack)     = new PimpedItemStack(i)
  implicit def pimpedCancellable(c:Cancellable) = new PimpedCancellable(c)
  implicit def pimpedEntityEvent(e:EntityEvent) = new PimpedEntity(e.getEntity)
  implicit def pimpedPlayerEvent(e:PlayerEvent) = new PimpedPlayer(e.getPlayer)
  implicit def pimpedWeatherChangeEvent(e:WeatherChangeEvent) = new {
    def rain = e.toWeatherState
    def sun  = ! e.toWeatherState
  }
  implicit def pimpedOption[T](ot: Option[T])   = new PimpedOption(ot)

  case class PimpedBlock(b:Block) {
    lazy val world = b.getWorld
    lazy val loc   = b.getLocation
    private def blockAt(x: Double, y: Double, z: Double) = world.blockAt(x, y, z)
    lazy val (x, y, z) = (b.getX, b.getY, b.getZ)
    lazy val (xd, yd, zd) = (b.getX.toDouble, b.getY.toDouble, b.getZ.toDouble)
    def copy(x: Double = xd, y: Double = yd, z: Double = zd) = blockAt(x, y, z)
    lazy val blockAbove = blockAt(xd, yd + 1, zd)
    lazy val blockBelow = blockAt(xd, yd - 1, zd)
    def nthBlockAbove(n:Int) = blockAt(xd, yd + n, zd)
    def nthBlockBelow(n:Int) = blockAt(xd, yd - n, zd)
    def blocksAbove: Stream[Block] = blockAbove #:: blockAbove.blocksAbove
    def blocksBelow: Stream[Block] = blockBelow #:: blockBelow.blocksBelow
    def neighbors4: List[Block] =
      blockAt(xd + 1, yd, zd) ::
        blockAt(xd - 1, yd, zd) ::
        blockAt(xd, yd, zd + 1) ::
        blockAt(xd, yd, zd - 1) :: Nil
    def neighbors8: List[Block] = neighbors4 ++ (
      blockAt(xd + 1, yd, zd + 1) ::
        blockAt(xd + 1, yd, zd - 1) ::
        blockAt(xd - 1, yd, zd + 1) ::
        blockAt(xd - 1, yd, zd - 1) :: Nil
      )
    def neighbors: List[Block] =
      neighbors8 ++
        (b.blockBelow :: b.blockBelow.neighbors8) ++
        (b.blockAbove :: b.blockAbove.neighbors8)
    def neighborsForPlayer: List[Block] =
      neighbors8 ++ // 8 blocks at the feet of the player
        (b.blockAbove.neighbors8) ++ // 8 blocks at the head of the player
        (b.blockBelow :: b.blockBelow.neighbors8) ++ // 9 blocks below the player
        (b.nthBlockAbove(2) :: b.nthBlockAbove(2).neighbors8) // 9 blocks above the player.
    def is(m:Material)    = b.getType == m
    def isA(m:Material)   = b.getType == m
    def isNot(m:Material) = b.getType != m
    def itemStack(n:Int) = new ItemStack(b.getType, 1, b.getData)
    def erase = {
      b.world.dropItem(b.loc, b.itemStack(1))
      b.world.playEffect(b.loc, SMOKE, 1)
      changeTo(AIR)
    }

    def changeTo(m: Material) = b.setType(m)
  }

  case class PimpedCancellable(c:Cancellable){
    def cancel = c.setCancelled(true)
    def cancelIf(b: => Boolean, runBeforeCancelling: => Unit = () => ()){
      if(b) { runBeforeCancelling; c.setCancelled(true) }
    }
  }

  case class PimpedEntity(e:Entity){
    def loc      = e.getLocation
    def server   = e.getServer
    def world    = e.getWorld
    def whenPlayer(f: Player => Unit) = if(e.isInstanceOf[Player]) f(e.asInstanceOf[Player])
    def isA(et:EntityType)  = e.getType == et
    def isAn(et:EntityType) = e.getType == et
  }

  case class PimpedItemStack(i:ItemStack){
    def isA(m:Material)  = i.getType == m
    def isAn(m:Material) = i.getType == m
  }

  case class PimpedWorld(w:World){
    def entities = w.getEntities
    def blockAt(x: Double, y: Double, z: Double) = new Location(w, x, y, z).getBlock
    def between(loc1:Location, loc2: Location): Stream[Block] = {
      val ((x1, y1, z1), (x2, y2, z2)) = (loc1.xyz, loc2.xyz)
      def range(i1: Int, i2: Int) = (if(i1 < i2) i1 to i2 else i2 to i1).toStream
      for (x <- range(x1,x2); y <- range(y1,y2); z <- range(z1,z2)) yield w.blockAt(x,y,z)
    }
  }

  case class PimpedLocation(loc: Location){
    lazy val (x,y,z) = (loc.getX.toInt, loc.getY.toInt, loc.getZ.toInt)
    lazy val xyz = (x, y, z)
    def world = loc.getWorld
    def block = loc.getBlock
    def spawn(entityType:  EntityType) = world.spawnCreature(loc, entityType)
    def spawnN(entityType: EntityType, n: Int) = for (i <- 1 to n) spawn(entityType)
  }

  case class PimpedServer(s:Server){
    def findPlayer(name:String) = Option(s.getPlayer(name))
    def findOnlinePlayer = findPlayer _
    def findOfflinePlayer(name:String) = Option(s.getOfflinePlayer(name))
    def findOnlinePlayers(names: List[String]): List[Player] = names.map(findOnlinePlayer).flatten
    def findOfflinePlayers(names: List[String]): List[OfflinePlayer] = names.map(findOfflinePlayer).flatten
  }

  case class PimpedPlayer(player:Player){
    def name   = player.getName
    def x      = player.getLocation.getX
    def y      = player.getLocation.getY
    def z      = player.getLocation.getZ
    def loc    = player.getLocation
    def world  = player.getWorld
    def server = player.getServer

    def isHolding  (m: Material) = player.getItemInHand.getType == m
    def isHoldingA (m: Material) = isHolding(m)
    def isHoldingAn(m: Material) = isHolding(m)

    def withMaterial[T](nameOrId:String)(f: Material => T) {
      attemptingWith(findMaterial(nameOrId))("No such material: " + nameOrId, f)
    }
    def attemptingWith[T, U](ot: Option[T])(s: => String, f: T => U){
      ot.fold(player ! s)(t => f(t))
    }

    def blockOn         = player.loc.block
    def blockAboveHead  = blockOn.nthBlockAbove(2)
    def blocksAboveHead = blockOn.blockAbove.blockAbove.blocksAbove
    def blocksAround    = blockOn.neighborsForPlayer

    def doTo(otherPlayer: Player, f: => Unit, actionName: String){
      f
      otherPlayer  ! (GREEN + "you have been " + actionName + " by " + player.name)
      player       ! (GREEN + "you have " + actionName + " " + otherPlayer.name)
    }

    def !  (s:String)    = player.sendMessage(s)
    def !* (ss: String*) = ss.foreach(s => player ! s)
    def sendError(message:String) = player.sendMessage(RED + message)
    def sendUsage(cmd:Command)    = sendError(cmd.getUsage)
//    def sendUsage(cmd:CommandArguments) = sendError(cmd.cmd.getUsage)
    def findPlayer(name:String)(f: Player => Unit) = server.findPlayer(name) match {
      case Some(p) => f(p)
      case None => sendError("kill could not find player: " + name)
    }
    def findPlayers(names:List[String])(f: Player => Unit) = names.foreach(n => findPlayer(n)(f))
    def ban(reason:String){ player.setBanned(true); player.kickPlayer("banned: " + reason) }
    def kill(playerName:String): Unit = findPlayer(playerName)(kill)
    def kill(p:Player) = doTo(p, p.setHealth(0), "killed")
    def teleportTo(otherPlayer: Player) = player.teleport(otherPlayer)
    def teleportTo(b: Block)            = player.teleport(b.loc)
    def strike = world.strikeLightning(loc)
  }

  case class PimpedOption[T](ot: Option[T]){
    def fold[U](u: => U)(f: T => U) = ot.map(f).getOrElse(u)
  }

  def findEntity(nameOrId:String) = Option(EntityType.fromName(nameOrId)).orElse(
    try Option(EntityType.fromId(nameOrId.toInt)) catch { case e => None }
  )

  def findMaterial(nameOrId: String) = Option(getMaterial(nameOrId.toUpperCase)).orElse(
    try Option(getMaterial(nameOrId.toInt)) catch { case e => None }
  )

  def id[T](t:T) = identity(t)
}
