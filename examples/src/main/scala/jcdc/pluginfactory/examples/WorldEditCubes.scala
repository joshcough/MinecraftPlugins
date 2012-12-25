package jcdc.pluginfactory.examples

import org.bukkit.Material
import Material._
import jcdc.pluginfactory.{CommandsPlugin, Cubes, Cube, ListenersPlugin}

// WorldEdit redone using Cubes.
// Cubes is a trait that simplifies cube creation and manipulation.
class WorldEditCubes extends ListenersPlugin with CommandsPlugin with Cubes {

  val listeners = List(
    OnLeftClickBlock((p, e) => if (p isHoldingA WOOD_AXE) {
      setFirstPosition(p, e.loc)
      e.cancel
    }),
    OnRightClickBlock((p, e) => if (p isHoldingA WOOD_AXE) setSecondPosition(p, e.loc))
  )

  val commands = List(
    Command("/wand",  "Get a WorldEdit wand.")  (_.loc.dropItem(WOOD_AXE)),
    Command("/pos1",  "Set the first position") (p => setFirstPosition(p, p.loc)),
    Command("/pos2",  "Set the second position")(p => setSecondPosition(p, p.loc)),
    Command("/erase", "Set all the selected blocks to air.")(run(_)(_.eraseAll)),
    Command(
      name = "/set",
      desc = "Set all the selected blocks to the given material type.",
      args = material)(
      body = { case (p, m) => run(p)(_ setAll m) }
    ),
    Command(
      name = "/change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      args = material ~ material)(
      body = { case (p, oldM ~ newM) => run(p)(_.changeAll(oldM, newM)) }
    ),
    Command(
      name = "/walls",
      desc = "Create walls with the given material type.",
      args = material)(
      body = { case (p, m) => run(p)(_.walls.foreach(_ changeTo m)) }
    ),
    Command(
      name = "/empty-tower",
      desc = "Create walls and floor with the given material type, and set everything inside to air.",
      args = material)(
      body =  { case (p, m) => run(p)(cube =>
        for(b <- cube.blocks) if (cube.onWall(b) or cube.onFloor(b)) b changeTo m else b.erase
      )}
    ),
    Command(
      name = "/dig",
      desc = "Dig",
      args = oddNum ~ int)(
      body = { case (p, radius ~ depth) =>
        val b = radius / 2
        val (x, y, z) = p.loc.xyzd
        Cube(p.world(x + b, y, z + b).loc, p.world(x - b, y - depth, z - b).loc).eraseAll
      }
    )
  )
}
