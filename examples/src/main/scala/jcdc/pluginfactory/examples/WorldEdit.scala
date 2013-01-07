package jcdc.pluginfactory.examples

import org.bukkit.Material
import Material._
import jcdc.pluginfactory.{
  MineCraftCube, CubeState, CommandsPlugin, ListenersPlugin, UndoState
}
import MineCraftCube._
import org.bukkit.entity.Player

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
class WorldEdit extends ListenersPlugin with CommandsPlugin with CubeState {

  lazy val tasks = new PlayerTasks
  import tasks._

  val undoManager = new UndoManager[Player, Changes, Changes]

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
    Command(
      name = "set",
      desc = "Set all the selected blocks to the given material type.",
      args = material)(
      body = { case (p, m) => p.newChange(cube(p).setAll(m)) }
    ),
    Command(
      name = "replace",
      desc = "Replace all the selected blocks of the first material type to the second material type.",
      args = material ~ material)(
      body = { case (p, oldM ~ newM) => p.newChange(cube(p).changeAll(oldM, newM)) }
    ),
    Command(name = "undo", desc = "undo!")(body = p => p.undo),
    Command(name = "redo", desc = "redo!")(body = p => p.redo),
    Command("paste", "Paste your cube at your current location!"){ p =>
      p.newChange(cube(p).paste(p.loc))
    },
    Command("flip", "Flip your cube upside down!"){ p => p.newChange(cube(p).mirrorYChanges) },
    Command("goto", "Teleport!", location){ case (you, loc) => you teleport loc(you.world) },
    Command("cube-to",  "Set both positions",  location ~ location.?){ case (p, loc1 ~ loc2) =>
      setFirstPosition (p, loc1(p.world))
      setSecondPosition(p, loc2.fold(p.loc)(_(p.world)))
    },
    Command("between",  "Set both positions",  location ~ "-" ~ location){ case (p, loc1 ~_~ loc2) =>
      setFirstPosition (p, loc1(p.world))
      setSecondPosition(p, loc2(p.world))
      p teleport loc1(p.world)
    },
    Command(
      name = "find",
      desc = "Checks if your cube contains any of the given material, and tells where.",
      args = material)(
      body = { case (p, m) =>
        cube(p).blocks.find(_ is m).fold(s"No $m found in your cube!")(b => s"$m found at ${b.loc.xyz}")
      }
    ),
    Command("erase", "Set all the selected blocks to air.")(p => p.newChange(cube(p).eraseAll)),

    Command(
      name = "fib-tower",
      desc = "create a tower from the fib numbers",
      args = int ~ material){ case (p, i ~ m) =>
      lazy val fibs: Stream[Int] = 0 #:: 1 #:: fibs.zip(fibs.tail).map{case (i,j) => i+j}
      p.newChange(for {
        (startBlock,n) <- p.world.fromX(p.loc).zip(fibs take i)
        towerBlock     <- startBlock.andBlocksAbove take n
      } yield PotentialChange(towerBlock, m))
    },
    Command(
      name = "walls",
      desc = "Create walls with the given material type.",
      args = material)(
      body = { case (p, m) => p.newChange(cube(p).walls.map(PotentialChange(_, m))) }
    ),
    Command(
      name = "empty-tower",
      desc = "Create walls and floor with the given material type, and set everything inside to air.",
      args = material)(
      body = { case (p, m) =>
        p.newChange(for(b <- cube(p).blocks) yield PotentialChange(b,
          if (cube(p).onWall(b.coor) || cube(p).onFloor(b.coor)) m else AIR)
        )
      }
    ),
    Command(
      name = "dig",
      desc = "Dig",
      args = oddNum ~ int)(
      body = { case (p, radius ~ depth) =>
        val b = radius / 2
        val (x, y, z) = p.loc.xyzd
        p.newChange(
          MineCraftCube(p.world(x + b, y, z + b).loc, p.world(x - b, y - depth, z - b).loc).eraseAll
        )
      }
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
    )
  )

  class UndoManager[P, T, U] {
    import collection.mutable.Map
    val initialState = UndoState[T, U]()
    val state = Map[P, UndoState[T, U]]().withDefaultValue(initialState)
    def newChange(p: P, c: T): T = { state += (p -> state(p).newChange(c)); c }
    def undo(p: P)(f: T => U): Unit = state(p).undo(f).foreach { ns => state += (p -> ns) }
    def redo(p: P)(f: U => T): Unit = state(p).redo(f).foreach { ns => state += (p -> ns) }
  }

  implicit class RichPlayerWithChanges(p: Player){
    def newChange(cs: Changes): Unit = updated(undoManager.newChange(p, cs))
    def newChange(ps: Seq[PotentialChange]): Unit = newChange(Changer runChanges ps)
    def undo: Unit = undoManager.undo(p)(rerun)
    def redo: Unit = undoManager.redo(p)(rerun)
    def rerun(cs: Changes) = updated(Changer runChanges cs.cs.map(PotentialChange(_)))
    def updated(cs: Changes): Changes = { p ! s"${cs.size} blocks updated."; cs }
  }
}
