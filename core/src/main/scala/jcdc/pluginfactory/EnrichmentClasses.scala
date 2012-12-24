package jcdc.pluginfactory

import org.bukkit.{ChatColor, Effect, Location, Material, OfflinePlayer, Server, World}
import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import ChatColor._
import Effect._
import Material._
import org.bukkit.entity.{LivingEntity, Entity, EntityType, Player}
import org.bukkit.event.player.PlayerInteractEvent
import util.Try
import java.io.File
import scala.io.Source

object EnrichmentClasses extends EnrichmentClasses

trait EnrichmentClasses {

  implicit def blockToItemStack(b: Block)       = new ItemStack(b.getType, 1, b.getData)
  implicit def materialToItemStack(m: Material) = new ItemStack(m, 1)
  implicit def materialToMaterialAndData(m:Material) = MaterialAndData(m, None)
  implicit def materialAndDataToItemStack(m:MaterialAndData) = m.itemStack
  implicit def itemStackToMaterialAndData(is:ItemStack) = MaterialAndData(is.getType,
    // i'm not sure if this check is really needed, but i guess it doesnt hurt...
    if(is.getData.getData < (0:Byte)) None else Some(is.getData.getData)
  )
  implicit def blockToMaterialAndData(b:Block) = MaterialAndData(b.getType, Some(b.getData))
  implicit def blockToLoc(b: Block): Location  = b.getLocation

  implicit class RichT[T](t:T){
    def |> [U](f: T => U) = f(t)
  }

  implicit class RichFunction1Function1[A,B,R](f: A => (B => R)) {
    def flip: B => A => R = b => a => f(a)(b)
  }

  implicit class RichFunction2[A,B,R](f: (A, B) => R) {
    def flip: (B, A) => R = (b, a) => f(a, b)
  }

  implicit class RichOption[T](o: Option[T]){
    def flipFold[B]: (T => B) => B => B = ((b: B) => (f: T => B) => o.fold(b)(f)).flip
  }

  implicit class RichBlock(b:Block) {
    lazy val world        = b.getWorld
    lazy val loc          = b.getLocation
    lazy val (x, y, z)    = (b.getX, b.getY, b.getZ)
    lazy val (xd, yd, zd) = (b.getX.toDouble, b.getY.toDouble, b.getZ.toDouble)
    lazy val chunk        = world.getChunkAt(b)
    def copy(x: Double = xd, y: Double = yd, z: Double = zd) = world(x, y, z)
    lazy val blockAbove      = world(xd, yd + 1, zd)
    lazy val blockBelow      = world(xd, yd - 1, zd)
    def nthBlockAbove(n:Int) = world(xd, yd + n, zd)
    def nthBlockBelow(n:Int) = world(xd, yd - n, zd)
    def blocksAbove   : Stream[Block] = blockAbove #:: blockAbove.blocksAbove
    def andBlocksAbove: Stream[Block] = b #:: blocksAbove
    def blocksBelow   : Stream[Block] = blockBelow #:: blockBelow.blocksBelow
    def andBlocksBelow: Stream[Block] = b #:: blocksBelow
    def neighbors4: Stream[Block] =
      world(xd + 1, yd, zd) #::
      world(xd - 1, yd, zd) #::
      world(xd, yd, zd + 1) #::
      world(xd, yd, zd - 1) #:: Stream.empty
    def andNeighbors4: Stream[Block] = b #:: neighbors4
    def neighbors8   : Stream[Block] = neighbors4 ++ (
      world(xd + 1, yd, zd + 1) #::
      world(xd + 1, yd, zd - 1) #::
      world(xd - 1, yd, zd + 1) #::
      world(xd - 1, yd, zd - 1) #:: Stream.empty
    )
    def andNeighbors8: Stream[Block] = b #:: neighbors8
    def neighbors    : Stream[Block] =
      neighbors8 ++
      (b.blockBelow #:: b.blockBelow.neighbors8) #:::
      (b.blockAbove #:: b.blockAbove.neighbors8)
    def andNeighbors : Stream[Block] = b #:: neighbors
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
    def changeTo(m: Material) = {
      try if(! chunk.isLoaded) chunk.load
      catch { case e: Exception => println("unable to load chunk.") }
      b setType m
    }
  }

  implicit class RichCancellable(c:Cancellable){
    def cancel: Unit = c.setCancelled(true)
    def cancelIf(b: => Boolean, runBeforeCancelling: => Unit = () => ()){
      if(b) { runBeforeCancelling; c.setCancelled(true) }
    }
  }

  implicit class RichEntity(e:Entity){
    def loc      = e.getLocation
    def x        = loc.x
    def y        = loc.y
    def z        = loc.z
    def server   = e.getServer
    def world    = e.getWorld
    def whenPlayer(f: Player => Unit) = if(e.isInstanceOf[Player]) f(e.asInstanceOf[Player])
    def isA(et:EntityType)  = e.getType == et
    def isAn(et:EntityType) = e.getType == et
    def shock = world strikeLightning loc
  }

  implicit class RichLivingEntity(e: LivingEntity){
    def die = e setHealth 0
  }

  implicit class RichItemStack(i:ItemStack){
    def isA (m:Material) = i.getType == m
    def isAn(m:Material) = i.getType == m
  }

  implicit class RichWorld(w:World){
    def name = w.getName
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
    def fromX(loc:Location): Stream[Block] = {
      lazy val nats:Stream[Int] = 0 #:: 1 #:: nats.tail.map(_+1)
      for (x<-nats) yield w(loc.x + x, loc.y, loc.z)
    }
  }

  implicit class RichLocation(loc: Location){
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

  implicit class RichServer(s:Server){
    def findPlayer(name:String) = tryO(s.getPlayer(name))
    def findOnlinePlayer = findPlayer _
    def findOfflinePlayer(name:String) = Option(s.getOfflinePlayer(name))
    def findOnlinePlayers(names: List[String]): List[Player] = names.map(findOnlinePlayer).flatten
    def findOfflinePlayers(names: List[String]): List[OfflinePlayer] =
      names.map(findOfflinePlayer).flatten
  }

  implicit class RichPlayer(player:Player){
    def loc    = player.getLocation
    def name   = player.getName
    def world  = player.getWorld
    def server = player.getServer

    def inventory = player.getInventory

    def holding = player.getItemInHand
    def is(pname: String) = name == pname
    def isHolding  (m: Material) = player.getItemInHand.getType == m
    def isHoldingA (m: Material) = isHolding(m)
    def isHoldingAn(m: Material) = isHolding(m)
    def isHoldingAnyOf(ms: Material*) = ms.exists(isHolding)

    def withMaterial[T](nameOrId:String)(f: Material => T) {
      attemptO(findMaterial(nameOrId))("No such material: $nameOrId", f)
    }
    def attemptO[T, U](ot: Option[T])(s: => String, f: T => U){
      ot.fold(player ! s)(t => f(t))
    }
    def attempt[T](f: => T): Unit = try f catch {
      case e: Exception => player ! RED(s"$e ${e.getMessage}\n${e.getStackTraceString}")
    }

    def blockOn         = player.loc.block.blockBelow
    def blockAboveHead  = blockOn.nthBlockAbove(3)
    def blocksAboveHead = blockAboveHead.blocksAbove
    def blocksAround    = blockOn.blockAbove.neighborsForPlayer

    def doTo(otherPlayer: Player, f: => Unit, actionName: String){
      f
      otherPlayer  ! GREEN(s"you have been $actionName by ${player.name}")
      player       ! GREEN(s"you have $actionName ${otherPlayer.name}")
    }

    def !  (s:String)    = if(s != null) player.sendMessage(s)
    def !* (ss: String*) = ss.foreach(s => player ! s)
    def sendError(message:String) = player.sendMessage(RED(message))
    def bomb(message:String) = {
      player ! RED(message)
      throw new RuntimeException(message)
    }
    def findPlayer(name:String)(f: Player => Unit) =
      server.findPlayer(name).fold(sendError("kill could not find player: " + name))(f)
    def findPlayers(names:List[String])(f: Player => Unit) = names.foreach(n => findPlayer(n)(f))
    def ban(reason:String){ player.setBanned(true); player.kickPlayer("banned: $reason") }
    def kill(playerName:String): Unit = findPlayer(playerName)(kill)
    def kill(p:Player) = doTo(p, p.setHealth(0), "killed")
    def teleportTo(otherPlayer: Player) = player.teleport(otherPlayer)
    def teleportTo(b: Block)            = player.teleport(b.loc)
    def shockWith(message:String) {
      player.shock
      player ! message
    }
    def surface = teleportTo(world.getHighestBlockAt(loc))
  }

  implicit class RichEntityDamageByEntityEvent(e: EntityDamageByEntityEvent) {
    def damager = e.getDamager
    def damagee = e.getEntity
  }

  implicit class RichPlayerInteractEvent(e: PlayerInteractEvent) {
    def block = e.getClickedBlock
    def loc = block.loc
  }

  implicit class RichWeatherChangeEvent(e:WeatherChangeEvent) {
    def rain = e.toWeatherState
    def sun  = ! rain
  }

  def tryO[T](f: => T): Option[T] = Try(Option(f)).getOrElse(None)

  def findEntity(name:String) = Option(EntityType.fromName(name.toUpperCase)).orElse(
    Option(EntityType.valueOf(name.toUpperCase))
  )

  def findMaterial(nameOrId: String) = Option(getMaterial(nameOrId.toUpperCase)).orElse(
    tryO(getMaterial(nameOrId.toInt))
  )

  def id[T](t:T) = identity(t)

  implicit def richBoolean(b1:Boolean) = new {
    def or (b2: => Boolean) = b1 || b2
    def and(b2: => Boolean) = b1 && b2
  }

  implicit class RichColor(c: ChatColor) {
    def apply(s: String) = c + s
  }

  sealed case class Color(data:Byte){
    def wool = MaterialAndData(WOOL, Some(data))
  }

  object Color {
    val WHITE       = new Color(0)
    val ORANGE      = new Color(1)
    val MAGENTA     = new Color(2)
    val LIGHT_BLUE  = new Color(3)
    val YELLOW      = new Color(4)
    val LIGHT_GREEN = new Color(5)
    val PINK        = new Color(6)
    val GREY        = new Color(7)
    val LIGHT_GREY  = new Color(8)
    val CYAN        = new Color(9)
    val VIOLET      = new Color(10)
    val BLUE        = new Color(11)
    val BROWN       = new Color(12)
    val GREEN       = new Color(13)
    val RED         = new Color(14)
    val BLACK       = new Color(15)
  }

  case class MaterialAndData(m: Material, data: Option[Byte]){
    def update(b: Block): Unit = {
      b changeTo m
      data.foreach(b.setData)
    }
    def itemStack = data.fold(new ItemStack(m))(new ItemStack(m, 1, 0:Short, _))
  }

  implicit class RichFile(f:File){
    def child(name:String) = new File(f, name)
    def slurp: String      = Source.fromFile(f).getLines().mkString("\n")
  }
}
