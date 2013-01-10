package jcdc.pluginfactory.examples

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import Material._
import jcdc.pluginfactory.{
  Cube, CubeState, CommandsPlugin, ListenersPlugin, MineCraftCube, UndoState
}
import MineCraftCube._

//GIANT TODO: force some of the streams in mirroring!

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

  import Changer._

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
      body = { case (p, m) => p.newChange(setAll(cube(p), (m))) }
    ),
    Command(
      name = "replace",
      desc = "Replace all the selected blocks of the first material type to the second material type.",
      args = material ~ material)(
      body = { case (p, oldM ~ newM) => p.newChange(changeAll(cube(p), oldM, newM)) }
    ),
    Command(
      name = "undo",
      desc = "undo!",
      args = ("on" or "off") or eof)(
      body = {
        case (p, Left(Left("on")))   => undoManager.turnOn
        case (p, Left(Right("off"))) => undoManager.turnOff
        case (p, _)                  => if(undoManager.on) p.undo else p ! "undo is off!"
      }
    ),
    Command(name = "redo", desc = "redo!")(body = p => if(undoManager.on) p.redo else p ! "undo is off!"),
    Command("paste", "Paste your cube at your current location!"){ p =>
      p.newChange(cube(p).translateTo(p.loc.coor))
    },
//    Command("move", "Move your cube to your current location!"){ p =>
//      p.newChange(cube(p).move(p.loc)) //paste(newL1) ++ setAll(Material.AIR)
//    },
    Command("flip", "Flip your cube upside down!"){ p => p.newChange(cube(p).mirrorY) },
    Command("flipxz", "Flip x and z"){ p =>
      p.newChange(cube(p).mirrorX.mirrorZ)
    },
    Command("paste-mirror-y", "paste your cube somewhere, but flipped upside down!"){ p =>
      p.newChange(cube(p).translateTo(p.loc.coor).mirrorY)
    },
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
    Command("erase", "Set all the selected blocks to air.")(p => p.newChange(eraseAll(cube(p)))),

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
          eraseAll(MineCraftCube(p.world(x + b, y, z + b).loc, p.world(x - b, y - depth, z - b).loc))
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
    var on = true
    def turnOn{ on = true }
    def turnOff{ on = false; state.clear() }
    import collection.mutable.Map
    val initialState = UndoState[T, U]()
    val state = Map[P, UndoState[T, U]]().withDefaultValue(initialState)
    def newChange(p: P, c: T): T = { state += (p -> state(p).newChange(c)); c }
    def undo(p: P)(f: T => U): Unit = state(p).undo(f).foreach { ns => state += (p -> ns) }
    def redo(p: P)(f: U => T): Unit = state(p).redo(f).foreach { ns => state += (p -> ns) }
  }

  implicit class RichPlayerWithChanges(p: Player){
    def newChange(cs: Changes): Unit = updated(undoManager.newChange(p, cs))
    def newChange(ps: Seq[PotentialChange]): Unit = newChange(runChanges(ps))
    def newChange(ps: Cube[Block]): Unit = newChange(runChanges(Changer run ps))
    def undo: Unit = undoManager.undo(p)(rerun)
    def redo: Unit = undoManager.redo(p)(rerun)
    def rerun(cs: Changes) = updated(runChanges(cs.cs.map(PotentialChange(_))))
    def updated(cs: Changes): Changes = { p ! s"${cs.size} blocks updated."; cs }
  }

  object Changer {

    case class Change(b: Block, oldM: MaterialAndData){
      override def toString = s"Change(b:${b.loc.xyz} m:${oldM.m.name})"
    }

    object PotentialChange {
      def apply(c: Change) = new PotentialChange(c.b, c.oldM)
      def apply(b: Block, m: Material) = new PotentialChange(b, m.andData)
    }

    case class PotentialChange(b: Block, newM: MaterialAndData){
      val oldM = b.materialAndData
      def run: Boolean = newM update b
    }

    case class Changes(cs:Array[Change]){
      override def toString = cs.toList.mkString(",")
      def size = cs.length
      def ++(cs: Changes) = Changes(this.cs ++ cs.cs)
    }

    type PotentialChanges = Stream[PotentialChange]

    def runChanges(newData: Seq[PotentialChange]): Changes =
      Changes(newData.filter(_.run).map(p => Change(p.b, p.oldM)).toArray)

    def run(c: Cube[Block]): Stream[PotentialChange] =
      c.toZippedStream.map(t => (PotentialChange(c.world(t._1.x, t._1.y, t._1.z), t._2.materialAndData)))

    def setAll(c: Cube[Block], newM: Material): Changes = setAll(c, new MaterialAndData(newM, None))
    def setAll(c: Cube[Block], newM: MaterialAndData): Changes = changeAll(c.blocks, newM)

    def changeAll(bms: Stream[Block], newM: MaterialAndData) = runChanges(
      bms.zip(Stream.continually(newM)).map{ case (b,n) => PotentialChange(b,n) }
    )

    def changeAll(c: Cube[Block], oldM: Material, newM: Material): Changes =
      changeAll(c, oldM, new MaterialAndData(newM, None))

    def changeAll(c: Cube[Block], oldM: Material, newM: MaterialAndData): Changes =
      changeAll(c.blocks.filter(_ is oldM), newM)

    def eraseAll(c: Cube[Block]): Changes = changeAll(c.blocks, MaterialAndData.AIR)
  }
}




//  case class PotentialSwap(b1: Block, b2: Block){
//    def run: Seq[Change] = {
//      val oldB1M = b1.materialAndData
//      val oldB2M = b2.materialAndData
//      List(
//        oldB1M.update(b2).toOption(Change(b2, oldB2M)),
//        oldB2M.update(b1).toOption(Change(b1, oldB1M))
//      ).flatten
//    }
//  }
//    def runSwaps(swaps: Seq[PotentialSwap]): Changes = Changes(swaps.flatMap(_.run).toArray)


//
//    def paste(newL1: Location): Changes = Changer.runChanges(run(c.paste(newL1.coor)))
//    def pasteMirrorY(newL1: Location): Changes = Changer.runChanges(run(c.paste(newL1.coor).mirrorY))

//    /**
//     * We have to force these (.force), because if they are run lazily,
//     * then a will be replaced with b, and later when b trieds to get replaced with a,
//     * a's material type is already what b is, so b just gets set to itself.
//     * Forcing guarantees that we get the right data values in the list.
//     * @return
//     */
//    def mirrorXChanges: Changes = Changer.runChanges(run(c.mirrorX).force)
//    def mirrorYChanges: Changes = Changer.runChanges(run(c.mirrorY).force)
//    def mirrorZChanges: Changes = Changer.runChanges(run(c.mirrorZ).force)
//
//    /**
//     * @param newL1
//     */
//    def move(newL1: Location): Changes = paste(newL1) ++ setAll(Material.AIR)
