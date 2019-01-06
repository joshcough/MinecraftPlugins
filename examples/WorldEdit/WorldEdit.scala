package com.joshcough.minecraft.examples

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import Material._
import com.joshcough.minecraft._
import com.joshcough.minecraft.UndoState

class WorldEditPlugin extends ListenersPlugin with CommandsPlugin  {

  val cubeState = new CubeState{}

  val listeners = List(
    OnLeftClickBlock ((p, e) => if(p isHoldingA WOODEN_AXE){ cubeState.setFirstPosition (p, e.loc); e.cancel }),
    OnRightClickBlock((p, e) => if(p isHoldingA WOODEN_AXE){ cubeState.setSecondPosition(p, e.loc) })
  )
  val commands = WorldEditCommands.commands(getConfig(), cubeState)(server)
}

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
object WorldEditCommands {

  import CubeModifier._
  import CommandsPlugin._
  import org.bukkit.Server
  import org.bukkit.configuration.file.FileConfiguration

  val undoManager = new UndoManager[Player, Changes, Changes]

  def main(args: Array[String]): Unit = YMLGenerator.writeYML(args, commands(null, null)(null))

  def commands(config: FileConfiguration, cubeState: CubeState)(implicit server: Server) = {
    import cubeState._
    //import BukkitEnrichment._
    import org.bukkit.GameMode._

    // some simple useful commands
    val allCommonCommands = {
      List(
        Command("goto", "Teleport to a player.", player or location){
          case (you, Left(them)) => you.teleportTo(them)
          case (you, Right(loc)) => you.teleport(loc of you.world)
        },
        Command("day",   "Sets the time to 1."    )(_.world.setTime(1)),
        Command("night", "Sets the time to 15000.")(_.world.setTime(15000)),
        Command("s",   "Set your game mode to survival.")(_.setGameMode(SURVIVAL)),
        Command("c",   "Set your game mode to creative.")(_.setGameMode(CREATIVE))
      )
    }

    val worldEditCommands = List(
      Command("getconf", "get a config", anyString){ case (p, s) =>
        p ! config.getString(s)
      },
      Command("wand", "Get a WorldEdit wand.")(_.loc.dropItem(WOODEN_AXE)),
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
        body = { case (p, m) => p.newChange(setAll(cube(p), m)) }
      ),
      // example: /replace sand obsidian
      Command(
        name = "replace",
        desc = "Replace all the selected blocks of the first material type to the second material type.",
        args = material ~ material)(
        body = { case (p, oldM ~ newM) => p.newChange(changeAll(cube(p), oldM, newM.andData)) }
      ),
      // example: /replace-under stone air dirt
      Command(
        name = "replace-under",
        desc = "Replace all the selected blocks of the first material type under the second material type, to the third material type.",
        args = material ~ material ~ material)(
        body = { case (p, oldM ~ underM ~ newM) =>
          p.newChange(changeAllUnder(cube(p), oldM, underM, newM.andData))
        }
      ),

      Command(
        name = "undo",
        desc = "undo!",
        args = "on" or "off" or eof)(
        body = {
          case (p, Left(Left("on")))   => undoManager.turnOn
          case (p, Left(Right("off"))) => undoManager.turnOff
          case (p, _)                  => if(undoManager.on) p.undo else p ! "undo is off!"
        }
      ),
      Command(name = "redo", desc = "redo!")(body = p => if(undoManager.on) p.redo else p ! "undo is off!"),
      Command("paste", "Paste your cube at your current location!"){ p =>
        p.newChange(cube(p).translateTo(p.loc.point))
      },
      Command("move", "Move your cube to your current location!"){ p =>
        p.newChange(
          translateAll(cube(p).translateTo(p.loc.point)) ++ setAll(cube(p), Material.AIR)
        )
      },
      Command("flip", "Flip your cube upside down!"){ p => p.newChange(cube(p).mirrorY, force = true) },
      Command("flipxz", "Flip x and z"){ p =>
        p.newChange(cube(p).mirrorX.mirrorZ, force = true)
      },
      Command("paste-mirror-y", "paste your cube somewhere, but flipped upside down!"){ p =>
        p.newChange(cube(p).translateTo(p.loc.point).mirrorY, force = true)
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
            if (cube(p).onWall(b.point) || cube(p).onFloor(b.point)) m else AIR)
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
          p.newChange(eraseAll(p.world(x + b, y, z + b).cubeTo(p.world(x - b, y - depth, z - b))))
        }
      ),
      Command("goto",     "Teleport!", player(server) or location){ case (you, e) =>
        e.fold(them => you teleportTo them, loc => you.teleport(loc of you.world))
      },
      Command("up",       "Go up to the surface.")(_.surface),
      Command("set-time", "Sets the time.", time){ case (p, n) => p.world setTime n },
      Command("day",      "Sets the time to day (1000).")   (_.world setTime 1000),
      Command("night",    "Sets the time to night (15000).")(_.world setTime 15000),
    )
    allCommonCommands ++ worldEditCommands
  }

  /**
   *
   * @tparam A
   * @tparam U
   * @tparam R
   */
  class UndoManager[A, U, R] {
    var on = true
    def turnOn { on = true }
    def turnOff{ on = false; state.clear() }
    import collection.mutable.Map
    val initialState = UndoState[U, R]()
    val state = Map[A, UndoState[U, R]]().withDefaultValue(initialState)
    def newChange(a: A, u: U): U = { state += (a -> state(a).newChange(u)); u }
    def undo(p: A)(f: U => R): Unit = state(p).undo(f).foreach { ns => state += (p -> ns) }
    def redo(p: A)(f: R => U): Unit = state(p).redo(f).foreach { ns => state += (p -> ns) }
  }

  implicit class RichPlayerWithChanges(p: Player){
    def newChange(cs: Changes): Changes = notifyChange(undoManager.newChange(p, cs))
    def newChange(ps: Seq[PotentialChange]): Unit = newChange(runChanges(ps))
    def newChange(c: Cube[Block], force: Boolean = false): Unit = newChange(translateAll(c, force))
    def undo: Unit = undoManager.undo(p)(rerun)
    def redo: Unit = undoManager.redo(p)(rerun)
    def rerun(cs: Changes) = notifyChange(runChanges(cs.cs.map(PotentialChange(_))))
    def notifyChange(cs: Changes): Changes = { p ! s"${cs.size} blocks updated."; cs }
  }
}



//  lazy val tasks = new PlayerTasks
//  import tasks._

//    Command(
//      name = "cycle-walls",
//      desc =
//        "Create walls, and cycle the walls material between the given materials, " +
//          "in a span of N seconds.",
//      args = int ~ material ~ material.+)(
//      body = { case (p, period ~ initialMaterial ~ materials) =>
//        val c = cube(p)
//        val allMaterials = initialMaterial :: materials
//        def initialDelay(index: Int) = index * period * 20 / allMaterials.size
//        for((m, i) <- allMaterials.zipWithIndex)
//          p.scheduleSyncRepeatingTask(initialDelay = initialDelay(i), period = period * 20){
//            c.walls.foreach(_ changeTo m)
//          }
//      }
//    ),
//    Command(
//      name = "random-house",
//      desc = "make all the blocks in your house change at random!",
//      args = material ~ material.+)(
//      body = { case (p, initialMaterial ~ materials) =>
//        val c = cube(p)
//        val allMaterials = (initialMaterial :: materials).toArray
//        p.scheduleSyncRepeatingTask(initialDelay = 0, period = 20){
//          c.shell.foreach(_ changeTo (allMaterials((math.random * allMaterials.size).toInt)))
//        }
//        p.scheduleSyncRepeatingTask(initialDelay = 0, period = 10){
//          c.shrink(1, 1, 1).corners.foreach(_ changeTo TORCH)
//        }
//      }
//    ),
//    Command(
//      name = "wave",
//      desc = "Create an awesome wave made of any material.",
//      args = length ~ height ~ material)(
//      body = { case (p, length ~ height ~ m) =>
//        val startX     = p.x
//        val minHeight  = p.y
//        val maxHeight  = minHeight + height
//        val z          = p.z
//        val up         = true
//        val down       = false
//        val directions = Array.fill(length)(up)
//        def highestBlock(n: Int) = p.world.getHighestBlockAt(n + startX, z)
//
//        for (n <- 0 to length)
//          p.scheduleSyncRepeatingTask(initialDelay = n * 10, period = 10 /* every .5 second */ ){
//            def hb           = highestBlock(n)
//            def ascending    = directions(n)
//            def descending   = ! ascending
//            def startAscent  = directions(n) = up
//            def startDescent = directions(n) = down
//            def atTop        = hb.y >= maxHeight
//            def atBottom     = hb.y <= minHeight
//            if(ascending && atTop) startDescent else if(descending && atBottom) startAscent
//            if(ascending) hb changeTo m else hb.blockBelow changeTo AIR
//          }
//      }
//    ),