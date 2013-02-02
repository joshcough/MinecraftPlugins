package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, ListenersPlugin}
import org.bukkit.Material._

class DoorPasswords extends ListenersPlugin with CommandsPlugin {
  val listeners = List(
    OnLeftClickBlock ((p, e) => if(e.getClickedBlock is IRON_DOOR) p ! "punched a door!")
  )

  val commands = List(
    Command("protect", "Put a combination on a door", )(_.loc.dropItem(WOOD_AXE))
  )
}