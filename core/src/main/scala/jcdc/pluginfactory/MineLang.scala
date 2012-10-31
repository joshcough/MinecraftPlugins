package jcdc.pluginfactory

import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import java.io.File

trait MineLangAST {
  case class Program(defs:List[Def], body:Expr)

  sealed trait Def{ val name:Symbol }
    case class Defn(name:Symbol, lam:Lambda) extends Def
    case class Val (name:Symbol, expr:Expr)  extends Def

  sealed trait Expr
    case class Lambda(args:List[Symbol], body: Expr, recursive:Option[Symbol]) extends Expr
    case class Let(x:Symbol, e:Expr, body:Expr) extends Expr
    case class LetRec(x:Symbol, e:Lambda, body:Expr) extends Expr
    case class App(f:Expr, args:List[Expr]) extends Expr
    case class New(className:String, args:List[Expr]) extends Expr
    case class StaticMethodCall(className:String, func:String, args:List[Expr]) extends Expr
    case class StaticReference(className:String, field:String) extends Expr
    case class InstanceMethodCall(obj:Expr, func:String, args:List[Expr]) extends Expr
    case class Sequential(exps:List[Expr]) extends Expr
    case class Bool(b:Boolean)      extends Expr
    case class Num(i:Int)           extends Expr
    case class StringExpr(s:String) extends Expr
    case class Variable(s:Symbol)   extends Expr
    case class EvaledExpr(v:Value)  extends Expr

  type Env = Map[Symbol,Value]

  sealed trait Value
    case class Closure(l:Lambda, env:Env) extends Value
    case class ObjectValue(value:Any)           extends Value
    case class DynamicValue(value: () => Value) extends Value
    case class BuiltinFunction(name: Symbol, eval: (List[Expr], Env) => Value) extends Value
}

trait MineLangParser extends MineLangAST with io.Reader {
  def parse(code:String): Program = parseProgram(read(code))

  def parseProgram(a:Any): Program = {
    a match {
      case Nil         => sys error s"bad program: $a"
      case List(x)     => Program(Nil,parseExpr(x))
      case l@(x :: xs) => Program(l.init map parseDef, parseExpr(l.last))
      case _           => sys error s"bad program: $a"
    }
  }

  def parseDefs(a:Any): List[Def] ={
    a match {
      case Nil         => sys error s"bad defs: $a"
      // first, try to parse the code as just a list of defs
      // if that fails, try to parse it as a whole program
      case l@(x :: xs) => try l map parseDef catch { case e:Exception => parseProgram(a).defs }
      case _           => sys error s"bad defs: $a"
    }
  }

  def parseDef(a:Any): Def = {
    def parseName(name:Any): Symbol = name match {
      case s:Symbol => s // TODO: check s against builtin things like X,Y,Z,etc
      case _ => sys error s"bad def name: $a"
    }
    a match {
      // catch def with one body expression
      case List('def,    name, args, body) =>
        Defn(parseName(name), parseLambda(args, body, recursive=None))
      case 'def :: name :: args :: bodyStatements =>
        Defn(parseName(name), parseLambda(args, 'begin :: bodyStatements, recursive=None))
      // catch defrec with one body expression
      case List('defrec, name, args, body) =>
        val n = parseName(name)
        Defn(n, parseLambda(args, body, recursive=Some(n)))
      case 'defrec :: name :: args :: bodyStatements =>
        val n = parseName(name)
        Defn(n, parseLambda(args, 'begin :: bodyStatements, recursive=Some(n)))
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
        case Nil => Nil
        case _ => sys error s"bad lambda arg list: $a"
      }
    }
    Lambda(parseLamArgList(args), parseExpr(body), recursive)
  }

  def parseLet(arg:Any, expr:Any, body:Any) = arg match {
    case s:Symbol => Let(s, parseExpr(expr), parseExpr(body))
    case _ => sys error s"bad let argument: $arg"
  }

  def parseExpr(a:Any): Expr = {
    a match {
      // some prims
      case i: Int                  => Num(i)
      case Symbol(s) if s.contains("/") => StaticReference(s.split('/')(0), s.split('/')(1))
      case s:Symbol                => Variable(s)
      case s:String                => StringExpr(s)
      // new, lam, let, begin
      case 'new :: Symbol(className) :: args => New(className, args map parseExpr)
      case List('lam, args, body)  => parseLambda(args, body, None)
      case 'lam :: args :: body    => parseLambda(args, 'begin :: body, None)
      case List('let, List(arg, expr), body) => parseLet(arg,expr,body)
      case List(Symbol("let*"), args, body)  => args match {
        case Nil                     => parseExpr(body)
        case List(arg1, body1) :: xs => parseLet(arg1, body1, List(Symbol("let*"), xs, body))
        case _                       => sys error s"bad let* arguments: $args"
      }
      case List('letrec, List(arg, List('lam, args, expr)), body) => arg match {
        case s:Symbol => LetRec(s, parseLambda(args, expr, Some(s)), parseExpr(body))
        case _        => sys error s"bad letrec argument: $a"
      }
      case 'begin :: body          => Sequential(body map parseExpr)
      // finally, function application
      case f :: args               => {
        parseExpr(f) match {
          case Variable(Symbol(s)) if s.startsWith(".") => args match {
            case a :: as => InstanceMethodCall(parseExpr(a), s.drop(1), as map parseExpr)
            case _ => sys error "reflective call with no object!"
          }
          // turn static references into static function calls here.
          case StaticReference(clazz, func) => StaticMethodCall(clazz, func, args map parseExpr)
          case func =>  App(func, args map parseExpr)
        }
      }
      case _                       => sys error s"bad expression: $a"
    }
  }
}

trait MineLangInterpreter extends MineLangAST {
  def debug[T](t: => T): T = { println(t); t }

  // evaluates the defs in order (no forward references allowed)
  // then evaluates the body with the resulting environment
  def evalProg(prog:Program, defaultEnv:Env): Value =
    eval(prog.body, prog.defs.foldLeft(defaultEnv)(evalDef))

  // extends the env, and collects side effects for vals
  def evalDef(env: Env, d:Def): Env = env + (d match {
    case Val (name, expr) => name -> eval(expr, env)
    case Defn(name, lam)  => name -> Closure(lam, env)
  })

  def reduce(v:Value): Value = v match {
    case DynamicValue(f) => reduce(f())
    case _ => v
  }

  def call(c:Closure, args:List[Any], env:Env): Value = {
    evalred(App(EvaledExpr(c), args.map(a => EvaledExpr(ObjectValue(a)))), env)
  }

  def evalred   (e:Expr, env:Env): Value = reduce(eval(e, env))
  def evalredval(e:Expr, env:Env): Any = unbox(evalred(e, env))
  def unbox(v:Value): Any = v match {
    case c@Closure(l, env)  => l args match {
      case Nil      => () => ()
      case a1 :: Nil => (a2: Any) => unbox(call(c, List(a2), env))
      case a1 :: b1 :: Nil => (a2: Any, b2: Any) => unbox(call(c, List(a2,b2), env))
    }
    case ObjectValue(a)              => a
    case DynamicValue(getter)        => sys error "shouldnt be possible, we already reduced"
    case BuiltinFunction(name, eval) => ???
  }

  def eval(e:Expr, env:Env): Value = {
    //println(s"eval: $e")
    val res = e match {
      case Sequential(exps) => exps.map(eval(_, env)).last
      case Bool(b)            => ObjectValue(b)
      case Num(i)             => ObjectValue(i)
      case StringExpr(s)      => ObjectValue(s)
      case EvaledExpr(v)      => v
      case Variable(s)        => env.get(s).getOrElse(sys error s"not found: $s in: ${env.keys}")
      case l@Lambda(_, _, _)  => Closure(l, env)
      case Let(x, e, body)    => eval(body, env + (x -> eval(e,env)))
      case LetRec(x, e, body) => eval(body, env + (x -> eval(e, env + (x -> Closure(e,env)))))
      case App(f, args)       =>
        evalred(f, env) match {
          // todo: make sure formals.size == args.size...
          // or partially apply?
          case c@Closure(Lambda(formals, body, rec), closedOverEnv) =>
            val envWORecursion   = closedOverEnv ++ formals.zip(args map (eval(_, env)))
            val envWithRecursion = rec.fold(envWORecursion)(name => envWORecursion + (name -> c))
            eval(body, envWithRecursion)
          case BuiltinFunction(name, f) => f(args, env)
          case blah => sys error s"app expected a function, but got: $blah"
        }
      // TODO: better error handling in almost all cases
      case New(c, args) =>
        // first, go look up the class by name
        val clas: Class[_] = Class.forName(c)
        // then eval all the arguments
        val evaledArgs = args map (e => evalredval(e, env))
        // then look for the right constructor
        val constructors = clas.getConstructors
        val matches = constructors.filter(c => matchesAll(c.getParameterTypes, evaledArgs))
        //val con = clas.getConstructor(getClasses(evaledArgs):_*)
        // todo: obviously do something better if there are more than one matches.
        matches.headOption.fold(
          sys error s"could not find constructor on class $c with args $evaledArgs"
        )(con =>
        // then call the constructor with the value (.value) of each of the args
          ObjectValue(con.newInstance(evaledArgs.map(_.asInstanceOf[AnyRef]):_*).asInstanceOf[AnyRef])
        )
      // TODO: better error handling in almost all cases
      case InstanceMethodCall(ob, func, args) =>
        // first, eval this dood to an object.
        val o = evalToObject(ob, env)
        invoke(o.getClass, func, o, args map (evalredval(_, env)))
      case StaticMethodCall(className, func, args) =>
        invoke(Class.forName(className), func, null, args map (evalredval(_, env)))
      case StaticReference(className, field) =>
        ObjectValue(Class.forName(className).getField(field).get(null))
    }
    //println(s"eval res: $res")
    res
  }

  def evalTo[T](e:Expr, env:Env, argType:String)(f: PartialFunction[Value, T]): T = {
    val v = evalred(e,env)
    if(f isDefinedAt v) f(v) else sys error s"not a valid $argType: $v"
  }
  protected def allNumbers(as: List[Any]) =
    as.forall(x => x.isInstanceOf[Int] || x.isInstanceOf[Double])
  protected def toInt(a:Any): Int = a match {
    case i:Int => i
    case d:Double => d.toInt
    case  _ => sys error s"not a number: $a"
  }
  def evalToInt(e:Expr, env:Env): Int =
    evalTo(e,env,"int"){ case ObjectValue(v:Int) => v }
  def evalToObject(e:Expr, env:Env): Any =
    evalTo(e,env,"object"){ case ObjectValue(o) => o }

  def getClasses(as:List[Any]): List[Class[_]] = as map (_ match {
    case i:Int     => classOf[Int]
    case b:Boolean => classOf[Boolean]
    case a         => a.getClass
  })

  def invoke(c:Class[_], methodName:String, invokedOn: Any, args:List[Any]) = {
    //println(s"trying to invoke: $c.$methodName (on object $invokedOn) w/ args: $args")
    //println(c.getMethods.filter(_.getName == "map").mkString(","))
    val methods    = c.getMethods.filter(_.getName == methodName)
    val matches    = methods.filter(m => matchesAll(m.getParameterTypes, args))
    // todo: obviously do something better if there are more than one matches.
    matches.headOption.fold(
      sys error s"could not find method $c.$methodName (on object $invokedOn) with args ${
        args.map(_.getClass).mkString(",")
      }"
    )(method => {
      val finalArgs = args.map(_.asInstanceOf[AnyRef])
      val res = method.invoke(invokedOn, finalArgs:_*)
      ObjectValue(res)
    })
  }
  // TODO: repeat this for all AnyVal types.
  def matchesAll(cs:Seq[Class[_]], as:Seq[Any]) = {
    def isInt(a:Any)  = a.isInstanceOf[Int]  || a.isInstanceOf[Integer]
    def isUnit(a:Any) = a.isInstanceOf[Unit] || a.isInstanceOf[scala.runtime.BoxedUnit]
    def matches(c:Class[_], a:Any): Boolean =
      if      (c == classOf[Int]     && isInt(a))  true
      else if (c == classOf[Integer] && isInt(a))  true
      else if (c == classOf[Unit]    && isUnit(a)) true
      else if (c == classOf[scala.runtime.BoxedUnit] && isUnit(a)) true
      else if (classOf[Function1[_,_]].isAssignableFrom(c) &&
               classOf[Function1[_,_]].isAssignableFrom(a.getClass)) true
      else a.getClass.isInstance(a)
    cs.size == as.size && cs.zip(as).forall((matches _).tupled)
  }

  def builtIn(name:Symbol, eval: (List[Expr], Env) => Value) =
    (name -> BuiltinFunction(name, eval))
  def builtInNil(name:Symbol, eval: (List[Expr], Env) => Unit) =
    (name -> BuiltinFunction(name, (es, env) => { ObjectValue(eval(es,env)) }))
}

trait MineLangCore extends MineLangInterpreter with MineLangParser {

  val NilValue   = ObjectValue(())
  val TrueValue  = ObjectValue(true)
  val FalseValue = ObjectValue(false)

  // TODO on all these builtins, check the number of arguments.
  val isa = builtIn(Symbol("isa?"), (exps, env) => (evalred(exps(0), env), exps(1)) match {
    case (NilValue, Variable(Symbol("scala.collection.immutable.List"))) =>
      ObjectValue(true)
    case (o, Variable(Symbol(className)))  =>
      ObjectValue(Class.forName(className).isAssignableFrom(o.getClass))
    case (o, c) =>
      sys error s"invalid class name: $c"
  })
  val ifStat = builtIn('if, (exps, env) => evalred(exps(0), env) match {
    case ObjectValue(true)  => eval(exps(1), env)
    case ObjectValue(false) => eval(exps(2), env)
    case ev                 => sys error s"bad if predicate: $ev"
  })
  // TODO: this could be done with a macro
  val unless = builtIn('unless, (exps, env) => evalred(exps(0), env) match {
    case ObjectValue(true)  => NilValue
    case ObjectValue(false) => eval(exps(1), env)
    case ev                 => sys error s"bad unless predicate: $ev"
  })
  val eqBuiltIn = builtIn('eq, (exps, env) =>
    (evalred(exps(0), env), evalred(exps(1), env)) match {
      case (ObjectValue(av), ObjectValue(bv)) => ObjectValue(av == bv)
      // todo: could we handle lambdas somehow? does anyone ever do that? is it insane?
      // todo: and what about BuiltinFunctions?
      case _                                  => ObjectValue(false)
    })
  val toStringPrim = builtIn(Symbol("to-string"), (exps, env) =>
    ObjectValue(evalredval(exps(0), env).toString)
  )
  val spawn = builtIn('spawn, (exps, env) =>
    (evalred(exps(0), env),evalred(exps(1), env),evalred(exps(2), env)) match {
      case (ObjectValue(n:Int), ObjectValue(waitTime:Int), c@Closure(lam, env))  =>
        new Thread(new Runnable() {
          def run(){ (1 to n).foreach(n => {call(c, List(n), env); Thread.sleep(waitTime * 1000)})}
        }).start
        NilValue
      case (i,w,f) => sys error s"spawn expected <int> <int> <function>, but got: $i, $w $f"
    }
  )
  val add = builtIn('+, (exps, env) => {
    val vals = exps.map(e => evalredval(e, env))
    if (allNumbers(vals)){ // all numbers
      ObjectValue(vals.map(toInt).foldLeft(0){(acc,i) => acc + i})
    }
    else if (vals.forall(_.isInstanceOf[String])) // all strings
      ObjectValue(vals.foldLeft(""){(acc,s) => acc + s })
    else sys error s"+ expected all numbers or all strings, but got $vals"
  })
  val abs = builtIn('abs, (exps, env) => ObjectValue(Math.abs(evalToInt(exps(0), env))))
  def twoNumOp(name:Symbol)(f: (Int,Int) => Value) = builtIn(name, (exps, env) =>
    (evalred(exps(0), env), evalred(exps(1), env)) match {
      case (ObjectValue(av:Int), ObjectValue(bv:Int)) => f(av, bv)
      case (av,bv) => sys error s"${name.toString drop 1} expected two numbers, but got: $av, $bv"
    }
  )
  val sub  = twoNumOp('-) ((i,j) => ObjectValue(i - j))
  val mult = twoNumOp('*) ((i,j) => ObjectValue(i * j))
  val mod  = twoNumOp('%) ((i,j) => ObjectValue(i % j))
  val lt   = twoNumOp('<) ((i,j) => ObjectValue(i < j))
  val gt   = twoNumOp('>) ((i,j) => ObjectValue(i > j))
  val lteq = twoNumOp('<=)((i,j) => ObjectValue(i <= j))
  val gteq = twoNumOp('>=)((i,j) => ObjectValue(i >= j))

  val printOnSameLine = builtInNil('print, (exps, env) =>
    print(exps.map(e => evalredval(e, env).toString).mkString(" ")))

  val printLine = builtInNil('println, (exps, env) =>
    println(exps.map(e => evalredval(e, env).toString).mkString("\n")))

  val boolLib = List(
    "(def and (a b) (if a b false))",
    "(def or  (a b) (if a true b))",
    "(def zero? (x) (if (eq x 0) true false))",
    "(def not (x) (if x 1 0))"
  ).map(s => parseDef(read(s)))

  //http://stackoverflow.com/questions/6578615/how-to-use-scala-collection-immutable-list-in-a-java-code
  val listLib = List(
    "(val empty scala.collection.immutable.Nil$/MODULE$)",
    "(def empty? (l)   (or (eq? l nil) (eq? l false)))",
    "(def cons   (h t) (.apply scala.collection.immutable.$colon$colon$/MODULE$ h t))",
    "(def list?  (l)   (isa? l scala.collection.immutable.List))"
  ).map(s => parseDef(read(s)))

  private val initialLib: Env = Map(
    // primitives
    'true  -> ObjectValue(true),
    'false -> ObjectValue(false),
    'nil   -> ObjectValue(()),
    // simple builtins
    eqBuiltIn, ifStat, unless, toStringPrim, printOnSameLine, printLine,
    add, sub, mult, mod, lt, lteq, gt, gteq, abs,
    'random -> DynamicValue(() => ObjectValue(math.random)),
    spawn
  )

  val lib = (boolLib ::: listLib).foldLeft(initialLib)(evalDef)
}

object MineLang extends EnrichmentClasses with MineLangCore {

  def run(code:String, p:Player) = runProgram(parse(code), p)
  def runExpr(code:String, p:Player) = unbox(runProgram(parse(s"($code)"), p))
  def runProgram(prog:Program, p:Player) = evalProg(prog, new WorldEditExtension(p).lib ++ lib)

  case class WorldEditExtension(p:Player) {

    def evalToLocation(e:Expr, env:Env): Location =
      evalTo(e,env,"location"){ case ObjectValue(l:Location) => l }
    def evalToMaterial(e:Expr, env:Env): Material =
      evalTo(e,env,"material"){
        case ObjectValue(m:Material) => m
        case ObjectValue(s:String) => BasicMinecraftParsers.material(s).fold(sys error _)((m, _) => m)
      }
    def evalToCube(e:Expr, env:Env): Cube =
      evalTo(e,env,"cube"){ case ObjectValue(c@Cube(_,_)) => c }

    val getMaterial = builtIn('material, (exps, env) => {
      evalred(exps(0),env) match {
        case ObjectValue(s:String) => ObjectValue(
          BasicMinecraftParsers.material(s).fold(sys error _)((m, _) => m)
        )
        case ev                    => sys error s"not a material: $ev"
      }
    })

    val goto = builtInNil('goto, (exps, env) => {
      val loc = evalToLocation(exps(0),env)
      p ! s"teleported to: ${loc.xyz}"; p.teleport(loc)
    })
    val loc = builtIn('loc, (exps, env) => {
      val (xe,ye,ze) = (evalredval(exps(0),env),evalredval(exps(1),env),evalredval(exps(2),env))
      if (allNumbers(List(xe,ye,ze)))
        ObjectValue(new Location(p.world,toInt(xe),toInt(ye),toInt(ze)))
      else sys error s"bad location data: ${(xe,ye,ze)}"
    })

    // here are all the cube block mutation functions.
    def builtInCube(name:Symbol, eval: (List[Expr], Env) => Cube) =
      (name -> BuiltinFunction(name, (es, env) => { ObjectValue(eval(es,env)) }))
    val setAll = builtInCube(Symbol("cube:set-all"), (exps, env) => {
      val c = evalToCube(exps(0), env)
      val m = evalToMaterial(exps(1), env)
      for(b <- c) b changeTo m
      p ! s"setting all in $c to $m"
      c
    })
    val changeSome = builtInCube(Symbol("cube:change"), ((exps, env) => {
      val c    = evalToCube(exps(0), env)
      val oldM = evalToMaterial(exps(1),env)
      val newM = evalToMaterial(exps(2),env)
      for(b <- c; if(b is oldM)) b changeTo newM
      p ! s"changed $oldM in $c to $newM"
      c
    }))
    val setWalls = builtInCube(Symbol("cube:set-walls"), ((exps, env) => {
      val c = evalToCube(exps(0), env)
      val m = evalToMaterial(exps(1),env)
      c.walls.foreach(_ changeTo m)
      p ! s"set walls in $c to: $m"
      c
    }))
    val setFloor = builtInCube(Symbol("cube:set-floor"), ((exps, env) => {
      val c = evalToCube(exps(0), env)
      val m = evalToMaterial(exps(1),env)
      c.floor.foreach(_ changeTo m)
      p ! s"set floor in $c to: $m"
      c
    }))
    val message = builtInNil('message, (exps, env) =>
      p ! (exps.map(e => evalredval(e, env).toString).mkString("\n"))
    )

    val lib: Env = Map(
      loc, goto,
      'MAXY   -> ObjectValue(255),
      'MINY   -> ObjectValue(0),
      'X      -> DynamicValue(() => ObjectValue(p.x)),
      'X      -> DynamicValue(() => ObjectValue(p.x)),
      'Y      -> DynamicValue(() => ObjectValue(p.blockOn.y)),
      'Z      -> DynamicValue(() => ObjectValue(p.z)),
      'XYZ    -> DynamicValue(() => ObjectValue(p.blockOn.loc)),
      'origin -> DynamicValue(() => ObjectValue(p.world.getHighestBlockAt(0,0))),
      // material functions
      getMaterial,
      // mutable world edit functions
      setAll, changeSome, setWalls, setFloor,
      // send a message to the player
      message
    )
  }
}

object MineLangRepl {

  import scala.tools.jline.console.ConsoleReader
  import scala.tools.jline.console.history.{FileHistory}
  //.{ArgumentCompletor, Completor, ConsoleReader, MultiCompletor, NullCompletor, SimpleCompletor}

  class Session(p:Player){
    import MineLang._
    var count = 0
    val baseLib = new WorldEditExtension(p).lib ++ lib
    var currentLib = baseLib
    def runExpr(code:String) = {
      val name = nextName
      try {
        val res = runProgram(parse(s"($code)"))
        currentLib = currentLib + (name -> res)
        println(s"${name.toString.drop(1)}: ${unbox(res)}")
      } catch { case e: Exception => e.printStackTrace }
    }
    def nextName: Symbol = {
      val n = Symbol(s"res$count")
      if(! currentLib.contains(n)) n else { count = count + 1; nextName }
    }
    def runProgram(prog:Program): Value = evalProg(prog, currentLib)
  }

  val session = new Session(TestServer.player)

  val input = new ConsoleReader(){
    setBellEnabled(false)
    val history = {
      val historyFile = new File(s"${System.getProperty("user.home")}/.mc-history")
      historyFile.createNewFile
      new FileHistory(historyFile)
    }
    setHistory(history)
    setHistoryEnabled(true)
    //cr addCompletor completor
    def saveHistory: Unit = history.flush
    def next = readLine("mc> ")
  }

  def main(args:Array[String]): Unit = {
    var next = input.next
    while(next != null) {
      session runExpr next
      next = input.next
    }
    input.saveHistory
  }
}
