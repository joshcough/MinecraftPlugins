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
      (defrec roof (bigx bigz smallx smallz y m)
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
    case class Lambda(args:List[Symbol], body: Expr, recursive:Option[Symbol]) extends Expr
    case class Let(x:Symbol, e:Expr, body:Expr) extends Expr
    case class IfStatement(e:Expr, truePath:Expr, falsePath:Expr) extends Expr
    case class App(f:Expr, args:List[Expr]) extends Expr
    case class Sequential(exps:List[Expr]) extends Expr
    case class Bool(b:Boolean) extends Expr
    case class Num(i:Int) extends Expr
    case class StringExpr(s:String) extends Expr
    case class Variable(s:Symbol) extends Expr
    case object UnitExpr extends Expr

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
        case List('def,    name, args, body) =>
          Defn(parseName(name), parseLambda(args, body, recursive=None))
        case List('defrec, name, args, body) =>
          val n = parseName(name)
          Defn(n, parseLambda(args, body, recursive=Some(n)))
        case List('val,    name, body) => Val(parseName(name), parseExpr(body))
      }
    }

    def parseLambda(args:Any, body:Any, recursive:Option[Symbol]): Lambda = {
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
      Lambda(parseLamArgList(args), parseExpr(body), recursive)
    }

    def parseExpr(a:Any): Expr = {
      a match {
        case List('lam, args, body) => parseLambda(args, body, None)
        case List('let, List(arg, expr), body) => arg match {
          case s:Symbol => Let(s, parseExpr(expr), parseExpr(body))
          case _ => sys error s"bad let argument: $a"
        }
        case List('if,pred,tru,fals) => IfStatement(parseExpr(pred),parseExpr(tru),parseExpr(fals))
        case 'begin :: body => Sequential(body map parseExpr)
        // other prims
        case i: Int                 => Num(i)
        case s:Symbol               => Variable(s)
        case s:String               => StringExpr(s)
        // finally, function application
        case f :: args              => App(parseExpr(f), args map parseExpr)
        case _                      => sys error s"bad expression: $a"
      }
    }

    def run(code:String, p:Player) = {
      val ast = parse(code)
      println(ast)
      runProgram(ast, p)
    }
    def runProgram(prog:Program, p:Player) = new WorldEditInterp(p).evalProg(prog)

    case class WorldEditInterp(p:Player) {
      type Env = Map[Symbol,Value]

      sealed trait Value
      case class   Closure(l:Lambda, env:Env)  extends Value
      case class   MaterialValue(m:Material)    extends Value
      case class   LocationValue(l:Location)    extends Value
      case class   BoolValue(b:Boolean)         extends Value
      case class   NumValue(n:Int)              extends Value
      case class   StringValue(s:String)        extends Value
      case class   DynamicValue(n: () => Value) extends Value
      case class   BuiltinFunction(name: Symbol, eval: (List[Expr], Env) => Value) extends Value

      case object  UnitValue extends Value

      def builtIn(name:Symbol, eval: (List[Expr], Env) => Value) =
        (name -> BuiltinFunction(name, eval))

      def builtInUnit(name:Symbol, eval: (List[Expr], Env) => Unit) =
        (name -> BuiltinFunction(name, (es, env) => { eval(es,env); UnitValue }))

      // TODO on all these builtins, check the number of arguments.
      val add = builtIn('+, ((exps, env) =>
        NumValue(exps.map(eval(_, env)).foldLeft(0){(acc,v) => reduce(v) match {
          case NumValue(i) => acc + i
          case blah => sys error s"+ (add) expected a number, but got: $blah"
        }})
      ))

      val sub = builtIn('-, ((exps, env) =>
        (reduce(eval(exps(0), env)), reduce(eval(exps(1), env))) match {
          case (NumValue(av), NumValue(bv)) => NumValue(av - bv)
          case (av,bv) => sys error s"- (subtract) expected two numbers, but got: $av, $bv"
        }
      ))

      val mult = builtIn('*, ((exps, env) =>
        (reduce(eval(exps(0), env)), reduce(eval(exps(1), env))) match {
          case (NumValue(av), NumValue(bv)) => NumValue(av * bv)
          case (av,bv) => sys error s"* (mult) expected two numbers, but got: $av, $bv"
        }
      ))

      val print = builtInUnit('print, (exps, env) =>
        exps.map(eval(_, env)).foreach(v => println(reduce(v)))
      )

      val setCorners = builtInUnit('corners, ((exps, env) => {
        setFirstPos (p,  evalToLoc(exps(0),env))
        setSecondPos (p, evalToLoc(exps(1),env))
      }))

      val goto = builtInUnit('goto, ((exps, env) => {
        val loc = evalToLoc(exps(0),env)
        p ! s"teleported to: ${loc.xyz}"; p.teleport(loc)
        UnitValue
      }))

      val pos1 = builtInUnit('pos1, ((exps, env) => {
        setFirstPos(p,evalToLoc(exps(0),env)); UnitValue
      }))

      val pos2 = builtInUnit('pos2, ((exps, env) => {
        setSecondPos(p, evalToLoc(exps(0),env)); UnitValue
      }))

      val getMaterial = builtIn('material, ((exps, env) => {
        reduce(eval(exps(0),env)) match {
          case StringValue(s) => MaterialValue(BasicMinecraftParsers.material(s).get)
          case ev             => sys error s"not a material: $ev"
        }
      }))

      val setAll = builtInUnit('setall, (exps, env) => {
        val m = evalToMaterial(exps(0), env)
        p ! s"setting all to: $m"
        for(b <- cube(p)) b changeTo m
      })

      val change = builtInUnit('change, ((exps, env) => {
        val oldM = evalToMaterial(exps(0),env)
        val newM = evalToMaterial(exps(1),env)
        p ! s"changing material from $oldM to $newM"
        for(b <- cube(p); if(b is oldM)) b changeTo newM
      }))

      val walls = builtInUnit('walls, ((exps, env) => {
        val m = evalToMaterial(exps(0),env)
        p ! s"setting walls to: $m"
        cube(p).walls.foreach(_ changeTo m)
        p ! s"set walls to: $m"
      }))

      val floor = builtInUnit('floor, ((exps, env) => {
        val m = evalToMaterial(exps(0),env)
        p ! s"setting walls to: $m"; cube(p).floor.foreach(_ changeTo m)
      }))

      val loc = builtIn('loc, (exps, env) => {
        val (xe,ye,ze) = (reduce(eval(exps(0),env)),reduce(eval(exps(1),env)),reduce(eval(exps(2),env)))
        (xe,ye,ze) match {
          case (NumValue(xv), NumValue(yv), NumValue(zv)) =>
            LocationValue(new Location(p.world,xv,yv,zv))
          case _ => sys error s"bad location data: ${(xe,ye,ze)}"
        }
      })

      val eq = builtIn('eq, (exps, env) => {
        (reduce(eval(exps(0), env)), reduce(eval(exps(1), env))) match {
          case (NumValue(av),      NumValue(bv))      => BoolValue(av == bv)
          case (BoolValue(av),     BoolValue(bv))     => BoolValue(av == bv)
          case (StringValue(av),   StringValue(bv))   => BoolValue(av == bv)
          case (MaterialValue(av), MaterialValue(bv)) => BoolValue(av == bv)
          case (LocationValue(av), LocationValue(bv)) => BoolValue(av == bv)
          case _                                      => BoolValue(false)
        }
      })

      val defaultEnv: Env = Map(
        'true   -> BoolValue(true),
        'false  -> BoolValue(false),
        'unit   -> UnitValue,
        // equality
        eq,
        // match
        add, sub, mult,
        // print
        print,
        // location functions
        loc, goto,
        'MAXY   -> NumValue(255),
        'MINY   -> NumValue(0),
        'X      -> DynamicValue(() => NumValue(p.x)),
        'X      -> DynamicValue(() => NumValue(p.x)),
        'Y      -> DynamicValue(() => NumValue(p.blockOn.y)),
        'Z      -> DynamicValue(() => NumValue(p.z)),
        'XYZ    -> DynamicValue(() => LocationValue(p.loc)),
        'origin -> DynamicValue(() => LocationValue(p.world.getHighestBlockAt(0,0))),
        // material functions
        getMaterial,
        // world edit functions
        setCorners, pos1, pos2, setAll, change, walls, floor
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
      def evalProg(prog:Program): Value = {
        val allFunctionsEnv = (boolLib ::: prog.defs).foldLeft(defaultEnv)(evalDef)
        eval(prog.body, allFunctionsEnv)
      }

      // extends the env, and collects side effects for vals
      def evalDef(env: Env, d:Def): Env = {
        env + (d match {
          case Val (name, expr) => name -> eval(expr, env)
          case Defn(name, lam)  => name -> Closure(lam, env)
        })
      }

      def reduce(v:Value) = v match {
        case DynamicValue(f) => f()
        case _ => v
      }

      def eval(e:Expr, env:Env): Value = try {
        //println(e)
        e match {
          case l@Lambda(_, _, _) => Closure(l, env)
          case Let(x:Symbol, e:Expr, body:Expr) =>
            eval(body, env + (x -> eval(e,env)))
          case IfStatement(e:Expr, truePath:Expr, falsePath:Expr) =>
            reduce(eval(e, env)) match {
              case BoolValue(true)  => eval(truePath,  env)
              case BoolValue(false) => eval(falsePath, env)
              case ev => sys error s"bad if predicate: $ev"
            }
          case Variable(s) => env.get(s).getOrElse(sys error s"not found: $s in: ${env.keys}")
          case App(f:Expr, args:List[Expr]) =>
            reduce(eval(f, env)) match {
              // todo: make sure formals.size == args.size...
              // or partially apply?
              case c@Closure(Lambda(formals, body, rec), closedOverEnv) =>
                val envWithoutRecursion = closedOverEnv ++ formals.zip(args map (eval(_, env)))
                val finalEnv = rec.fold(envWithoutRecursion)(name => envWithoutRecursion + (name -> c))
                //println("calling eval with $body and $finalEnv")
                eval(body, finalEnv)
              case BuiltinFunction(name, e) =>
                //println(s"found builtin: $name. calling it with $args $env")
                e(args, env)
              case blah => sys error s"app expected a function, but got: $blah"
            }
          case Sequential(exps:List[Expr]) => exps.map(eval(_, env)).last
          case Bool(b) => BoolValue(b)
          case Num(i)  => NumValue(i)
          case StringExpr(i)  => StringValue(i)
          case UnitExpr => UnitValue
        }
      } catch{
        case ex: Exception =>
          //println(s"error evaluating: $e ${env.mkString("\n\t")}")
          throw ex
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