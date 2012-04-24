package jcdc.pluginfactory.examples

import jcdc.pluginfactory.{CommandsPlugin, Cube, ListenersPlugin}
import org.bukkit.Material
import Material._

class WorldEditV2 extends ListenersPlugin with CommandsPlugin with CubePlugin with JCDCPluginFactoryExample {

  val listeners = List(
    OnLeftClickBlock((p, e) => if (p isHoldingA WOOD_AXE) {
      setFirstPosition(p, e.getClickedBlock)
      e.cancel
    }),
    OnRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) setSecondPosition(p, e.getClickedBlock))
  )

  val commands = List(
    Command("/wand",  "Get a WorldEdit wand.",   noArgs(_.loc.dropItem(WOOD_AXE))),
    Command("/pos1",  "Set the first position",  noArgs(p => setFirstPosition(p, p.loc))),
    Command("/pos2",  "Set the second position", noArgs(p => setSecondPosition(p, p.loc))),
    Command("/erase", "Set all the selected blocks to air.", noArgs(run(_)(_.erase))),
    Command(
      name = "/set",
      desc = "Set all the selected blocks to the given material type.",
      body = args(material) { case p ~ m => run(p)(_ set m) }
    ),
    Command(
      name = "/change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material) { case p ~ (oldM ~ newM) => run(p)(_.change(oldM, newM)) }
    ),
    Command(
      name = "/walls",
      desc = "Create walls with the given material type.",
      body = args(material) { case p ~ m => run(p)(_.walls.foreach(_ changeTo m)) }
    ),
    Command(
      name = "/empty-tower",
      desc = "Create walls and floor with the given material type, and set everything inside to air.",
      body = args(material) { case p ~ m => run(p)(cube =>
        for(b <- cube.blocks) if (cube.onWall(b) or cube.onFloor(b)) b changeTo m else b.erase
      )}
    ),
    Command(
      name = "/dig",
      desc = "Dig",
      body = args(oddNum ~ num) { case p ~ (radius ~ depth) =>
        val b = radius / 2
        val (x, y, z) = p.loc.xyzd
        Cube(p.world(x + b, y, z + b), p.world(x - b, y - depth, z - b)).erase
      }
    )
  )
}
