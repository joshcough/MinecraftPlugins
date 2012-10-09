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
    case class Seqential(exps:List[Expr]) extends Expr
    case class SetCorners(l1:Expr,l2:Expr)extends Expr
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
    case object Unit extends Value

    trait Effect { self => 
      def run(p:Player): Unit
      def andThen(e2:Effect) = new Effect{ def run(p:Player){ self.run(p); e2.run(p) }}
    }

    case class SetCornersEffect(l1:Location,l2:Location) extends Effect {
      override def toString = s"SetCornersEffect(l1: ${l1.xyz}, l2: ${l2.xyz})"
      def run(p:Player) = { setFirstPos (p, l1); setSecondPos (p, l2) }
    }
    case class GotoEffect(loc:Location) extends Effect {
      override def toString = s"GotoEffect(loc: ${loc.xyz})"
      def run(p:Player) = p.teleport(loc)
    }
    case class SetFirstPosEffect(loc:Location) extends Effect {
      override def toString = s"SetFirstPosEffect(loc: ${loc.xyz})"
      def run(p:Player) = setFirstPos(p,loc)
    }
    case class SetSecondPosEffect(loc:Location) extends Effect {
      override def toString = s"SetSecondPosEffect(loc: ${loc.xyz})"
      def run(p:Player) = setSecondPos(p, loc)
    }
    case class SetMaterialEffect(m:Material) extends Effect { def run(p:Player) = for(b <- cube(p)) b changeTo m }
    case class ChangeEffect(oldM:Material,newM:Material) extends Effect {
      def run(p:Player) = for(b <- cube(p); if(b is oldM)) b changeTo newM
    }
    case class SetWallsEffect(m:Material) extends Effect { def run(p:Player) = cube(p).walls.foreach(_ changeTo m) }
    case class SetFloorEffect(m:Material) extends Effect { def run(p:Player) = cube(p).floor.foreach(_ changeTo m) }

    type Effects = List[Effect]
    type V = State[Effects, Value]

    case class State[S,A](f: S => (A, S)) {
      def apply(s:S) = f(s)
      def flatMap[B](f1: A => State[S, B]): State[S, B] = State(s => {
        val (a,s1) = f(s)
        f1(a).f(s1)
      })
      def map[B](f: A => B): State[S,B] = flatMap(a => State(s => (f(a), s)))
    }

    class StateMonad[S] extends Monad[({type f[x] = State[S,x]})#f] {
      def unit[A](a: => A): State[S,A] = State(s => (a, s))
      def bind[A,B](fa: State[S,A])(f: A => State[S,B]): State[S,B] = fa.flatMap(f)
    }
    object WEStateMonad extends StateMonad[Effects]

    trait Monad[F[_]] {
      def unit[A](a: => A): F[A]
      def bind[A,B](fa: F[A])(f: A => F[B]): F[B]
      def map[A,B](fa: F[A])(f: A => B): F[B] = bind(fa)(a => unit(f(a)))
      def join[A](f: F[F[A]]): F[A] = bind(f)(identity)
      def kleisli[A,B](f: A => B): A => F[B] = a => unit(f(a))
      def bind2[A,B](fa: F[A])(f: A => F[B]): F[B] = join(map(fa)(f))
      def ap[A,B](f: F[A => B])(fa: F[A]): F[B] = bind(f)(fab => map(fa)(fab))
      def map2[A,B,C](fa: F[A], fb: F[B])(f: (A,B) => C): F[C] = bind(fa)(a => map(fb)(b => f(a,b)))
      def map3[A,B,C,D](fa: F[A], fb: F[B], fc: F[C])(f: (A,B,C) => D): F[D] =
        bind(fa)(a => bind(fb)(b => map(fc)(c => f(a,b,c))))
      def lift2[A,B,C](f: (A,B) => C): (F[A],F[B]) => F[C] = (fa,fb) => map2(fa,fb)(f)
      def lift2Cons[A] = lift2[A, List[A], List[A]](_ :: _)
      def sequence[A](fas: List[F[A]]): F[List[A]] = fas.foldRight(unit(List[A]())){
        (fa:F[A], acc:F[List[A]]) => lift2Cons(fa, acc)
      }
      def traverse[A,B](fas: List[A])(f: A => F[B]): F[List[B]] = sequence(fas.map(f))
    }

    def parse(code:String): Program = parseProgram(io.Reader read code)

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
      //println(s"parse def: $a")
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
      def parseMaterial(a:Any) = BasicMinecraftParsers.material(a.toString.drop(1)).get
      a match {
        case List('lam, args, body) => parseLambda(args, body)
        case List('let, List(arg, expr), body) => arg match {
          case s:Symbol => Let(s, parseExpr(expr), parseExpr(body))
          case _ => sys error s"bad let argument: $a"
        }
        case List('if,pred,tru,fals) => IfStatement(parseExpr(pred),parseExpr(tru),parseExpr(fals))
        case 'seq :: body => Seqential(body map parseExpr)
        // location based prims
        case List('goto, loc)       => Goto(parseExpr(loc))
        case List('pos1, loc)       => Pos1(parseExpr(loc))
        case List('pos2, loc)       => Pos2(parseExpr(loc))
        case List('corners, l1, l2) => SetCorners(parseExpr(l1), parseExpr(l2))
        case 'origin => Origin
        case 'XYZ    => XYZ
        case 'X      => X
        case 'Y      => Y
        case 'Z      => Z
        case List('loc, x, y, z)    => Loc(parseExpr(x),parseExpr(y),parseExpr(z))
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

    case class WorldEditInterp(p:Player) {
      val x: Int = p.x
      val y: Int = p.y
      val z: Int = p.z

      type Env = Map[Symbol,Value]
      val emptyEnv: Env = Map()

      // evaluates the defs in order (no forward references allowed)
      // then evaluates the body with the resulting environment
      def evalProg(prog:Program): (Value, Effects) =
        (for{
          env <- prog.defs.foldLeft(WEStateMonad.unit(emptyEnv)){ (accS, d) =>
            for{ acc <- accS; more <- evalDef(acc, d) } yield more
          }
          res <- eval(prog.body, env)
        } yield res)(List())

      // extends the env, and collects side effects for vals
      def evalDef(env: Env, d:Def): State[Effects,Env] = d match {
        case Defn(name:Symbol, lam:Lambda) => WEStateMonad.unit(env + (name -> FunValue(lam)))
        case Val (name:Symbol, expr:Expr)  => for(ev <- eval(expr, env)) yield env + (name -> ev)
      }

      def pure(v:Value): V = WEStateMonad.unit(v)
      def addSideEffect(f: Effect): V = State(s => (Unit, s ::: List(f)))
      def sideEffect(e: Effect): V = for(_ <- addSideEffect(e)) yield Unit

      def eval(e:Expr, env:Map[Symbol,Value]): V = e match {
        case l@Lambda(_, _) => pure(FunValue(l))
        case Let(x:Symbol, e:Expr, body:Expr) =>
          for{
            ev <- eval(e,env)
            bv <- eval(body, env + (x -> ev))
          } yield bv
        case IfStatement(e:Expr, truePath:Expr, falsePath:Expr) =>
          for {
            ev <- eval(e, env)
            resv <- ev match {
              case BoolValue(true)  => eval(truePath,  env)
              case BoolValue(false) => eval(falsePath, env)
              case ev => sys error s"bad if predicate: $ev"
            }
          } yield resv
        case Variable(s) => pure(env.get(s).getOrElse(sys error s"not found: ${s.toString.drop(1)}"))
        case App(f:Expr, args:List[Expr]) =>
          for{
            fv    <- eval(f, env)
            argvs <- WEStateMonad.sequence(args map (eval(_, env)))
            res   <- fv match {
              // todo: make sure formals.size == args.size...
              case FunValue(Lambda(formals, body)) => eval(body, env ++ formals.zip(argvs))
              case blah => sys error s"app expected a function, but got: $blah"
            }
          } yield res
        case Add(exps) =>
          for(argvs <- WEStateMonad.sequence(exps map (eval(_, env)))) yield
            NumValue(argvs.foldLeft(0){(acc,v) => v match {
              case NumValue(i) => acc + i
              case blah => sys error s"add expected a number, but got: $blah"
            }})
        case Subtract(a, b) => for{ av <- eval(a, env); bv <- eval(b, env) } yield (av, bv) match {
          case (NumValue(av), NumValue(bv)) => NumValue(av - bv)
          case (av,bv) => sys error s"subtract expected two numbers, but got: $av, $bv"
        }
        case Seqential(exps:List[Expr]) =>
          for(argvs <- WEStateMonad.sequence(exps map (eval(_, env)))) yield argvs.last
        case SetCorners(e1:Expr,e2:Expr) => for {
          l1 <- evalToLoc(e1,env)
          l2 <- evalToLoc(e2,env)
          _ <- addSideEffect(SetCornersEffect(l1,l2))
        } yield Unit
        case Goto(l:Expr) => locationSideEffect(l, env, GotoEffect(_))
        case Pos1(l:Expr) => locationSideEffect(l, env, SetFirstPosEffect(_))
        case Pos2(l:Expr) => locationSideEffect(l, env, SetSecondPosEffect(_))
        case SetMaterial(m) => sideEffect(SetMaterialEffect(m))
        case Change(oldM, newM) => sideEffect(ChangeEffect(oldM, newM))
        case SetWalls(m) => sideEffect(SetWallsEffect(m))
        case SetFloor(m) => sideEffect(SetFloorEffect(m))
        case Loc(x:Expr, y:Expr, z:Expr) => for(xe <- eval(x,env); ye <- eval(y,env); ze <- eval(z,env)) yield
          (xe,ye,ze) match {
            case (NumValue(xv), NumValue(yv), NumValue(zv)) => LocationValue(new Location(p.world,xv,yv,zv))
            case _ => sys error s"bad location data: ${(xe,ye,ze)}"
          }
        case Origin  => pure(LocationValue(p.world.getHighestBlockAt(0,0)))
        case XYZ     => pure(LocationValue(p.world(x,y,z)))
        case X       => pure(NumValue(x))
        case Y       => pure(NumValue(y))
        case Z       => pure(NumValue(z))
        case MaxY    => pure(NumValue(255))
        case MinY    => pure(NumValue(0))
        case Num(i)  => pure(NumValue(i))
        case Bool(b) => pure(BoolValue(b))
      }
      def evalToLoc(e:Expr, env:Env): State[List[Effect],Location] =
        for(ev <- eval(e,env)) yield ev match {
          case LocationValue(l) => l
          case ev => sys error s"not a location: $ev"
        }
      def locationSideEffect(e:Expr, env:Env, f: Location => Effect): V =
        for(l <- evalToLoc(e,env); _ <- addSideEffect(f(l))) yield Unit

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