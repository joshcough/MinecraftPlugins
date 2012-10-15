package jcdc.pluginfactory

import org.bukkit.{Location, Material}
import org.bukkit.entity.Player

object MineLang extends EnrichmentClasses {

  case class Program(defs:List[Def], body:Expr)

  sealed trait Def
  case class Defn(name:Symbol, lam:Lambda) extends Def
  case class Val (name:Symbol, expr:Expr) extends Def

  sealed trait Expr
  case class Lambda(args:List[Symbol], body: Expr, recursive:Option[Symbol]) extends Expr
  case class Let(x:Symbol, e:Expr, body:Expr) extends Expr
  case class App(f:Expr, args:List[Expr]) extends Expr
  case class New(className:Symbol, args:List[Expr]) extends Expr
  case class MethodCall(obj:Expr, func:Symbol, args:List[Expr]) extends Expr
  case class Sequential(exps:List[Expr]) extends Expr
  case class Bool(b:Boolean) extends Expr
  case class Num(i:Int) extends Expr
  case class StringExpr(s:String) extends Expr
  case class Variable(s:Symbol) extends Expr
  case class EvaledExpr[V](v:Value) extends Expr
  case object UnitExpr extends Expr

  type Env = Map[Symbol,Value]

  sealed trait Value{ val value: Any }
  case class Closure[V](l:Lambda, env:Env)    extends Value{ val value = this }
  case class BoolValue(value:Boolean)         extends Value
  case class IntValue(value:Int)              extends Value
  case class StringValue(value:String)        extends Value
  case class ObjectValue(value:Any)           extends Value
  case class DynamicValue(value: () => Value) extends Value
  case class BuiltinFunction(name: Symbol, eval: (List[Expr], Env) => Value) extends Value{
    val value = this
  }
  case object UnitValue extends Value{ val value = () }
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

  def parseDefs(a:Any): List[Def] = {
    //println(a)
    a match {
      case Nil => sys error s"bad defs: $a"
      case l@(x :: xs) => l map parseDef
      case _ => sys error s"bad defs: $a"
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
    //println(a)
    a match {
      case List('lam, args, body) => parseLambda(args, body, None)
      case List('let, List(arg, expr), body) => arg match {
        case s:Symbol => Let(s, parseExpr(expr), parseExpr(body))
        case _ => sys error s"bad let argument: $a"
      }
      case 'begin :: body => Sequential(body map parseExpr)
      case 'new :: Symbol(className) :: args => New(Symbol(className), args map parseExpr)
      // other prims
      case i: Int                 => Num(i)
      case s:Symbol               => Variable(s)
      case s:String               => StringExpr(s)
      // finally, function application
      case f :: args              => {
        parseExpr(f) match {
          case v@Variable(s) if symbolToString(s).startsWith(".") =>
            args match {
              case a :: as => MethodCall(parseExpr(a), s, as map parseExpr)
              case _ => sys error "reflective call with no object!"
            }
          case func =>  App(func, args map parseExpr)
        }
      }
      case _                      => sys error s"bad expression: $a"
    }
  }

  def run(code:String, p:Player) = {
    val ast = parse(code)
    //println(ast)
    runProgram(ast, p)
  }
  def runProgram(prog:Program, p:Player) = new WorldEditInterp(p).evalProg(prog)

  case class WorldEditInterp(p:Player) {
    def builtIn[V](name:Symbol, eval: (List[Expr], Env) => Value) =
      (name -> BuiltinFunction(name, eval))

    def builtInUnit(name:Symbol, eval: (List[Expr], Env) => Unit) =
      (name -> BuiltinFunction(name, (es, env) => { eval(es,env); UnitValue }))

    // TODO on all these builtins, check the number of arguments.
    val ifStat = builtIn('if, (exps, env) => reduce(eval(exps(0), env)) match {
      case BoolValue(true)  => eval(exps(1), env)
      case BoolValue(false) => eval(exps(2), env)
      case ev => sys error s"bad if predicate: $ev"
    })
    val eqBuiltIn = builtIn('eq, (exps, env) =>
      (reduce(eval(exps(0), env)), reduce(eval(exps(1), env))) match {
        case (IntValue(av),      IntValue(bv))      => BoolValue(av == bv)
        case (BoolValue(av),     BoolValue(bv))     => BoolValue(av == bv)
        case (StringValue(av),   StringValue(bv))   => BoolValue(av == bv)
        case (ObjectValue(av),   ObjectValue(bv))   => BoolValue(av == bv)
        case _                                      => BoolValue(false)
      })
    val toStringPrim = builtIn(Symbol("to-string"), (exps, env) =>
      StringValue(reduce(eval(exps(0), env)).value.toString)
    )
    val add = builtIn('+, (exps, env) => {
      val vals = exps.map(e => reduce(eval(e, env)))
      if (vals.forall(_.isInstanceOf[IntValue])) // all numbers
        IntValue(vals.foldLeft(0){(acc,v) => v match {
          case IntValue(i) => acc + i
          case _ => acc // impossible, but shuts the compiler up
        }})
      else if (vals.forall(_.isInstanceOf[StringValue])) // all strings
        StringValue(vals.foldLeft(""){(acc,v) => v match {
          case StringValue(s) => acc + s
          case _ => acc // impossible, but shuts the compiler up
        }})
      else sys error s"+ expected all numbers or all strings, but got $vals"
    })
    val abs = builtIn('abs, (exps, env) => IntValue(Math.abs(evalToInt(exps(0), env))))
    def twoNumOp(name:Symbol)(f: (Int,Int) => Value) = builtIn(name, (exps, env) =>
      (reduce(eval(exps(0), env)), reduce(eval(exps(1), env))) match {
        case (IntValue(av), IntValue(bv)) => f(av, bv)
        case (av,bv) => sys error s"${symbolToString(name)} expected two numbers, but got: $av, $bv"
      }
    )
    val sub  = twoNumOp('-) ((i,j) => IntValue(i - j))
    val mult = twoNumOp('*) ((i,j) => IntValue(i * j))
    val lt   = twoNumOp('<) ((i,j) => BoolValue(i < j))
    val gt   = twoNumOp('>) ((i,j) => BoolValue(i > j))
    val lteq = twoNumOp('<=)((i,j) => BoolValue(i <= j))
    val gteq = twoNumOp('>=)((i,j) => BoolValue(i >= j))

    val printOnSameLine = builtInUnit('print, (exps, env) =>
      print(exps.map(e => reduce(eval(e, env)).value.toString).mkString(" ")))

    val printLine = builtInUnit('println, (exps, env) =>
      println(exps.map(e => reduce(eval(e, env)).value.toString).mkString("\n")))

    val getMaterial = builtIn('material, (exps, env) => {
      reduce(eval(exps(0),env)) match {
        case StringValue(s) => ObjectValue(BasicMinecraftParsers.material(s).get)
        case ev             => sys error s"not a material: $ev"
      }
    })

    val goto = builtInUnit('goto, (exps, env) => {
      val loc = evalToLocation(exps(0),env)
      p ! s"teleported to: ${loc.xyz}"; p.teleport(loc)
    })
    val loc = builtIn('loc, (exps, env) => {
      val (xe,ye,ze) = (reduce(eval(exps(0),env)),reduce(eval(exps(1),env)),reduce(eval(exps(2),env)))
      (xe,ye,ze) match {
        case (IntValue(xv), IntValue(yv), IntValue(zv)) =>
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

    val defaultEnv: Env = Map(
      // primitives
      'true   -> BoolValue(true),
      'false  -> BoolValue(false),
      'unit   -> UnitValue,
      // simple builtins
      eqBuiltIn, ifStat, toStringPrim, printOnSameLine, printLine,
      add, sub, mult, lt, lteq, gt, gteq, abs,
      // location functions
      loc, goto,
      'MAXY   -> IntValue(255),
      'MINY   -> IntValue(0),
      'X      -> DynamicValue(() => IntValue(p.x)),
      'X      -> DynamicValue(() => IntValue(p.x)),
      'Y      -> DynamicValue(() => IntValue(p.blockOn.y)),
      'Z      -> DynamicValue(() => IntValue(p.z)),
      'XYZ    -> DynamicValue(() => ObjectValue(p.blockOn.loc)),
      'origin -> DynamicValue(() => ObjectValue(p.world.getHighestBlockAt(0,0))),
      // material functions
      getMaterial,
      // mutable world edit functions
      setAll, changeSome, setWalls, setFloor
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
    def evalDef(env: Env, d:Def): Env = env + (d match {
      case Val (name, expr) => name -> eval(expr, env)
      case Defn(name, lam)  => name -> Closure(lam, env)
    })

    def reduce[V](v:Value): Value = v match {
      case DynamicValue(f) => f()
      case _ => v
    }

    def eval(e:Expr, env:Env): Value = {
      //println(e)
      e match {
        case Sequential(exps:List[Expr]) => exps.map(eval(_, env)).last
        case Bool(b)       => BoolValue(b)
        case Num(i)        => IntValue(i)
        case StringExpr(i) => StringValue(i)
        case EvaledExpr(v) => v
        case UnitExpr      => UnitValue
        case Variable(s) => env.get(s).getOrElse(sys error s"not found: $s in: ${env.keys}")
        case l@Lambda(_, _, _) => Closure(l, env)
        case Let(x:Symbol, e:Expr, body:Expr) =>
          eval(body, env + (x -> eval(e,env)))
        case App(f:Expr, args:List[Expr]) =>
          reduce(eval(f, env)) match {
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
          val clas: Class[_] = Class.forName(symbolToString(c))
          // then eval all the arguments
          val evaledArgs = args map (e => reduce(eval(e, env))) map (_.value)
          // then look for the right constructor
          val constructors = clas.getConstructors
          val matches = constructors.filter(c => matchesAll(c.getParameterTypes, evaledArgs))
          //val con = clas.getConstructor(getClasses(evaledArgs):_*)
          // todo: obviously do something better if there are more than one matches.
          matches.headOption.fold(
            sys error s"could not find constructor on class $c with args $evaledArgs"
          )(con =>
            // then call the constructor with the value (.value) of each of the args
            getValue(con.newInstance(evaledArgs.map(_.asInstanceOf[AnyRef]):_*).asInstanceOf[AnyRef])
          )
        }
        // TODO: better error handling in almost all cases
        case MethodCall(ob, func, args) => {
          // first, eval this dood to an object.
          val o = evalToObject(ob, env)
          // then eval all the arguments
          val evaledArgs = args map (eval(_, env)) map (_.value)
          // then lookup the function name via reflection
          val methodName = symbolToString(func).drop(1)
          val methods    = o.getClass.getMethods.filter(_.getName == methodName)
          val matches    = methods.filter(m => matchesAll(m.getParameterTypes, evaledArgs))
          // todo: obviously do something better if there are more than one matches.
          matches.headOption.fold(
            sys error s"could not find method $func on $o with args $evaledArgs"
          )(method =>
            getValue(method.invoke(o, evaledArgs.map(_.asInstanceOf[AnyRef]):_*))
          )
        }
      }
    }

    def evalTo[T](e:Expr, env:Env, argType:String)(f: PartialFunction[Value, T]): T = {
      val v = reduce(eval(e,env))
      if(f isDefinedAt v) f(v) else sys error s"not a valid $argType: $v"
    }
    def evalToInt(e:Expr, env:Env): Int =
      evalTo(e,env,"int"){ case IntValue(v) => v }
    def evalToLocation(e:Expr, env:Env): Location =
      evalTo(e,env,"location"){ case ObjectValue(l:Location) => l }
    def evalToMaterial(e:Expr, env:Env): Material =
      evalTo(e,env,"material"){ case ObjectValue(m:Material) => m }
    def evalToCube(e:Expr, env:Env): Cube =
      evalTo(e,env,"cube"){ case ObjectValue(c@Cube(_,_)) => c }
    def evalToObject(e:Expr, env:Env): Any =
      evalTo(e,env,"object"){ case ObjectValue(o) => o }

    def getValue(o:Object) = o match {
      // TODO: can i collapse all these Value classes into just ObjectValue?
      case s:String                => StringValue(s)
      case i:java.lang.Integer     => IntValue(i.intValue)
      case b:java.lang.Boolean     => BoolValue(b.booleanValue)
      case res                     => ObjectValue(res)
    }

    def getClasses(as:List[Any]): List[Class[_]] = as map (_ match {
      case i:Int     => classOf[Int]
      case b:Boolean => classOf[Boolean]
      case a         => a.getClass
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
      else a.getClass.isInstance(a)
    cs.size == as.size && cs.zip(as).forall((matches _).tupled)
  }

  def symbolToString(s:Symbol) = s.toString.drop(1)
}