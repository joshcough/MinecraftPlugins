package jcdc.pluginfactory.examples

import org.bukkit.Material
import Material._
import jcdc.pluginfactory.{MineCraftCube, Cubes, CommandsPlugin, ListenersPlugin}
import MineCraftCube._

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
class WorldEdit extends ListenersPlugin with CommandsPlugin with Cubes {

  lazy val tasks = new PlayerTasks
  import tasks._

  val listeners = List(
    OnLeftClickBlock ((p, e) => if(p isHoldingA WOOD_AXE){ setFirstPosition (p, e.loc); e.cancel }),
    OnRightClickBlock((p, e) => if(p isHoldingA WOOD_AXE){ setSecondPosition(p, e.loc) })
  )

  val commands = List(
    Command("wand", "Get a WorldEdit wand.")(_.loc.dropItem(WOOD_AXE)),
    Command("pos1", "Set the first position", location.?){ case (p, loc) =>
      setFirstPosition (p, loc.fold(p.loc)(_(p.world)))
    },
    Command("pos2", "Set the second position", location.?){ case (p, loc) =>
      setSecondPosition(p, loc.fold(p.loc)(_(p.world)))
    },
    Command("cube-to",  "Set both positions",  location ~ location.?){ case (p, loc1 ~ loc2) =>
      setFirstPosition (p, loc1(p.world))
      setSecondPosition(p, loc2.fold(p.loc)(_(p.world)))
    },
    Command("between",  "Set both positions",  location ~ "-" ~ location){ case (p, loc1 ~_~ loc2) =>
      setFirstPosition (p, loc1(p.world))
      setSecondPosition(p, loc2(p.world))
      p teleport loc1(p.world)
    },
    Command("erase", "Set all the selected blocks to air.")(cube(_).eraseAll),
    Command(
      name = "set",
      desc = "Set all the selected blocks to the given material type.",
      args = material)(
      body = { case (p, m) =>  p ! s"${cube(p) setAll m} blocks updated." }
    ),
    Command(
      name = "change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      args = material ~ material)(
      body = { case (p, oldM ~ newM) => p ! s"${cube(p).changeAll(oldM, newM)} blocks updated." }
    ),
    Command(
      name = "find",
      desc = "Checks if your cube contains any of the given material, and tells where.",
      args = material)(
      body = { case (p, m) =>
        cube(p).blocks.find(_ is m).fold(s"No $m found in your cube!")(b => s"$m found at ${b.loc.xyz}")
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
      name = "cycle-walls",
      desc =
        "Create walls, and cycle the walls material between the given materials, " +
        "in a span of N seconds.",
      args = int ~ material ~ material.+)(
      body = { case (p, period ~ initialMaterial ~ materials) =>
        val c = cube(p)
        val allMaterials = initialMaterial :: materials
        def initialDelay(index: Int) = index * period * 20 / allMaterials.size
        for((m, i) <- allMaterials.zipWithIndex)
          p.scheduleSyncRepeatingTask(initialDelay = initialDelay(i), period = period * 20){
            c.walls.foreach(_ changeTo m)
          }
      }
    ),
    Command(
      name = "random-house",
      desc = "make all the blocks in your house change at random!",
      args = material ~ material.+)(
      body = { case (p, initialMaterial ~ materials) =>
        val c = cube(p)
        val allMaterials = (initialMaterial :: materials).toArray
        p.scheduleSyncRepeatingTask(initialDelay = 0, period = 20){
          c.shell.foreach(_ changeTo (allMaterials((math.random * allMaterials.size).toInt)))
        }
        p.scheduleSyncRepeatingTask(initialDelay = 0, period = 10){
          c.shrink(1, 1, 1).corners.foreach(_ changeTo TORCH)
        }
      }
    ),
    Command(
      name = "wave",
      desc = "Create an awesome wave made of any material.",
      args = length ~ height ~ material)(
      body = { case (p, length ~ height ~ m) =>
        val startX     = p.x
        val minHeight  = p.y
        val maxHeight  = minHeight + height
        val z          = p.z
        val up         = true
        val down       = false
        val directions = Array.fill(length)(up)
        def highestBlock(n: Int) = p.world.getHighestBlockAt(n + startX, z)

        for (n <- 0 to length)
          p.scheduleSyncRepeatingTask(initialDelay = n * 10, period = 10 /* every .5 second */ ){
            def hb           = highestBlock(n)
            def ascending    = directions(n)
            def descending   = ! ascending
            def startAscent  = directions(n) = up
            def startDescent = directions(n) = down
            def atTop        = hb.y >= maxHeight
            def atBottom     = hb.y <= minHeight
            if(ascending && atTop) startDescent else if(descending && atBottom) startAscent
            if(ascending) hb changeTo m else hb.blockBelow changeTo AIR
          }
      }
    ),
    Command(
      name = "empty-tower",
      desc = "Create walls and floor with the given material type, and set everything inside to air.",
      args = material)(
      body = { case (p, m) =>
        val c = cube(p)
        for(b <- cube(p).blocks) if (c.onWall(b.coor) || c.onFloor(b.coor))
          b changeTo m else b.erase
      }
    ),
    Command(
      name = "dig",
      desc = "Dig",
      args = oddNum ~ int)(
      body = { case (p, radius ~ depth) =>
        val b = radius / 2
        val (x, y, z) = p.loc.xyzd
        MineCraftCube(p.world(x + b, y, z + b).loc, p.world(x - b, y - depth, z - b).loc).eraseAll
      }
    ),
    Command("goto", "Teleport!", location){ case (you, loc) => you teleport loc(you.world) },
    Command("paste", "Paste your cube at your current location!"){ p =>
      val pasteCount = cube(p).paste(p.loc)
      p ! s"$pasteCount blocks updated."
    }
  )
}
