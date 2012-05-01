package jcdc.pluginfactory

import org.bukkit.{ChatColor, Effect, Location, Material, OfflinePlayer, Server, World}
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import ChatColor._
import Effect._
import Material._
import org.bukkit.craftbukkit.CraftWorld
import net.minecraft.server.WorldServer
import org.bukkit.entity.{LivingEntity, Entity, EntityType, Player}
import org.bukkit.event.player.{PlayerInteractEvent, PlayerEvent}

object Pimps extends Pimps

trait Pimps {

  implicit def pimpedEntity(e:Entity)             = new PimpedEntity(e)
  implicit def pimpedLivingEntity(e:LivingEntity) = new PimpedLivingEntity(e)
  implicit def pimpedPlayer(player:Player)        = new PimpedPlayer(player)
  implicit def pimpedLocaton(l:Location)          = new PimpedLocation(l)
  implicit def pimpedWorld(w:World)               = new PimpedWorld(w)
  implicit def pimpedServer(s:Server)             = new PimpedServer(s)
  implicit def pimpedBlock(b:Block)               = new PimpedBlock(b)
  implicit def pimpedItemStack(i:ItemStack)       = new PimpedItemStack(i)
  implicit def pimpedCancellable(c:Cancellable)   = new PimpedCancellable(c)
  implicit def pimpedEntityEvent(e:EntityEvent)   = new PimpedEntity(e.getEntity)
  implicit def pimpedPlayerEvent(e:PlayerEvent)   = new PimpedPlayer(e.getPlayer)
  implicit def pimpedWeatherChangeEvent(e:WeatherChangeEvent) = new {
    def rain = e.toWeatherState
    def sun  = ! e.toWeatherState
  }
  implicit def pimpPlayerInteractEvent(e:PlayerInteractEvent) = new PimpedPlayerInteractEvent(e)
  implicit def pimpedOption[T](ot: Option[T])   = new PimpedOption(ot)
  implicit def blockToLoc(b: Block) = b.getLocation
  implicit def blockToItemStack(b: Block) = new ItemStack(b.getType, 1, b.getData)
  implicit def materialToItemStack(m: Material) = new ItemStack(m, 1)

  case class PimpedBlock(b:Block) {
    lazy val world = b.getWorld
    lazy val loc   = b.getLocation
    lazy val (x, y, z) = (b.getX, b.getY, b.getZ)
    lazy val (xd, yd, zd) = (b.getX.toDouble, b.getY.toDouble, b.getZ.toDouble)
    def copy(x: Double = xd, y: Double = yd, z: Double = zd) = world(x, y, z)
    lazy val blockAbove = world(xd, yd + 1, zd)
    lazy val blockBelow = world(xd, yd - 1, zd)
    def nthBlockAbove(n:Int) = world(xd, yd + n, zd)
    def nthBlockBelow(n:Int) = world(xd, yd - n, zd)
    def blocksAbove: Stream[Block] = blockAbove #:: blockAbove.blocksAbove
    def andBlocksAbove: Stream[Block] = b #:: blocksAbove
    def blocksBelow: Stream[Block] = blockBelow #:: blockBelow.blocksBelow
    def andBlocksBelow: Stream[Block] = b #:: blocksBelow
    def neighbors4: Stream[Block] =
      world(xd + 1, yd, zd) #::
      world(xd - 1, yd, zd) #::
      world(xd, yd, zd + 1) #::
      world(xd, yd, zd - 1) #:: Stream.empty
    def andNeighbors4: Stream[Block] = b #:: neighbors4
    def neighbors8: Stream[Block] = neighbors4 ++ (
      world(xd + 1, yd, zd + 1) #::
      world(xd + 1, yd, zd - 1) #::
      world(xd - 1, yd, zd + 1) #::
      world(xd - 1, yd, zd - 1) #:: Stream.empty
    )
    def andNeighbors8: Stream[Block] = b #:: neighbors8
    def neighbors: Stream[Block] =
      neighbors8 ++
      (b.blockBelow #:: b.blockBelow.neighbors8) #:::
      (b.blockAbove #:: b.blockAbove.neighbors8)
    def andNeighbors: Stream[Block] = b #:: neighbors
    def neighborsForPlayer: Stream[Block] =
      neighbors8 ++ // 8 blocks at the feet of the player
      (b.blockAbove.neighbors8) ++ // 8 blocks at the head of the player
      (b.blockBelow #:: b.blockBelow.neighbors8) #::: // 9 blocks below the player
      (b.nthBlockAbove(2) #:: b.nthBlockAbove(2).neighbors8) // 9 blocks above the player.
    def is(m:Material)    = b.getType == m
    def isA(m:Material)   = b.getType == m
    def isNot(m:Material) = b.getType != m
    def erase: Unit = if(! (b is AIR)) {
      b.world.dropItem(b.loc, b)
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
    def x        = loc.x
    def y        = loc.y
    def z        = loc.z
    def server   = e.getServer
    def world    = e.getWorld
    def whenPlayer(f: Player => Unit) = if(e.isInstanceOf[Player]) f(e.asInstanceOf[Player])
    def isA(et:EntityType)  = e.getType == et
    def isAn(et:EntityType) = e.getType == et
  }
  
  case class PimpedLivingEntity(e: LivingEntity){
    def die = e.setHealth(0)
  }

  case class PimpedItemStack(i:ItemStack){
    def isA(m:Material)  = i.getType == m
    def isAn(m:Material) = i.getType == m
  }

  case class PimpedWorld(w:World){
    def entities = w.getEntities
    def apply(x: Int, y: Int, z: Int): Block = blockAt(x.toDouble, y.toDouble, z.toDouble)
    def apply(x: Double, y: Double, z: Double): Block = new Location(w, x, y, z).getBlock
    def blockAt(x: Int, y: Int, z: Int): Block = blockAt(x.toDouble, y.toDouble, z.toDouble)
    def blockAt(x: Double, y: Double, z: Double): Block = new Location(w, x, y, z).getBlock
    def between(loc1:Location, loc2: Location): Stream[Block] = {
      val ((x1, y1, z1), (x2, y2, z2)) = (loc1.xyz, loc2.xyz)
      def range(i1: Int, i2: Int) = (if(i1 < i2) i1 to i2 else i2 to i1).toStream
      for (x <- range(x1,x2); y <- range(y1,y2); z <- range(z1,z2)) yield w(x,y,z)
    }
    def mcWorld = w.asInstanceOf[CraftWorld]
    def worldServer: WorldServer = mcWorld.getHandle
  }

  case class PimpedLocation(loc: Location){
    lazy val (x,y,z) = (loc.getX.toInt, loc.getY.toInt, loc.getZ.toInt)
    lazy val xyz = (x, y, z)
    lazy val (xd,yd,zd) = (loc.getX, loc.getY, loc.getZ)
    lazy val xyzd = (xd, yd, zd)
    def world = loc.getWorld
    def block = loc.getBlock
    def spawn(entityType:  EntityType): Unit = world.spawnCreature(loc, entityType)
    def spawnN(entityType: EntityType, n: Int): Unit = for (i <- 1 to n) spawn(entityType)
    def dropItem(stack: ItemStack): Unit = loc.world.dropItem(loc, stack)
  }

  case class PimpedServer(s:Server){
    def findPlayer(name:String) = Option(s.getPlayer(name))
    def findOnlinePlayer = findPlayer _
    def findOfflinePlayer(name:String) = Option(s.getOfflinePlayer(name))
    def findOnlinePlayers(names: List[String]): List[Player] = names.map(findOnlinePlayer).flatten
    def findOfflinePlayers(names: List[String]): List[OfflinePlayer] = names.map(findOfflinePlayer).flatten
  }

  case class PimpedPlayer(player:Player){
    def loc    = player.getLocation
    def name   = player.getName
    def world  = player.getWorld
    def server = player.getServer

    def isHolding  (m: Material) = player.getItemInHand.getType == m
    def isHoldingA (m: Material) = isHolding(m)
    def isHoldingAn(m: Material) = isHolding(m)

    def withMaterial[T](nameOrId:String)(f: Material => T) {
      attemptO(findMaterial(nameOrId))("No such material: " + nameOrId, f)
    }
    def attemptO[T, U](ot: Option[T])(s: => String, f: T => U){
      ot.fold(player ! s)(t => f(t))
    }
    def attempt[T](f: => Unit) = try f catch { case e => this.!(e.getMessage) }
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

  case class PimpedPlayerInteractEvent(e: PlayerInteractEvent){
    def block = e.getClickedBlock
    def loc = block.loc
  }

  case class PimpedOption[T](ot: Option[T]){
    def fold[U](u: => U)(f: T => U) = ot.map(f).getOrElse(u)
  }
  def tryO[T](f: => T): Option[T] = try Some(f) catch { case e => None }

  def findEntity(nameOrId:String) = Option(EntityType.fromName(nameOrId)).orElse(
    try Option(EntityType.fromId(nameOrId.toInt)) catch { case e => None }
  )

  def findMaterial(nameOrId: String) = Option(getMaterial(nameOrId.toUpperCase)).orElse(
    try Option(getMaterial(nameOrId.toInt)) catch { case e => None }
  )

  def id[T](t:T) = identity(t)

  implicit def pimpedBoolean(b1:Boolean) = new {
    def or (b2: => Boolean) = b1 || b2
    def and(b2: => Boolean) = b1 && b2
  }
}
