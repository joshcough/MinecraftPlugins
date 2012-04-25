package jcdc.pluginfactory.examples

import jcdc.pluginfactory.CommandsPlugin
import org.bukkit.entity.Player
import net.minecraft.server.{MinecraftServer, EntityPlayer, World => MCWorld, ItemInWorldManager}
import org.bukkit.craftbukkit.CraftServer

class NPC extends CommandsPlugin {

  val commands = List(
    Command("spawn", "Spawn a human", noArgs(spawnNPC))
  )

  def spawnNPC(p:Player): Unit = {
    val h = NPCHuman(p.world.worldServer, "joshcough")
    h.setLocation(p.x + 2, p.y, p.z + 2, 0, 0)
    p.world.worldServer.addEntity(h)
  }
}

object NPCHuman{
  def server = org.bukkit.Bukkit.getServer.asInstanceOf[CraftServer].getHandle.server
  def apply(world: MCWorld, name:String) = new NPCHuman(server, world, name)
}

class NPCHuman(server: MinecraftServer, world: MCWorld, name:String)
  extends EntityPlayer(server, world, name, new ItemInWorldManager(world)) {
  override def move(arg0: Double, arg1: Double, arg2: Double) { setPosition(arg0, arg1, arg2) }
}