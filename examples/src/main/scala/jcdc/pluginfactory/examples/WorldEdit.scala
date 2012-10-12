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

  val house = """
    (
      (def roof (bigx bigz smallx smallz y m)
        (if (or (eq bigx smallx) (eq bigz smallz)) unit
          (begin
            (corners (loc bigx y bigz) (loc smallx y smallz))
            (setall m)
            (roof (- bigx 1) (- bigz 1) (+ smallx 1) (+ smallz 1) (+ 1 y) m)
          )
        )
      )
      (def house (w h d)
        (begin
          (corners (loc w (+ Y h) d) (loc (- 0 w) Y (- 0 d)))
          (setall (material "air"))
          ; build walls and floor
          (floor (material "stone"))
          (walls (material "brick"))
          ; build roof
          (roof (+ 1 w) (+ 1 d) (- -1 w) (- -1 d) (+ Y h 1) (material "wood"))
        )
      )
      (house 5 8 5)
    )
    """

  val commands = List(
    Command("house", "build a house", noArgs(WorldEditLang.run(house, _))),
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
    corners.get(p).filter(_.size == 2) match {
      case None => p bomb "Both corners must be set!"
      case Some(ls) => Cube(ls(0), ls(1))
    }
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

  object WorldEditLang extends EnrichmentClasses {

    case class Program(defs:List[Def], body:Expr)

    sealed trait Def
    case class Defn(name:Symbol, lam:Lambda) extends Def
    case class Val (name:Symbol, expr:Expr) extends Def

    sealed trait Expr
    case class Lambda(args:List[Symbol], body: Expr) extends Expr
    case class Let(x:Symbol, e:Expr, body:Expr) extends Expr
    case class IfStatement(e:Expr, truePath:Expr, falsePath:Expr) extends Expr
    case class App(f:Expr, args:List[Expr]) extends Expr
    case class Sequential(exps:List[Expr]) extends Expr
    case class SetCorners(l1:Expr,l2:Expr)extends Expr
    case class Goto(l:Expr)extends Expr
    case class Pos1(l:Expr)extends Expr
    case class Pos2(l:Expr)extends Expr
    case class GetMaterial(m:Expr)extends Expr
    case class SetMaterial(m:Expr)extends Expr
    case class Change(m1:Expr, m2:Expr)extends Expr
    case class SetWalls(m:Expr)extends Expr
    case class SetFloor(m:Expr)extends Expr
    case class Loc(x:Expr, y:Expr, z:Expr) extends Expr
    case object Origin extends Expr
    case class Bool(b:Boolean) extends Expr
    case class Num(i:Int) extends Expr
    case class StringExpr(s:String) extends Expr
    case class Eq(e1:Expr, e2:Expr) extends Expr
    case class Variable(s:Symbol) extends Expr
    case class Add(args:List[Expr]) extends Expr
    case class Subtract(a:Expr, b:Expr) extends Expr
    case object UnitExpr extends Expr

    sealed trait Value
    case class   FunValue(l:Lambda)        extends Value
    case class   MaterialValue(m:Material) extends Value
    case class   LocationValue(l:Location) extends Value
    case class   BoolValue(b:Boolean)      extends Value
    case class   NumValue(n:Int)           extends Value
    case class   StringValue(s:String)     extends Value
    case class   DynamicValue(n: () => Value)    extends Value
    case object  UnitValue extends Value

    trait Effect { def run(p:Player): Unit }
    case class SetCornersEffect(l1:Location,l2:Location) extends Effect {
      override def toString = s"SetCornersEffect(l1: ${l1.xyz}, l2: ${l2.xyz})"
      def run(p:Player) = { setFirstPos (p, l1); setSecondPos (p, l2) }
    }
    case class GotoEffect(loc:Location) extends Effect {
      override def toString = s"GotoEffect(loc: ${loc.xyz})"
      def run(p:Player) = { p ! s"teleported to: ${loc.xyz}"; p.teleport(loc) }
    }
    case class SetFirstPosEffect(loc:Location) extends Effect {
      override def toString = s"SetFirstPosEffect(loc: ${loc.xyz})"
      def run(p:Player) = setFirstPos(p,loc)
    }
    case class SetSecondPosEffect(loc:Location) extends Effect {
      override def toString = s"SetSecondPosEffect(loc: ${loc.xyz})"
      def run(p:Player) = setSecondPos(p, loc)
    }
    case class SetMaterialEffect(m:Material) extends Effect {
      def run(p:Player) = { p ! s"setting all to: $m"; for(b <- cube(p)) b changeTo m }
    }
    case class ChangeEffect(oldM:Material,newM:Material) extends Effect {
      def run(p:Player) = {
        p ! s"changing material from $oldM to $newM"
        for(b <- cube(p); if(b is oldM)) b changeTo newM
      }
    }
    case class SetWallsEffect(m:Material) extends Effect {
      def run(p:Player) = {
        p ! s"setting walls to: $m"
        cube(p).walls.foreach(_ changeTo m)
        p ! s"set walls to: $m"
      }
    }
    case class SetFloorEffect(m:Material) extends Effect {
      def run(p:Player) = { p ! s"setting walls to: $m"; cube(p).floor.foreach(_ changeTo m) }
    }

    def read(code:String): Any = io.Reader read code

    def parse(code:String): Program = parseProgram(read(code))

    def parseProgram(a:Any): Program = {
      //println(a)
      a match {
        case Nil => sys error s"bad program: $a"
        case List(x) => Program(Nil,parseExpr(x))
        case l@(x :: xs) => Program(l.init map parseDef, parseExpr(l.last))
        case _ => sys error s"bad program: $a"
      }
    }

    def parseDef(a:Any): Def = {
      def parseName(name:Any): Symbol = name match {
        case s:Symbol => s // TODO: check s against builtin things like X,Y,Z,etc
        case _ => sys error s"bad def name: $a"
      }
      a match {
        case List('def, name, args, body) => Defn(parseName(name), parseLambda(args, body))
        case List('val, name, body) => Val(parseName(name), parseExpr(body))
      }
    }

    def parseLambda(args:Any, body:Any): Lambda = {
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
      Lambda(parseLamArgList(args), parseExpr(body))
    }

    def parseExpr(a:Any): Expr = {
      a match {
        case List('lam, args, body) => parseLambda(args, body)
        case List('let, List(arg, expr), body) => arg match {
          case s:Symbol => Let(s, parseExpr(expr), parseExpr(body))
          case _ => sys error s"bad let argument: $a"
        }
        case List('if,pred,tru,fals) => IfStatement(parseExpr(pred),parseExpr(tru),parseExpr(fals))
        case 'begin :: body => Sequential(body map parseExpr)
        // location based prims
        case List('goto, loc)       => Goto(parseExpr(loc))
        case List('pos1, loc)       => Pos1(parseExpr(loc))
        case List('pos2, loc)       => Pos2(parseExpr(loc))
        case List('corners, l1, l2) => SetCorners(parseExpr(l1), parseExpr(l2))
        case 'origin => Origin
        case List('loc, x, y, z)    => Loc(parseExpr(x),parseExpr(y),parseExpr(z))
        // material based prims
        case List('material, m)     => GetMaterial(parseExpr(m))
        case List('setall, m)       => SetMaterial(parseExpr(m))
        case List('change, m1, m2)  => Change(parseExpr(m1), parseExpr(m2))
        case List('walls, m)        => SetWalls(parseExpr(m))
        case List('floor, m)        => SetFloor(parseExpr(m))
        // other prims
        case i: Int                 => Num(i)
        case List('eq, e1, e2)      => Eq(parseExpr(e1),parseExpr(e2))
        case s:Symbol               => Variable(s)
        case s:String               => StringExpr(s)
        // math operations
        case '+ :: e :: es          => Add((e::es) map parseExpr)
        case '- :: a :: b :: Nil    => Subtract(parseExpr(a), parseExpr(b))
        // finally, function application
        case f :: args              => App(parseExpr(f), args map parseExpr)
        case _                      => sys error s"bad expression: $a"
      }
    }

    def run(code:String, p:Player) = try {
      val ast = parse(code)
      println(ast)
      runProgram(ast, p)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        throw e
    }
    def runProgram(prog:Program, p:Player) = new WorldEditInterp(p).evalProg(prog)

    case class WorldEditInterp(p:Player) {
      type Env = Map[Symbol,Value]

      val defaultEnv: Env = Map(
        'MAXY  -> NumValue(255),
        'MINY  -> NumValue(0),
        'X     -> DynamicValue(() => NumValue(p.x)),
        'X     -> DynamicValue(() => NumValue(p.x)),
        'Y     -> DynamicValue(() => NumValue(p.blockOn.y)),
        'Z     -> DynamicValue(() => NumValue(p.z)),
        'XYZ   -> DynamicValue(() => LocationValue(p.loc)),
        'true  -> BoolValue(true),
        'false -> BoolValue(false),
        'unit  -> UnitValue
      )

      val boolLib = List(
        "(def and (a b) (if a b false))",
        "(def or  (a b) (if a true b))",
        "(def zero? (x) (if (eq x 0) true false))",
        "(def not (x) (if x 1 0))"
      ).map(s => parseDef(read(s)))

      def debug[T](t: => T): T = { println(t); t }

      // evaluates the defs in order (no forward references allowed)
      // then evaluates the body with the resulting environment
      def evalProg(prog:Program): Value =
        eval(prog.body, (boolLib ::: prog.defs).foldLeft(defaultEnv)(evalDef))

      // extends the env, and collects side effects for vals
      def evalDef(env: Env, d:Def): Env = env + (d match {
        case Defn(name, lam)  => name -> FunValue(lam)
        case Val (name, expr) => name -> eval(expr, env)
      })

      def reduce(v:Value) = v match {
        case DynamicValue(f) => f()
        case _ => v
      }

      def eval(e:Expr, env:Map[Symbol,Value]): Value = {
        //println(e)
        e match {
          case l@Lambda(_, _) => FunValue(l)
          case Let(x:Symbol, e:Expr, body:Expr) =>
            eval(body, env + (x -> eval(e,env)))
          case IfStatement(e:Expr, truePath:Expr, falsePath:Expr) =>
            reduce(eval(e, env)) match {
              case BoolValue(true)  => eval(truePath,  env)
              case BoolValue(false) => eval(falsePath, env)
              case ev => sys error s"bad if predicate: $ev"
            }
          case Variable(s) => env.get(s).getOrElse(sys error s"not found: ${s.toString.drop(1)}")
          case App(f:Expr, args:List[Expr]) =>
            reduce(eval(f, env)) match {
              // todo: make sure formals.size == args.size...
              // or partially apply?
              case FunValue(Lambda(formals, body)) =>
                eval(body, env ++ formals.zip(args map (eval(_, env))))
              case blah => sys error s"app expected a function, but got: $blah"
            }
          case Add(exps) =>
            NumValue(exps.map(eval(_, env)).foldLeft(0){(acc,v) => reduce(v) match {
              case NumValue(i) => acc + i
              case blah => sys error s"add expected a number, but got: $blah"
            }})
          case Subtract(a, b) =>
            (reduce(eval(a, env)), reduce(eval(b, env))) match {
              case (NumValue(av), NumValue(bv)) => NumValue(av - bv)
              case (av,bv) => sys error s"subtract expected two numbers, but got: $av, $bv"
            }
          case Sequential(exps:List[Expr]) => exps.map(eval(_, env)).last
          case SetCorners(e1:Expr,e2:Expr) =>
            sideEffect(SetCornersEffect(evalToLoc(e1,env),evalToLoc(e2,env)))
          case Goto(l:Expr)       => sideEffect(GotoEffect(evalToLoc(l,env)))
          case Pos1(l:Expr)       => sideEffect(SetFirstPosEffect(evalToLoc(l,env)))
          case Pos2(l:Expr)       => sideEffect(SetSecondPosEffect(evalToLoc(l,env)))
          case GetMaterial(m)     => reduce(eval(m,env)) match {
            case StringValue(s) => MaterialValue(BasicMinecraftParsers.material(s).get)
            case ev             => sys error s"not a location: $ev"
          }
          case SetMaterial(m)     => sideEffect(SetMaterialEffect(evalToMaterial(m,env)))
          case Change(oldM, newM) => sideEffect(ChangeEffect(evalToMaterial(oldM,env), evalToMaterial(newM,env)))
          case SetWalls(m)        => sideEffect(SetWallsEffect(evalToMaterial(m,env)))
          case SetFloor(m)        => sideEffect(SetFloorEffect(evalToMaterial(m,env)))
          case Loc(x:Expr, y:Expr, z:Expr) =>
            val (xe,ye,ze) = (reduce(eval(x,env)),reduce(eval(y,env)),reduce(eval(z,env)))
            (xe,ye,ze) match {
              case (NumValue(xv), NumValue(yv), NumValue(zv)) =>
                LocationValue(new Location(p.world,xv,yv,zv))
              case _ => sys error s"bad location data: ${(xe,ye,ze)}"
            }
          case Origin  => LocationValue(p.world.getHighestBlockAt(0,0))
          case Bool(b) => BoolValue(b)
          case Num(i)  => NumValue(i)
          case StringExpr(i)  => StringValue(i)
          case Eq(a,b) =>
            (reduce(eval(a, env)), reduce(eval(b, env))) match {
              case (NumValue(av),      NumValue(bv))      => BoolValue(av == bv)
              case (BoolValue(av),     BoolValue(bv))     => BoolValue(av == bv)
              case (StringValue(av),   StringValue(bv))   => BoolValue(av == bv)
              case (MaterialValue(av), MaterialValue(bv)) => BoolValue(av == bv)
              case (LocationValue(av), LocationValue(bv)) => BoolValue(av == bv)
              case _                              => BoolValue(false)
            }
          case UnitExpr => UnitValue
        }
      }
      def evalToLoc(e:Expr, env:Env): Location =
        reduce(eval(e,env)) match {
          case LocationValue(l) => l
          case ev => sys error s"not a location: $e"
        }
      def evalToMaterial(e:Expr, env:Env): Material =
        reduce(eval(e,env)) match {
          case MaterialValue(l) => l
          case ev => sys error s"not a material: $e"
        }
      def sideEffect(e:Effect): Value = { e.run(p); UnitValue }

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