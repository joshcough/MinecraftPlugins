package jcdc.pluginfactory.examples

import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import Material._
import jcdc.pluginfactory.{CommandsPlugin, Cube, ListenersPlugin}

/**
 * Classic WorldEdit plugin, done in Scala.
 *
 * This plugin allows you to manipulate the world in various ways.
 *
 * To do this, first set two corners of the world by:
 *   Using the /wand command to get a wooden axe.
 *   Left clicking on the first corner with a wooden axe.
 *   Right clicking on the second corner with a wooden axe.
 *   Or alternatively, using the pos1 and pos2 commands.
 *
 * After you've set your two corners, you can manipulate blocks in that cube.
 *
 * Popular world manipulation commands are:
 *
 *   /set material: sets all the blocks in the cube to the given material
 *   /change m1 m2: changes all the blocks of type m1 to m2
 *   /walls material: sets the walls of the cube to the given material
 *
 * Have a look through the code, or navigate the help menu for more info on the commands.
 */
class WorldEdit extends ListenersPlugin with CommandsPlugin {

  val corners = collection.mutable.Map[Player, List[Location]]().withDefaultValue(Nil)

  val listeners = List(
    OnLeftClickBlock ((p, e) => if(p isHoldingA WOOD_AXE) { setFirstPos (p, e.loc); e.cancel }),
    OnRightClickBlock((p, e) => if(p isHoldingA WOOD_AXE) { setSecondPos(p, e.loc) })
  )

  val commands = List(
    Command("wand", "Get a WorldEdit wand.")(_.loc.dropItem(WOOD_AXE)),
    Command("pos1", "Set the first position", location.?){ case (p, loc) =>
      setFirstPos (p, loc.fold(p.loc)(_(p.world)))
    },
    Command("pos2", "Set the second position", location.?){ case (p, loc) =>
      setSecondPos(p, loc.fold(p.loc)(_(p.world)))
    },
    Command("cube-to",  "Set both positions",  location ~ location.?){ case (p, loc1 ~ loc2) =>
      setFirstPos (p, loc1(p.world))
      setSecondPos(p, loc2.fold(p.loc)(_(p.world)))
    },
    Command("between",  "Set both positions",  location ~ "-" ~ location){ case (p, loc1 ~_~ loc2) =>
      setFirstPos (p, loc1(p.world))
      setSecondPos(p, loc2(p.world))
      p teleport loc1(p.world)
    },
    Command("erase", "Set all the selected blocks to air.")(cube(_).eraseAll),
    Command(
      name = "set",
      desc = "Set all the selected blocks to the given material type.",
      args = material)(
      body = { case (p, m) => for(b <- cube(p)) b changeTo m }
    ),
    Command(
      name = "change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      args = material ~ material)(
      body = { case (p, oldM ~ newM) => for(b <- cube(p); if(b is oldM)) b changeTo newM }
    ),
    Command(
      name = "find",
      desc = "Checks if your cube contains any of the given material, and tells where.",
      args = material)(
      body = { case (p, m) =>
        cube(p).find(_ is m).fold(s"No $m found in your cube!")(b => s"$m found at ${b.loc.xyz}")
      }
    ),
    Command(
      name = "fib-tower",
      desc = "create a tower from the fib numbers",
      args = int ~ material){ case (p, i ~ m) =>
      lazy val fibs: Stream[Int] = 0 #:: 1 #:: fibs.zip(fibs.tail).map{case (i,j) => i+j}
      for {
        (startBlock,n) <- p.world.fromX(p.loc).zip(fibs take i)
        towerBlock     <- startBlock.andBlocksAbove take n
      } towerBlock changeTo m
    },
    Command(
      name = "walls",
      desc = "Create walls with the given material type.",
      args = material)(
      body = { case (p, m) => cube(p).walls.foreach(_ changeTo m) }
    ),
    Command(
      name = "crazy-walls",
      desc = "Create walls, and change them between several different materials, every N seconds.",
      args = int ~ material ~ material.+)(
      body = { case (p, period ~ initialMaterial ~ materials) => {
        val allMaterials = initialMaterial :: materials
        def initialDelay(index: Int) = index * period * 20 / allMaterials.size
        for((m, i) <- allMaterials.zipWithIndex)
          scheduleSyncRepeatingTask(initialDelay = initialDelay(i), period = period * 20){
            cube(p).walls.foreach(_ changeTo m)
          }
      }}
    ),
    Command(
      name = "empty-tower",
      desc = "Create walls and floor with the given material type, and set everything inside to air.",
      args = material)(
      body = { case (p, m) =>
        val c = cube(p)
        for(b <- cube(p)) if (c.onWall(b) or c.onFloor(b)) b changeTo m else b.erase
      }
    ),
    Command(
      name = "dig",
      desc = "Dig",
      args = oddNum ~ int)(
      body = { case (p, radius ~ depth) =>
        val b = radius / 2
        val (x, y, z) = p.loc.xyzd
        Cube(p.world(x + b, y, z + b).loc, p.world(x - b, y - depth, z - b).loc).eraseAll
      }
    ),
    Command("goto", "Teleport!", location){ case (you, loc) => you teleport loc(you.world) }
  )

  def cube(p:Player): Cube = corners.get(p).filter(_.size == 2).
    flipFold(ls => Cube(ls(0), ls(1)))(p bomb "Both corners must be set!")

  def setFirstPos(p:Player,loc: Location): Unit = {
    corners.update(p, List(loc))
    p ! s"first corner set to: ${loc.xyz}"
  }

  def setSecondPos(p:Player,loc2: Location): Unit = corners(p) match {
    case loc1 :: _ =>
      corners += (p -> List(loc1, loc2))
      p ! s"second corner set to: ${loc2.xyz}"
    case Nil =>
      p ! "set corner one first! (with a left click)"
  }
}
