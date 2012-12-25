package jcdc.pluginfactory.examples

import scala.collection.JavaConversions._
import jcdc.pluginfactory._
import org.bukkit.{Location, Material}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import Material._

class WorldEdit extends ListenersPlugin with CommandsPlugin {

  val corners = collection.mutable.Map[Player, List[Location]]().withDefaultValue(Nil)

  val listeners = List(
    OnLeftClickBlock ((p, e) => if(p isHoldingA WOOD_AXE) { setFirstPos (p, e.loc); e.cancel }),
    OnRightClickBlock((p, e) => if(p isHoldingA WOOD_AXE) { setSecondPos(p, e.loc) })
  )

  val commands = List(
    Command("goto", "Teleport!", args(location){ case (you, loc) => you teleport loc(you.world) }),
    Command("wand", "Get a WorldEdit wand.", noArgs(_.loc.dropItem(WOOD_AXE))),
    Command("pos1", "Set the first position",  args(location.?){ case (p, loc) =>
      setFirstPos (p, loc.fold(p.loc)(_(p.world)))
    }),
    Command("pos2", "Set the second position", args(location.?){ case (p, loc) =>
      setSecondPos(p, loc.fold(p.loc)(_(p.world)))
    }),
    Command("cube-to",  "Set both positions",  args(location ~ location.?){
      case (p, loc1 ~ loc2) =>
        setFirstPos (p, loc1(p.world))
        setSecondPos(p, loc2.fold(p.loc)(_(p.world)))
    }),
    Command("between",  "Set both positions",  args(location ~ "-" ~ location){
      case (p, loc1 ~ _ ~ loc2) =>
        setFirstPos (p, loc1(p.world))
        setSecondPos(p, loc2(p.world))
        p.teleport(loc1(p.world))
    }),
    Command("erase", "Set all the selected blocks to air.", noArgs(cube(_).eraseAll)),
    Command(
      name = "set", desc = "Set all the selected blocks to the given material type.",
      body = args(material){ case (p, m) => for(b <- cube(p)) b changeTo m }
    ),
    Command(
      name = "change",
      desc = "Change all the selected blocks of the first material type to the second material type.",
      body = args(material ~ material){
        case (p, oldM ~ newM) => for(b <- cube(p); if(b is oldM)) b changeTo newM
      }
    ),
    Command(
      name = "find",
      desc = "Checks if your cube contains any of the given material, and tells where.",
      body = args(material){ case (p, m) =>
        cube(p).find(_ is m).fold(
          s"No $m found in your cube!")(b => s"$m found at ${b.loc.xyz}")
      }
    ),
    Command(
      name = "fib-tower",
      desc = "create a tower from the fib numbers",
      body = args(int ~ material){ case (p, i ~ m) =>
        lazy val fibs: Stream[Int] = 0 #:: 1 #:: fibs.zip(fibs.tail).map{case (i,j) => i+j}
        for {
          (startBlock,n) <- p.world.fromX(p.loc).zip(fibs take i)
          towerBlock     <- startBlock.andBlocksAbove take n
        } towerBlock changeTo m
      }
    ),
    Command(
      name = "walls",
      desc = "Create walls with the given material type.",
      body = args(material) { case (p, m) => cube(p).walls.foreach(_ changeTo m) }
    ),
    Command(
      name = "empty-tower",
      desc = "Create walls and floor with the given material type, and set everything inside to air.",
      body = args(material) { case (p, m) =>
        val c = cube(p)
        for(b <- cube(p)) if (c.onWall(b) or c.onFloor(b)) b changeTo m else b.erase
      }
    ),
    Command(
      name = "dig",
      desc = "Dig",
      body = args(oddNum ~ int) { case (p, radius ~ depth) =>
        val b = radius / 2
        val (x, y, z) = p.loc.xyzd
        Cube(p.world(x + b, y, z + b).loc, p.world(x - b, y - depth, z - b).loc).eraseAll
      }
    )
  )

  def cube(p:Player): Cube = corners.get(p).filter(_.size == 2).
    fold(p bomb "Both corners must be set!")(ls => Cube(ls(0), ls(1)))

  def setFirstPos(p:Player,loc: Location): Unit = {
    corners.update(p, List(loc))
    p ! s"first corner set to: ${loc.xyz}"
  }

  def setSecondPos(p:Player,loc2: Location): Unit = corners(p) match {
    case loc1 :: _ =>
      corners.update(p, List(loc1, loc2))
      p ! s"second corner set to: ${loc2.xyz}"
    case Nil =>
      p ! "set corner one first! (with a left click)"
  }
}

//import javax.persistence._
//import scala.beans.BeanProperty
//@Entity
//class Script {
//  @Id @GeneratedValue @BeanProperty var id = 0
//  @BeanProperty var player = ""
//  @BeanProperty var title: String = ""
//  @BeanProperty var commandsString:String = ""
//  def commands = commandsString.split(";").map(_.trim).filter(_.nonEmpty)
//  override def toString = s"$player.$title \n[${commands.mkString("\n")}]"
//}
//with SingleClassDBPlugin[Script]
//val dbClass = classOf[Script]
//    Command("code-book-example", "get a 'code book' example", args(anyString.?){ case (p, title) =>
//      p.inventory addItem Book(author = p, title, pages =
//        """
//         ((change grass diamond_block)
//          (change dirt  gold_block)
//          (change stone iron_block))
//        """.trim
//      )
//    }),
//    Command("run-book", "run the code in a book", noArgs(p =>
//      ScriptRunner.runBook(p, Book.fromHand(p)))
//    ),
//    Command("make-script", "build a script", args(anyString ~ slurp){ case (p, title ~ code) =>
//      val script = createScript(p, title, code)
//      p ! s"$script"
//      db.insert(script)
//    }),
//    Command("show-script", "show the code in a script", args(anyString){ case (p, title) =>
//      db.firstWhere(Map("player" -> p.name, "title" -> title)).
//        fold(p ! s"unknown script: $title")(s => p ! s"$s")
//    }),
//    Command("show-scripts", "show the code in a script", noArgs(p =>
//      db.findAll.foreach(s => p ! s"$s")
//    )),
//    Command("run-script", "run the code in a script", args(anyString){ case (p, title) =>
//      db.firstWhere(Map("player" -> p.name, "title" -> title)).
//        fold(p ! s"unknown script: $title")(s => ScriptRunner.runScript(p, s))
//    }),
//  object ScriptRunner{
//    def run(p:Player, lines:Seq[String]): Unit = for {
//      commandAndArgs <- lines.map(_.trim).filter(_.nonEmpty)
//      x      = commandAndArgs.split(" ").map(_.trim).filter(_.nonEmpty)
//      cmd    = x.head
//      args   = x.tail
//    } runCommand(p, cmd, args)
//    def runScript(p:Player, script:Script): Unit = run(p, script.commands)
//    def runBook(p:Player, b:Book): Unit =
//      run(p, b.pages.flatMap(_.split("\n").map(_.trim).filter(_.nonEmpty)))
//  }
//
//  def createScript(p: Player, title:String, commands:String): Script = {
//    val s = new Script(); s.player = p.name; s.title = title; s.commandsString = commands; s
//  }

/**
 * broken recur code. move it somewhere else and fix it
  val recurs  = collection.mutable.Map[Player, Int]().withDefaultValue(0)
  OnBlockBreak((cb,p,_)    => if(p isHoldingA WOOD_PICKAXE)
    for(b <- Cube(cb.loc,cb.loc).expand(recurs(p)); if (b is cb.getType)) b.erase
  )
  Command(name="recur", desc="set your recur depth", body=args(int) {case (p,i) => recurs(p)=i})
 */