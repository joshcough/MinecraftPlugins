package jcdc.pluginfactory.examples

import scala.collection.JavaConversions._
import jcdc.pluginfactory._
import org.bukkit.{Location, Material}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import Material._
import java.io.File
import scala.io.Source

class WorldEdit extends ListenersPlugin
  with CommandsPlugin with SingleClassDBPlugin[Script] {

  val dbClass = classOf[Script]

  val corners = collection.mutable.Map[Player, List[Location]]().withDefaultValue(Nil)

  val listeners = List(
    OnLeftClickBlock((p, e)  => if(p isHoldingA WOOD_AXE) { setFirstPos (p, e.loc); e.cancel }),
    OnRightClickBlock((p, e) => if(p isHoldingA WOOD_AXE) { setSecondPos(p, e.loc) })
  )

  val commands = List(
//    Command("test-script", "run the test script", noArgs(WorldEditInterp.apply(_, testScript))),
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
    Command("goto", "Teleport!", args(location){ case (you, loc) => you teleport loc(you.world) }),
    Command("wand", "Get a WorldEdit wand.", noArgs(_.loc.dropItem(WOOD_AXE))),
    Command("pos1", "Set the first position",  args(location.?){ case (p, loc) =>
      setFirstPos(p, loc.fold(p.loc)(_(p.world)))
    }),
    Command("pos2", "Set the second position",  args(location.?){ case (p, loc) =>
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
        Cube(p.world(x + b, y, z + b), p.world(x - b, y - depth, z - b)).eraseAll
      }
    )
  )

  def cube(p:Player): Cube = {
    corners.get(p).filter(_.size == 2).
      fold({p ! "Both corners must be set!"; Cube(p.world(0,0,0),p.world(0,0,0))})(ls =>
        Cube(ls(0), ls(1))
    )
  }

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
//
//  val testScript =
//    """
//     ((goto origin)
//      (corners ((+ X 20) (+ Y 50) (+ Z 20)) ((- X 20) Y (- Z 20)))
//      (floor stone)
//      (walls brick)
//     )
//    """.stripMargin.trim


  object WorldEditLang {

    case class Program(defs:List[Def], body:Expr)

    sealed trait Def
    case class Defn(name:Symbol, lam:Lambda) extends Def
    case class Val (name:Symbol, expr:Expr) extends Def

    sealed trait Expr
    case class Lambda(args:List[Symbol], body: Seqential) extends Expr
    case class Let(x:Symbol, e:Expr, body:Expr) extends Expr
    case class IfStatement(e:Expr, truePath:Expr, falsePath:Expr) extends Expr
    case class App(f:Expr, args:List[Expr]) extends Expr
    case class Seqential(exps:List[Expr]) extends Expr
    case class Corners(l1:Expr,l2:Expr)extends Expr
    case class Goto(l:Expr)extends Expr
    case class Pos1(l:Expr)extends Expr
    case class Pos2(l:Expr)extends Expr
    case class SetMaterial(m:Material)extends Expr
    case class Change(m1:Material, m2:Material)extends Expr
    case class SetWalls(m:Material)extends Expr
    case class SetFloor(m:Material)extends Expr
    case class Loc(x:Expr, y:Expr, z:Expr) extends Expr
    case object Origin extends Expr
    case object XYZ extends Expr
    case object X extends Expr
    case object Y extends Expr
    case object Z extends Expr
    case object MaxY extends Expr
    case object MinY extends Expr
    case class Num(i:Int) extends Expr
    case class Bool(b:Boolean) extends Expr
    case class Variable(s:Symbol) extends Expr
    case class Add(args:List[Expr]) extends Expr
    case class Subtract(a:Expr, b:Expr) extends Expr

    sealed trait Value
    case class MaterialValue(m:Material) extends Value
    case class LocationValue(l:Location) extends Value
    case class FunValue(l:Lambda)        extends Value
    case class NumValue(n:Int)           extends Value
    case class BoolValue(b:Boolean)      extends Value
    case class EffectValue(u: () => Unit)  extends Value{
      def andThen(u2:EffectValue) = EffectValue(() => {u; u2;})
    }

    def parse(code:String): Program = parseProgram(io.Reader read code)

    def parseProgram(a:Any): Program = a match {
      case Nil => sys error s"bad program: $a"
      case List(x) => Program(Nil,parseExpr(x))
      case l@(x :: xs) => Program(l.init map parseDef, parseExpr(l.last))
      case _ => sys error s"bad program: $a"
    }

    def parseDef(a:Any): Def = {
      def parseName(name:Any): Symbol = name match {
        case s:Symbol => s // TODO: check s against builtin things like X,Y,Z,etc
        case _ => sys error s"bad def name: $a"
      }
      a match {
        case 'defn :: name :: args :: body => Defn(parseName(name), parseLambda(args :: body))
        case 'val  :: name :: body => Val(parseName(name), parseExpr(body))
      }
    }

    def parseLambda(a:List[Any]): Lambda = {
      def parseLamArgList(a:Any): List[Symbol] = {
        def parseLamArg(a:Any) = a match {
          case s:Symbol => s // TODO: check s against builtin things like X,Y,Z,etc
          case _ => sys error s"bad lambda arg: $a"
        }
        a match {
          case x :: xs => (x :: xs).map(parseLamArg)
          case _ => sys error s"bad lambda arg list: $a"
        }
      }
      a match {
        case args :: body => Lambda(parseLamArgList(args), Seqential(body map parseExpr))
        case _ => sys error s"bad lambda: $a"
      }
    }

    def parseExpr(a:Any): Expr = {
      def parseMaterial(a:Any) = BasicMinecraftParsers.material(a.toString.drop(1)).get
      a match {
        case 'lam :: args :: body => parseLambda(args :: body)
        case 'let :: args :: body => sys error "todo: parse let"
        case List('if,pred,tru,fals) => IfStatement(parseExpr(pred),parseExpr(tru),parseExpr(fals))
        case 'seq :: body => Seqential(body map parseExpr)
        // location based prims
        case List('goto, loc)       => Goto(parseExpr(loc))
        case List('pos1, loc)       => Pos1(parseExpr(loc))
        case List('pos2, loc)       => Pos2(parseExpr(loc))
        case List('corners, l1, l2) => Corners(parseExpr(l1), parseExpr(l2))
        case 'origin => Origin
        case 'XYZ    => XYZ
        case 'X      => X
        case 'Y      => Y
        case 'Z      => Z
        case 'loc :: x :: y :: z    => Loc(parseExpr(x),parseExpr(y),parseExpr(z))
        // material based prims
        case List('set, m)          => SetMaterial(parseMaterial(m))
        case List('change, m1, m2)  => Change(parseMaterial(m1), parseMaterial(m2))
        case List('walls, m)        => SetWalls(parseMaterial(m))
        case List('floor, m)        => SetFloor(parseMaterial(m))
        // other prims
        case i: Int => Num(i)
        case 'true  => Bool(true)
        case 'false => Bool(false)
        case s:Symbol => Variable(s)
        // math operations
        case '+ :: e :: es => Add((e::es) map parseExpr)
        case '- :: a :: b :: Nil => Subtract(parseExpr(a), parseExpr(b))
        // finally, function application
        case f :: args => App(parseExpr(f), args map parseExpr)
        case _      => sys error s"bad expression: $a"
      }
    }

    def run(prog:Program, p:Player) = new WorldEditInterp(p).evalProg(prog)

    case class WorldEditInterp(p:Player) extends EnrichmentClasses {
      val x: Int = p.x
      val y: Int = p.y
      val z: Int = p.z

      type Env = Map[Symbol,Value]
      val emptyEnv: Env = Map()

      def evalProg(prog:Program): Value = eval(prog.body, prog.defs.foldLeft(emptyEnv)(evalDef))
      // just extends the env
      def evalDef(env: Env, d:Def): Env = d match {
        case Defn(name:Symbol, lam:Lambda) => env + (name -> FunValue(lam))
        case Val (name:Symbol, expr:Expr)  => env + (name -> eval(expr, env))
      }
      def eval(e:Expr, env:Map[Symbol,Value]): Value = e match {
        case l@Lambda(_, _) => FunValue(l)
        case Let(x:Symbol, e:Expr, body:Expr) => eval(body, env + (x -> eval(e,env)))
        case IfStatement(e:Expr, truePath:Expr, falsePath:Expr) => eval(e,env) match {
          case BoolValue(true)  => eval(truePath,  env)
          case BoolValue(false) => eval(falsePath, env)
          case ev => sys error s"bad if predicate: $ev"
        }
        case Variable(s) => env.get(s).getOrElse(sys error s"not found: ${s.toString.drop(1)}")
        case App(f:Expr, args:List[Expr]) => eval(f,env) match {
          case FunValue(Lambda(formals, body)) =>
            eval(body, env ++ formals.zip(args.map(eval(_, env))))
          case blah => sys error s"app expected a function, but got: $blah"
        }
        case Add(exps) => NumValue(exps.map(eval(_, env)).foldLeft(0){(acc,v) => v match {
          case NumValue(i) => acc + i
          case blah => sys error s"add expected a number, but got: $blah"
        }})
        case Subtract(a, b) => (eval(a,env), eval(b,env)) match {
          case (NumValue(av), NumValue(bv)) => NumValue(av - bv)
          case (av,bv) => sys error s"subtract expected two numbers, but got: $av, $bv"
        }
        // todo: this it totally broken
        case Seqential(exps:List[Expr]) =>
          exps.map(eval(_, env)).foldLeft(EffectValue(() => {})){ (acc, v) => v match {
            case u@EffectValue(_) => acc andThen u
            case _ => acc
          }}
        case Corners(e1:Expr,e2:Expr) => EffectValue(() => {
          setFirstPos (p, evalToLoc(e1,env))
          setSecondPos(p, evalToLoc(e2,env))
        })
        case Goto(l:Expr) => EffectValue(() => p.teleport(evalToLoc(e,env)))
        case Pos1(e:Expr) => EffectValue(() => setFirstPos (p, evalToLoc(e,env)))
        case Pos2(e:Expr) => EffectValue(() => setSecondPos(p, evalToLoc(e,env)))
        case SetMaterial(m) => EffectValue(() => for(b <- cube(p)) b changeTo m)
        case Change(oldM, newM) =>
          EffectValue(() => for(b <- cube(p); if(b is oldM)) b changeTo newM)
        case SetWalls(m) => EffectValue(() => cube(p).walls.foreach(_ changeTo m))
        case SetFloor(m) => EffectValue(() => cube(p).floor.foreach(_ changeTo m))
        case Loc(x:Expr, y:Expr, z:Expr) =>
          val (xe,ye,ze) = (eval(x,env),eval(y,env),eval(z,env))
          (xe,ye,ze) match {
            case (NumValue(xv), NumValue(yv), NumValue(zv)) => LocationValue(p.world(xv,yv,zv))
            case _ => sys error s"bad location data: ${(xe,ye,ze)}"
          }
        case Origin  => LocationValue(p.world.getHighestBlockAt(0,0))
        case XYZ     => LocationValue(p.world(x,y,z))
        case X       => NumValue(x)
        case Y       => NumValue(y)
        case Z       => NumValue(z)
        case MaxY    => NumValue(255)
        case MinY    => NumValue(0)
        case Num(i)  => NumValue(i)
        case Bool(b) => BoolValue(b)
      }
      def evalToLoc(e:Expr, env:Env): Location = eval(e,env) match {
        case LocationValue(l) => l
        case ev => sys error s"not a location: $ev"
      }
      //    def apply(p:Player, nodes:List[BuiltIn]): Unit = nodes.foreach(apply(p, _))
      //    def apply(p:Player, code:String): Unit = attempt(p, { println(code); apply(p, p.parse(code)) })
      //    def apply(p:Player, commands:TraversableOnce[String]): Unit = apply(p, commands.mkString(" "))
      //    def apply(p:Player, f:File): Unit = attempt(p, apply(p, Source.fromFile(f).getLines))
    }
  }


}

import javax.persistence._
import scala.beans.BeanProperty

@Entity
class Script {
  @Id @GeneratedValue @BeanProperty var id = 0
  @BeanProperty var player = ""
  @BeanProperty var title: String = ""
  @BeanProperty var commandsString:String = ""
  def commands = commandsString.split(";").map(_.trim).filter(_.nonEmpty)
  override def toString = s"$player.$title \n[${commands.mkString("\n")}]"
}