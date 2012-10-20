package jcdc.pluginfactory

import org.bukkit.{Location, Material}
import org.bukkit.entity.Player

trait MineLangAST {
  case class Program(defs:List[Def], body:Expr)

  sealed trait Def
  case class Defn(name:Symbol, lam:Lambda) extends Def
  case class Val (name:Symbol, expr:Expr) extends Def

  sealed trait Expr
  case class Lambda(args:List[Symbol], body: Expr, recursive:Option[Symbol]) extends Expr
  case class Let(x:Symbol, e:Expr, body:Expr) extends Expr
  case class App(f:Expr, args:List[Expr]) extends Expr
  case class New(className:String, args:List[Expr]) extends Expr
  case class StaticMethodCall(className:String, func:String, args:List[Expr]) extends Expr
  case class StaticReference(className:String, field:String) extends Expr
  case class InstanceMethodCall(obj:Expr, func:String, args:List[Expr]) extends Expr
  case class Sequential(exps:List[Expr]) extends Expr
  case class Bool(b:Boolean) extends Expr
  case class Num(i:Int) extends Expr
  case class StringExpr(s:String) extends Expr
  case class Variable(s:Symbol) extends Expr
  case class EvaledExpr[V](v:Value) extends Expr
  case object UnitExpr extends Expr

  type Env = Map[Symbol,Value]

  sealed trait Value
  case class Closure(l:Lambda, env:Env) extends Value
  case class ObjectValue(value:Any)           extends Value
  case class DynamicValue(value: () => Value) extends Value
  case class BuiltinFunction(name: Symbol, eval: (List[Expr], Env) => Value) extends Value
}

trait MineLangParser extends MineLangAST {
  def read(code:String): Any = io.Reader read code

  def parse(code:String): Program = parseProgram(read(code))

  def parseProgram(a:Any): Program = a match {
    case Nil         => sys error s"bad program: $a"
    case List(x)     => Program(Nil,parseExpr(x))
    case l@(x :: xs) => Program(l.init map parseDef, parseExpr(l.last))
    case _           => sys error s"bad program: $a"
  }

  def parseDefs(a:Any): List[Def] = a match {
    case Nil         => sys error s"bad defs: $a"
    case l@(x :: xs) => l map parseDef
    case _           => sys error s"bad defs: $a"
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
    //println(a)
    a match {
      // some prims
      case i: Int                  => Num(i)
      case Symbol(s) if s.contains("/") => StaticReference(s.split('/')(0), s.split('/')(1))
      case s:Symbol                => Variable(s)
      case s:String                => StringExpr(s)
      // new, lam, let, begin
      case 'new :: Symbol(className) :: args => New(className, args map parseExpr)
      case List('lam, args, body)  => parseLambda(args, body, None)
      case List('let, List(arg, expr), body) => arg match {
        case s:Symbol => Let(s, parseExpr(expr), parseExpr(body))
        case _ => sys error s"bad let argument: $a"
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

  def evalred   (e:Expr, env:Env): Value = reduce(eval(e, env))
  def evalredval(e:Expr, env:Env): Any = evalred(e, env) match {
    case Closure(l:Lambda, env:Env)  => ???
    case ObjectValue(a)              => a
    case DynamicValue(getter)        => sys error "shouldnt be possible, we already reduced"
    case BuiltinFunction(name, eval) => ???
  }

  def eval(e:Expr, env:Env): Value = {
    //println(e)
    e match {
      case Sequential(exps:List[Expr]) => exps.map(eval(_, env)).last
      case Bool(b)       => ObjectValue(b)
      case Num(i)        => ObjectValue(i)
      case StringExpr(s) => ObjectValue(s)
      case EvaledExpr(v) => v
      case UnitExpr      => ObjectValue(())
      case Variable(s)   => env.get(s).getOrElse(sys error s"not found: $s in: ${env.keys}")
      case l@Lambda(_, _, _) => Closure(l, env)
      case Let(x:Symbol, e:Expr, body:Expr) => eval(body, env + (x -> eval(e,env)))
      case App(f:Expr, args:List[Expr]) =>
        evalred(f, env) match {
          // todo: make sure formals.size == args.size...
          // or partially apply?
          case c@Closure(Lambda(formals, body, rec), closedOverEnv) =>
            val envWithoutRecursion = closedOverEnv ++ formals.zip(args map (eval(_, env)))
            val finalEnv =
              rec.fold(envWithoutRecursion)(name => envWithoutRecursion + (name -> c))
            eval(body, finalEnv)
          case BuiltinFunction(name, f) => f(args, env)
          case blah => sys error s"app expected a function, but got: $blah"
        }
      // TODO: better error handling in almost all cases
      case New(c, args) => {
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
      }
      // TODO: better error handling in almost all cases
      case InstanceMethodCall(ob, func, args) => {
        // first, eval this dood to an object.
        val o = evalToObject(ob, env)
        invoke(o.getClass, func, o, args map (evalredval(_, env)))
      }
      case StaticMethodCall(className, func, args) =>
        invoke(Class.forName(className), func, null, args map (evalredval(_, env)))
      case StaticReference(className, field) =>
        ObjectValue(Class.forName(className).getField(field).get(null))
    }
  }

  def evalTo[T](e:Expr, env:Env, argType:String)(f: PartialFunction[Value, T]): T = {
    val v = evalred(e,env)
    if(f isDefinedAt v) f(v) else sys error s"not a valid $argType: $v"
  }
  def evalToInt(e:Expr, env:Env): Int =
    evalTo(e,env,"int"){ case ObjectValue(v:Int) => v }
  def evalToLocation(e:Expr, env:Env): Location =
    evalTo(e,env,"location"){ case ObjectValue(l:Location) => l }
  def evalToMaterial(e:Expr, env:Env): Material =
    evalTo(e,env,"material"){ case ObjectValue(m:Material) => m }
  def evalToCube(e:Expr, env:Env): Cube =
    evalTo(e,env,"cube"){ case ObjectValue(c@Cube(_,_)) => c }
  def evalToObject(e:Expr, env:Env): Any =
    evalTo(e,env,"object"){ case ObjectValue(o) => o }

  def getClasses(as:List[Any]): List[Class[_]] = as map (_ match {
    case i:Int     => classOf[Int]
    case b:Boolean => classOf[Boolean]
    case a         => a.getClass
  })

  def invoke(c:Class[_], methodName:String, invokedOn: Any, args:List[Any]) = {
    val methods    = c.getMethods.filter(_.getName == methodName)
    val matches    = methods.filter(m => matchesAll(m.getParameterTypes, args))
    // todo: obviously do something better if there are more than one matches.
    matches.headOption.fold(
      sys error s"could not find method $c.$methodName with args ${args.map(_.getClass).mkString(",")}"
    )(method => {
      //println(args)
      ObjectValue(method.invoke(invokedOn, args.map(_.asInstanceOf[AnyRef]):_*))
    })
  }
  // TODO: repeat this for all AnyVal types.
  def matchesAll(cs:Seq[Class[_]], as:Seq[Any]) = {
    //    println(s"matching $cs with $as")
    def isInt(a:Any)  = a.isInstanceOf[Int] || a.isInstanceOf[Integer]
    def isUnit(a:Any) = a.isInstanceOf[Unit] || a.isInstanceOf[scala.runtime.BoxedUnit]
    def matches(c:Class[_], a:Any): Boolean =
      if      (c == classOf[Int]     && isInt(a))  true
      else if (c == classOf[Integer] && isInt(a))  true
      else if (c == classOf[Unit]    && isUnit(a)) true
      else if (c == classOf[scala.runtime.BoxedUnit] && isUnit(a)) true
      else if (c.isAssignableFrom(classOf[Function1[_,_]])) {
        println("yo dog, found a function 1!")
        false
      }
      else a.getClass.isInstance(a)
    cs.size == as.size && cs.zip(as).forall((matches _).tupled)
  }

  def builtIn[V](name:Symbol, eval: (List[Expr], Env) => Value) =
    (name -> BuiltinFunction(name, eval))

  def builtInUnit(name:Symbol, eval: (List[Expr], Env) => Unit) =
    (name -> BuiltinFunction(name, (es, env) => { ObjectValue(eval(es,env)) }))
}

trait MineLangCore extends MineLangInterpreter with MineLangParser {

  val UnitValue  = ObjectValue(())
  val TrueValue  = ObjectValue(true)
  val FalseValue = ObjectValue(false)

  // TODO on all these builtins, check the number of arguments.
  val ifStat = builtIn('if, (exps, env) => evalred(exps(0), env) match {
    case ObjectValue(true)  => eval(exps(1), env)
    case ObjectValue(false) => eval(exps(2), env)
    case ev => sys error s"bad if predicate: $ev"
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
  val add = builtIn('+, (exps, env) => {
    val vals = exps.map(e => evalredval(e, env))
    if (vals.forall(_.isInstanceOf[Int])) // all numbers
      ObjectValue(vals.map(_.asInstanceOf[Int]).foldLeft(0){(acc,i) => acc + i})
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
  val lt   = twoNumOp('<) ((i,j) => ObjectValue(i < j))
  val gt   = twoNumOp('>) ((i,j) => ObjectValue(i > j))
  val lteq = twoNumOp('<=)((i,j) => ObjectValue(i <= j))
  val gteq = twoNumOp('>=)((i,j) => ObjectValue(i >= j))

  val printOnSameLine = builtInUnit('print, (exps, env) =>
    print(exps.map(e => evalredval(e, env).toString).mkString(" ")))

  val printLine = builtInUnit('println, (exps, env) =>
    println(exps.map(e => evalredval(e, env).toString).mkString("\n")))

  val boolLib = List(
    "(def and (a b) (if a b false))",
    "(def or  (a b) (if a true b))",
    "(def zero? (x) (if (eq x 0) true false))",
    "(def not (x) (if x 1 0))"
  ).map(s => parseDef(read(s)))

  private val initialLib: Env = Map(
    // primitives
    'true   -> ObjectValue(true),
    'false  -> ObjectValue(false),
    'unit   -> ObjectValue(()),
    // simple builtins
    eqBuiltIn, ifStat, toStringPrim, printOnSameLine, printLine,
    add, sub, mult, lt, lteq, gt, gteq, abs
  )

  val lib = boolLib.foldLeft(initialLib)(evalDef)
}

object MineLang extends EnrichmentClasses with MineLangParser with MineLangInterpreter with MineLangCore {

  def run(code:String, p:Player) = {
    val ast = parse(code)
    //println(ast)
    runProgram(ast, p)
  }
  def runProgram(prog:Program, p:Player) = evalProg(prog, lib ++ new WorldEditExtension(p).lib)

  case class WorldEditExtension(p:Player) {
    val getMaterial = builtIn('material, (exps, env) => {
      evalred(exps(0),env) match {
        case ObjectValue(s:String) => ObjectValue(
          BasicMinecraftParsers.material(s).fold(sys error _)((m, _) => m)
        )
        case ev                    => sys error s"not a material: $ev"
      }
    })

    val goto = builtInUnit('goto, (exps, env) => {
      val loc = evalToLocation(exps(0),env)
      p ! s"teleported to: ${loc.xyz}"; p.teleport(loc)
    })
    val loc = builtIn('loc, (exps, env) => {
      val (xe,ye,ze) = (evalred(exps(0),env),evalred(exps(1),env),evalred(exps(2),env))
      (xe,ye,ze) match {
        case (ObjectValue(xv:Int), ObjectValue(yv:Int), ObjectValue(zv:Int)) =>
          ObjectValue(new Location(p.world,xv,yv,zv))
        case _ => sys error s"bad location data: ${(xe,ye,ze)}"
      }
    })

    // here are all the cube block mutation functions.
    val setAll = builtInUnit(Symbol("cube:set-all"), (exps, env) => {
      val c = evalToCube(exps(0), env)
      val m = evalToMaterial(exps(1), env)
      for(b <- c) b changeTo m
      p ! s"setting all in $c to $m"
    })
    val changeSome = builtInUnit(Symbol("cube:change"), ((exps, env) => {
      val c    = evalToCube(exps(0), env)
      val oldM = evalToMaterial(exps(1),env)
      val newM = evalToMaterial(exps(2),env)
      for(b <- c; if(b is oldM)) b changeTo newM
      p ! s"changed $oldM in $c to $newM"
    }))
    val setWalls = builtInUnit(Symbol("cube:set-walls"), ((exps, env) => {
      val c = evalToCube(exps(0), env)
      val m = evalToMaterial(exps(1),env)
      c.walls.foreach(_ changeTo m)
      p ! s"set walls in $c to: $m"
    }))
    val setFloor = builtInUnit(Symbol("cube:set-floor"), ((exps, env) => {
      val c = evalToCube(exps(0), env)
      val m = evalToMaterial(exps(1),env)
      c.floor.foreach(_ changeTo m)
      p ! s"set floor in $c to: $m"
    }))

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
      setAll, changeSome, setWalls, setFloor
    )
  }
}