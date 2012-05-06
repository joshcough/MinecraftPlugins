package jcdc.pluginfactory

import org.bukkit.Location
import ch.spacebase.npccreatures.npcs.NPCType
import ch.spacebase.npccreatures.npcs.entity.NPC
import ch.spacebase.npccreatures.NPCCreatures

/**
 * helpful urls:
 * http://dev.bukkit.org/server-mods/npccreatures/
 * https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/NPCManager.java
 * https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/nms/NPCNetHandler.java
 * https://github.com/CitizensDev/Citizens/blob/master/src/core/net/citizensnpcs/resources/npclib/CraftNPC.java
 * https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/Entity.java
 */
trait NPCPlugin extends ScalaPlugin with MinecraftParsers {
  private lazy val npcManager =
    server.getPluginManager.getPlugin("NPCCreatures").asInstanceOf[NPCCreatures].getNPCManager
  def npcType = token("npc-type"){ (_, s) => Option(NPCType.getByName(s)) }

  def despawn(npc:NPC){ npcManager.despawnById(npc.getNPCId) }
  private def spawn(name:String, loc: Location, npcType:NPCType): NPC =
    this.npcManager.spawnNPC(name, loc,npcType)

  def human(name: String, loc:Location)       = spawn(name, loc, NPCType.HUMAN)
  def wolf (name: String, loc:Location)       = spawn(name, loc, NPCType.WOLF)
  def sheep(name: String, loc:Location)       = spawn(name, loc, NPCType.SHEEP)
  def villager(name: String, loc:Location)    = spawn(name, loc, NPCType.VILLAGER)
  def zombie(name: String, loc:Location)      = spawn(name, loc, NPCType.ZOMBIE)
  def spider(name: String, loc:Location)      = spawn(name, loc, NPCType.SPIDER)
  def skeleton(name: String, loc:Location)    = spawn(name, loc, NPCType.SKELETON)
  def creeper(name: String, loc:Location)     = spawn(name, loc, NPCType.CREEPER)
  def enderman(name: String, loc:Location)    = spawn(name, loc, NPCType.ENDERMAN)
  def blaze(name: String, loc:Location)       = spawn(name, loc, NPCType.BLAZE)
  def magmacube(name: String, loc:Location)   = spawn(name, loc, NPCType.MAGMACUBE)
  def pigzombie(name: String, loc:Location)   = spawn(name, loc, NPCType.PIGZOMBIE)
  def cavespider(name: String, loc:Location)  = spawn(name, loc, NPCType.CAVESPIDER)
  def slime(name: String, loc:Location)       = spawn(name, loc, NPCType.SLIME)
  def silverfish(name: String, loc:Location)  = spawn(name, loc, NPCType.SILVERFISH)
  def snowman(name: String, loc:Location)     = spawn(name, loc, NPCType.SNOWMAN)
  def pig(name: String, loc:Location)         = spawn(name, loc, NPCType.PIG)
  def chicken(name: String, loc:Location)     = spawn(name, loc, NPCType.CHICKEN)
  def cow(name: String, loc:Location)         = spawn(name, loc, NPCType.COW)
  def squid(name: String, loc:Location)       = spawn(name, loc, NPCType.SQUID)
  def mooshroom(name: String, loc:Location)   = spawn(name, loc, NPCType.MOOSHROOM)
  def enderdragon(name: String, loc:Location) = spawn(name, loc, NPCType.ENDERDRAGON)
  def ghast(name: String, loc:Location)       = spawn(name, loc, NPCType.GHAST)
  def giant(name: String, loc:Location)       = spawn(name, loc, NPCType.GIANT)
  def ocelot(name: String, loc:Location)      = spawn(name, loc, NPCType.OCELOT)
  def irongolem(name: String, loc:Location)   = spawn(name, loc, NPCType.IRONGOLEM)

  def spawn(id:Long, loc: Location, breed: String): NPC = (breed.toLowerCase match {
    case "wolves"       | "wolf"       => wolf  _
    case "sheep"                       => sheep _
    case "villagers"    | "villager"   => villager _
    case "zombies"      | "zombie"     => zombie _
    case "spiders"      | "spider"     => spider _
    case "skeletons"    | "skeleton"   => skeleton _
    case "creepers"     | "creeper"    => creeper _
    case "endermen"     | "enderman"   => enderman _
    case "blazes"       | "blaze"      => blaze _
    case "magmacubes"   | "magmacube"  => magmacube _
    case "pigzombies"   | "pigzombie"  => pigzombie _
    case "cavespiders"  | "cavespider" => cavespider _
    case "slimes"       | "slime"      => slime _
    case "silverfish"                  => silverfish _
    case "snowmen"      | "snowman"    => snowman _
    case "pigs"         | "pig"        => pig _
    case "chickens"     | "chicken"    => chicken _
    case "cows"         | "cow"        => cow _
    case "squids"       | "squid"      => squid _
    case "mooshrooms"   | "mooshroom"  => mooshroom _
    case "ghasts"       | "ghast"      => ghast _
    case "giants"       | "giant"      => giant _
    case "ocelots"      | "ocelot"     => ocelot _
    case "irongolems"   | "irongolem" | "golems" | "golem"     => irongolem _
    case "enderdragons" | "enderdragon" | "dragons" | "dragon" => enderdragon _
    case _        => human _
  })(id.toString, loc)
}

/**
//  object NPCHuman{
//    def apply(world:World, loc:Location, name: String): CraftPlayer = {
//      val npc = NPCHuman(world.worldServer, name)
//      npc.setLocation(loc.x, loc.y, loc.z, 0, 0)
//      world.worldServer.addEntity(npc)
//      new CraftPlayer(craftServer, npc)
//    }
//    def apply(world: MCWorld, name:String): NPCHuman =
//      new NPCHuman(craftServer.getHandle.server, world, name, new ItemInWorldManager(world))
//  }
//
//  class NPCHuman(server: MinecraftServer, world: MCWorld, name:String, im: ItemInWorldManager)
//    extends EntityPlayer(server, world, name, im) {
//    im.b(1)
//    this.netServerHandler = new NPCNetHandler(this)
//    override def move(arg0: Double, arg1: Double, arg2: Double) { setPosition(arg0, arg1, arg2) }
//  }
//
//  object NPCWolf {
//    def apply(world:World, loc:Location): CraftLivingEntity = {
//      val npcEntity = new EntityWolf(world.worldServer)
//      npcEntity.setLocation(loc.x, loc.y, loc.z, 0, 0)
//      world.worldServer.addEntity(npcEntity)
//      new CraftLivingEntity(craftServer, npcEntity)
//    }
//  }
//
////  case class NPCWolf(w:MCWorld) extends EntityWolf(w) {
//////    def setBukkitEntity(entity:org.bukkit.entity.Entity) { this.bukkitEntity = entity }
////    override def move(arg0:Double, arg1:Double, arg2:Double) {}
////    override def damageEntity(source:DamageSource, damage:Int) = false
////    override def die() {}
////    override def createChild(entityanimal:EntityAnimal): EntityAnimal = null
////    override def findTarget: Entity = null
////    override def setTamed(flag: Boolean) {}
////    // PathFinding
////    override def d_() {}
////    // Stroll
////    override def G() {}
////    // Lightning
////    override def a(entityweatherlighting:EntityWeatherLighting) {}
////    // Targeting
////    private def c(entity:Entity,f:Float) {}
////    // Attack?
////    override def a(entity:Entity,f:Float) {}
////    // Taming?
////    override def b(entityhuman:EntityHuman) = false
////    override def mate(entityanimal:EntityAnimal) = false
////    // Movement?
////    //    override def e() {
////    //      try {
////    //        val loc = this.getBukkitEntity.loc
////    //        val players = world.getWorld.getPlayers
////    //
////    //        final List<Player> players = this.world.getWorld().getPlayers();
////    //        final Packet34EntityTeleport packet = new Packet34EntityTeleport(this);
////    //
////    //      for (Player player : players) {
////    //        if (player.getLocation().distanceSquared(loc) < 4096) {
////    //          ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(packet);
////    //        }
////    //      }
////    //      } catch (ConcurrentModificationException ex) {
////    //      }
////    //    }
////}
//
//
//  // junk below needed so stuff doesnt bomb. todo: maybe some can be removed.
//  class NPCNetHandler(p: EntityPlayer) extends NetServerHandler(
//    craftServer.getHandle.server,
//    new NetworkManager(
//      new java.net.Socket {
//        override def getInputStream: java.io.InputStream = new java.io.ByteArrayInputStream(Array(1: Byte))
//        override def getOutputStream: java.io.OutputStream = new java.io.ByteArrayOutputStream()
//      }, "NPC Manager", new NetHandler() {
//        override def c = true
//      }) {
//
//      try {
//        val f = classOf[NetworkManager].getDeclaredField("l")
//        f.setAccessible(true)
//        f.set(this, false)
//      } catch {
//        case e => e.printStackTrace()
//      }
//      override def a(nethandler: NetHandler) {}
//      override def queue(packet: Packet) {}
//      //def a(s:String, aobject:Array[Object]) {}
//      override def a() {}
//    }, p) {
//
//    // Fake player prevents spout NPEs
//    override def getPlayer: CraftPlayer =
//      new CraftPlayer(Bukkit.getServer.asInstanceOf[CraftServer], player)
//
//    override def a() {}
//    override def a(packet10flying:Packet10Flying) {}
//    override def sendMessage(s:String) {}
//    override def a(d0:Double,d1:Double,d2:Double, f:Float, f1:Float) {}
//    override def a(packet14blockdig:Packet14BlockDig) {}
//    override def a(packet15place:Packet15Place) {}
//    override def a(s:String, aobject:Array[Object]) {}
//    override def onUnhandledPacket(packet:Packet) {}
//    override def a(packet16blockitemswitch:Packet16BlockItemSwitch) {}
//    override def a(packet3chat:Packet3Chat) {}
//    override def a(packet18armanimation:Packet18ArmAnimation) {}
//    override def a(packet19entityaction:Packet19EntityAction) {}
//    override def a(packet255kickdisconnect:Packet255KickDisconnect) {}
//    override def sendPacket(packet:Packet) {}
//    override def a(packet7useentity:Packet7UseEntity) {}
//    override def a(packet9respawn:Packet9Respawn) {}
//    override def handleContainerClose(packet101closewindow:Packet101CloseWindow) {}
//    override def a(packet102windowclick:Packet102WindowClick) {}
//    override def a(packet106transaction:Packet106Transaction) {}
//    override def a(packet130updatesign:Packet130UpdateSign) {}
//  }
**/