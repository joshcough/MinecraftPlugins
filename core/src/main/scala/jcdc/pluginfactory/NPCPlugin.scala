package jcdc.pluginfactory

import net.minecraft.server.{World => MCWorld, _}
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.{World, Location, Bukkit}

/**
 * helpful urls:
 * http://dev.bukkit.org/server-mods/npccreatures/
 * https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/NPCManager.java
 * https://github.com/Steveice10/NPCCreatures/blob/master/src/main/java/ch/spacebase/npccreatures/npcs/nms/NPCNetHandler.java
 * https://github.com/CitizensDev/Citizens/blob/master/src/core/net/citizensnpcs/resources/npclib/CraftNPC.java
 * https://github.com/Bukkit/CraftBukkit/blob/master/src/main/java/net/minecraft/server/Entity.java
 */
trait NPCPlugin extends ScalaPlugin {

  object NPCHuman{
    def apply(world:World, loc:Location, name: String): CraftPlayer = {
      val npc = NPCHuman(world.worldServer, name)
      npc.setLocation(loc.x, loc.y, loc.z, 0, 0)
      world.worldServer.addEntity(npc)
      new CraftPlayer(craftServer, npc)
    }
    def apply(world: MCWorld, name:String): NPCHuman =
      new NPCHuman(craftServer.getHandle.server, world, name, new ItemInWorldManager(world))
  }

  class NPCHuman(server: MinecraftServer, world: MCWorld, name:String, im: ItemInWorldManager)
    extends EntityPlayer(server, world, name, im) {
    im.b(1)
    this.netServerHandler = new NPCNetHandler(this)
    override def move(arg0: Double, arg1: Double, arg2: Double) { setPosition(arg0, arg1, arg2) }
  }

  // junk below needed so stuff doesnt bomb. todo: maybe some can be removed.
  class NPCNetHandler(p: EntityPlayer) extends NetServerHandler(
    craftServer.getHandle.server,
    new NetworkManager(
      new java.net.Socket {
        override def getInputStream: java.io.InputStream = new java.io.ByteArrayInputStream(Array(1: Byte))
        override def getOutputStream: java.io.OutputStream = new java.io.ByteArrayOutputStream()
      }, "NPC Manager", new NetHandler() {
        override def c = true
      }) {

      try {
        val f = classOf[NetworkManager].getDeclaredField("l")
        f.setAccessible(true)
        f.set(this, false)
      } catch {
        case e => e.printStackTrace()
      }
      override def a(nethandler: NetHandler) {}
      override def queue(packet: Packet) {}
      //def a(s:String, aobject:Array[Object]) {}
      override def a() {}
    }, p) {

    // Fake player prevents spout NPEs
    override def getPlayer: CraftPlayer =
      new CraftPlayer(Bukkit.getServer.asInstanceOf[CraftServer], player)

    override def a() {}
    override def a(packet10flying:Packet10Flying) {}
    override def sendMessage(s:String) {}
    override def a(d0:Double,d1:Double,d2:Double, f:Float, f1:Float) {}
    override def a(packet14blockdig:Packet14BlockDig) {}
    override def a(packet15place:Packet15Place) {}
    override def a(s:String, aobject:Array[Object]) {}
    override def onUnhandledPacket(packet:Packet) {}
    override def a(packet16blockitemswitch:Packet16BlockItemSwitch) {}
    override def a(packet3chat:Packet3Chat) {}
    override def a(packet18armanimation:Packet18ArmAnimation) {}
    override def a(packet19entityaction:Packet19EntityAction) {}
    override def a(packet255kickdisconnect:Packet255KickDisconnect) {}
    override def sendPacket(packet:Packet) {}
    override def a(packet7useentity:Packet7UseEntity) {}
    override def a(packet9respawn:Packet9Respawn) {}
    override def handleContainerClose(packet101closewindow:Packet101CloseWindow) {}
    override def a(packet102windowclick:Packet102WindowClick) {}
    override def a(packet106transaction:Packet106Transaction) {}
    override def a(packet130updatesign:Packet130UpdateSign) {}
  }
}
