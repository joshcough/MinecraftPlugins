package jcdc.pluginfactory

import java.io.File

object ClojureInScala {

  object Reader extends Reader
  import Reader._

  trait Reader {
    def read(s:String): Any = read(stripComments(s).toList)
    def read(f:File)  : Any = read(fileToString(f))
    def fileToString(f:File): String = scala.io.Source.fromFile(f).getLines().mkString("\n")
  
    def stripComments(code:String) = code.split("\n").map(s => s.takeWhile(_!=';').trim).mkString(" ")
    def read(data:List[Char]): Any = readWithRest(data)._1
    def readWithRest(s:String): (Any, String) = {
      val rwr = readWithRest(stripComments(s).toList)
      (rwr._1, rwr._2.mkString.trim)
    }
    def readWithRest(data:List[Char]): (Any, List[Char]) = {
      def readList(data: List[Char], acc: List[Any], terminator:Char): (List[Any], List[Char]) = data match {
        case ' ' :: tail => readList(tail, acc, terminator)
        case x   :: tail if x == terminator => (acc, tail)
        case x   :: tail =>
          val (next, rest) = readWithRest(data)
          readList(rest, acc ::: List(next), terminator)
        case List()     => die("unclosed list")
      }
      def readSymbol(data:List[Char]): (Symbol, List[Char]) = {
        val (chars, rest) = data.span( ! List('(', ')', '[', ']', ' ', '\n').contains(_) )
        (Symbol(chars.mkString), rest)
      }
      def readNumOrMaybeSymbol(data:List[Char], negate:Boolean): (Any, List[Char]) = {
        val (chars, rest) = data.span( ! List('(', ')', '[', ']', ' ', '\n').contains(_) )
        // if there are any non number characters, this must be a symbol
        if(chars.exists(c => ! Character.isDigit(c))) (Symbol(chars.mkString), rest)
        else (((if(negate) "-" else "") + (chars.mkString)).toInt, rest)
      }
      def readStringLit(data: List[Char], acc: String): (String, List[Char]) = data match {
        case '"' :: tail => (acc, tail)
        case c   :: tail => readStringLit(tail, acc + c)
        case List()      => die("unclosed string literal")
      }
      def readCharLit(data: List[Char]): (Char, List[Char]) = data match {
        case c :: '\'' :: tail => (c, tail)
        case _  => die("unclosed character literal")
      }
      data match {
        case '('  ::  tail => readList(data=tail, acc=Nil, terminator=')')
        case '['  ::  tail => readList(data=tail, acc=Nil, terminator=']')
        case ' '  ::  tail => readWithRest(tail)
        case '\n' ::  tail => readWithRest(tail)
        case '"'  ::  tail => readStringLit(tail, "")
        case '\'' ::  tail => readCharLit(tail)
        case ')'  ::  _    => die("unexpected list terminator")
        case ']'  ::  _    => die("unexpected list terminator")
        case c    ::  tail if(Character.isDigit(c)) => readNumOrMaybeSymbol(data, negate=false)
        case '-'  :: c :: tail if(Character.isDigit(c)) => readNumOrMaybeSymbol(c :: tail, negate=true)
        case _ => readSymbol(data)
      }
    }
  
    def printSExp(a:Any): String = a match {
      case s:Symbol    => s.toString.drop(1)
      case s:String    => s
      case i:Int       => i.toString
      case l:List[Any] => l.map(printSExp).mkString("(", " ", ")")
    }
  }
  
  object AST extends AST
  import AST._

  trait AST {
    case class Program(defs:List[Def], body:Expr)
  
    sealed trait Def{ val name: Symbol }
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

  object Parser extends Parser
  import Parser._
  
  trait Parser {
    def parse(code:String): Program = parseProgram(read(code))
  
    def parseProgram(a:Any): Program = a match {
      case Nil         => die(s"bad program: $a")
      case List(x)     => Program(Nil,parseExpr(x))
      case l:List[Any] => Program(l.init map parseDef, parseExpr(l.last))
      case _           => die(s"bad program: $a")
    }

    def parseDefs(a:Any): List[Def] = a match {
      case Nil         => die(s"bad defs: $a")
      // first, try to parse the code as just a list of defs
      // if that fails, try to parse it as a whole program
      case l:List[Any] => try l map parseDef catch { case e:Exception => parseProgram(a).defs }
      case _           => die(s"bad defs: $a")
    }

    def parseDef(a:Any): Def = {
      def parseName(name:Any): Symbol = name match {
        case s:Symbol => s // TODO: check s against builtin things like X,Y,Z,etc
        case _ => die(s"bad def name: $a")
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
          case _ => die(s"bad lambda arg: $a")
        }
        a match {
          case Nil         => Nil
          case l:List[Any] => l.map(parseLamArg)
          case _           => die(s"bad lambda arg list: $a")
        }
      }
      Lambda(parseLamArgList(args), parseExpr(body), recursive)
    }
  
    def parseLet(arg:Any, expr:Any, body:Any) = arg match {
      case s:Symbol => Let(s, parseExpr(expr), parseExpr(body))
      case _        => die(s"bad let argument: $arg")
    }
  
    def parseExpr(a:Any): Expr = a match {
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
        case _                       => die(s"bad let* arguments: $args")
      }
      case List('letrec, List(arg, List('lam, args, expr)), body) => arg match {
        case s:Symbol => LetRec(s, parseLambda(args, expr, Some(s)), parseExpr(body))
        case _        => die(s"bad letrec argument: $a")
      }
      case 'begin :: body          => Sequential(body map parseExpr)
      // finally, function application
      case f :: args               => {
        parseExpr(f) match {
          case Variable(Symbol(s)) if s.startsWith(".") => args match {
            case a :: as => InstanceMethodCall(parseExpr(a), s.drop(1), as map parseExpr)
            case _ => die("reflective call with no object!")
          }
          // turn static references into static function calls here.
          case StaticReference(clazz, func) => StaticMethodCall(clazz, func, args map parseExpr)
          case func =>  App(func, args map parseExpr)
        }
      }
      case _                       => die(s"bad expression: $a")
    }
  }
  
  object Interpreter extends Interpreter
  import Interpreter._

  trait Interpreter {
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
  
    def call(c:Closure, args:List[Any], env:Env): Value =
      evalred(App(EvaledExpr(c), args.map(a => EvaledExpr(ObjectValue(a)))), env)

    def evalred   (e:Expr, env:Env): Value = reduce(eval(e, env))
    def evalredval(e:Expr, env:Env): Any = unbox(evalred(e, env))
    def unbox(v:Value): Any = reduce(v) match {
      case c@Closure(l, env) => l.args match {
        case Nil             => () => ()
        case a1 :: Nil       => (a2: Any) => unbox(call(c, List(a2), env))
        case a1 :: b1 :: Nil => (a2: Any, b2: Any) => unbox(call(c, List(a2,b2), env))
        case a1 :: b1 :: c1 :: Nil =>
          (a2: Any, b2: Any, c2: Any) => unbox(call(c, List(a2,b2,c2), env))
      }
      case ObjectValue(a)              => a
      case DynamicValue(getter)        => die("shouldnt be possible, we already reduced")
      case BuiltinFunction(name, eval) => die(s"implement unbox for: $name")
    }
  
    def eval(e:Expr, env:Env): Value = e match {
      case Sequential(exps)   => exps.map(eval(_, env)).last
      case Bool(b)            => ObjectValue(b)
      case Num(i)             => ObjectValue(i)
      case StringExpr(s)      => ObjectValue(s)
      case EvaledExpr(v)      => v
      case Variable(s)        => env.get(s).getOrElse(die(s"not found: $s in: ${env.keys}"))
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
          case blah => die(s"app expected a function, but got: $blah")
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
          die(s"could not find constructor on class $c with args $evaledArgs")
        )(con =>
        // then call the constructor with the value (.value) of each of the args
          ObjectValue(con.newInstance(evaledArgs.map(_.asInstanceOf[AnyRef]):_*).asInstanceOf[AnyRef])
        )
      // TODO: better error handling in almost all cases
      case InstanceMethodCall(ob, func, args) =>
        val o = evalToObject(ob, env)
        invoke(o.getClass, func, o, args map (evalredval(_, env)))
      case StaticMethodCall(className, func, args) =>
        invoke(Class.forName(className), func, null, args map (evalredval(_, env)))
      case StaticReference(className, field) =>
        ObjectValue(Class.forName(className).getField(field).get(null))
    }

    def evalTo[T](e:Expr, env:Env, argType:String)(f: PartialFunction[Value, T]): T = {
      val v = evalred(e,env)
      if(f isDefinedAt v) f(v) else die(s"not a valid $argType: $v")
    }
    def allNumbers(as: List[Any]) = as.forall(x =>
      x.isInstanceOf[Int]    ||
      x.isInstanceOf[Double]
    )
    def toInt(a:Any): Int = a match {
      case i:Int    => i
      case d:Double => d.toInt
      case  _       => die(s"not a number: $a")
    }
    def evalToInt   (e:Expr, env:Env): Int    =
      evalTo(e,env,"Int")   {case ObjectValue(v:Int)    => v}
    def evalToString(e:Expr, env:Env): String =
      evalTo(e,env,"String"){case ObjectValue(v:String) => v}
    def evalToObject(e:Expr, env:Env): Any    =
      evalTo(e,env,"Object"){case ObjectValue(o)        => o}
  
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
        die(s"could not find method $c.$methodName (on object $invokedOn) with args ${
          args.map(_.getClass).mkString(",")
        }")
      )(method => {
        val finalArgs = args.map(_.asInstanceOf[AnyRef])
        val res = method.invoke(invokedOn, finalArgs:_*)
        ObjectValue(res)
      })
    }
    // TODO: repeat this for all AnyVal types.
    def matchesAll(cs:Seq[Class[_]], as:Seq[Any]) = {
      def isInt (a:Any) = a.isInstanceOf[Int]  || a.isInstanceOf[Integer]
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

  val stdLibDir = new File("./src/main/resources/minelang")
  val resourcesStdLibDir = new File("./minelang")

  object LibLoader {
    def loadLib(file:String): List[Def] = {
      // try to load from the file system
      val fromFileSystem = Option(new File(stdLibDir, file)).filter(_.exists)
      // if that fails, try to load from the jar
      val url = Option(getClass.getClassLoader.getResource(resourcesStdLibDir + "/" + file))
      val fromJar = url.map(u => new File(u.toURI))
      val theFile = fromFileSystem match {
        case Some(f)   => println(s"loading: $f from disk"); f
        case None      => fromJar match {
          case Some(f) => println(s"loading: $f from resources"); f
          case None    => die(s"couldn't find library: $file")
        }
      }
      val defs =
        try parseDefs(read(theFile))
        catch{ case e: Exception => die(s"failed parsing library: $file", e) }
      println(defs map (_.name))
      defs
    }
    def evalLib(file:String, env:Env): Env = loadLib(file).foldLeft(env)(evalDef)
  }
  import LibLoader._

  object Lib extends Lib

  trait Lib {

    val NilValue   = ObjectValue(())
    val TrueValue  = ObjectValue(true)
    val FalseValue = ObjectValue(false)
  
    // TODO on all these builtins, check the number of arguments.
    val isa = builtIn(Symbol("isa?"), (exps, env) => (evalredval(exps(0), env), exps(1)) match {
      case (NilValue, Variable(Symbol("scala.collection.immutable.List"))) =>
        ObjectValue(true)
      case (o, Variable(Symbol(className)))  =>
        ObjectValue(Class.forName(className).isAssignableFrom(o.getClass))
      case (o, c) =>
        die(s"invalid class name: $c")
    })
    val ifStat = builtIn('if, (exps, env) => evalred(exps(0), env) match {
      case ObjectValue(true)  => eval(exps(1), env)
      case ObjectValue(false) => eval(exps(2), env)
      case ev                 => die(s"bad if predicate: $ev")
    })
    // TODO: this could be done with a macro
    val unless = builtIn('unless, (exps, env) => evalred(exps(0), env) match {
      case ObjectValue(true)  => NilValue
      case ObjectValue(false) => eval(exps(1), env)
      case ev                 => die(s"bad unless predicate: $ev")
    })
    val eqBuiltIn = builtIn(Symbol("eq?"), (exps, env) =>
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
        case (i,w,f) => die(s"spawn expected <int> <int> <function>, but got: $i, $w $f")
      }
    )
    val add = builtIn('+, (exps, env) => {
      val vals = exps.map(e => evalredval(e, env))
      if (allNumbers(vals))  // all numbers
        ObjectValue(vals.map(toInt).foldLeft(0){(acc,i) => acc + i})
      else if (vals.forall(_.isInstanceOf[String])) // all strings
        ObjectValue(vals.foldLeft(""){(acc,s) => acc + s })
      else die(s"+ expected all numbers or all strings, but got $vals")
    })
    val abs = builtIn('abs, (exps, env) => ObjectValue(Math.abs(evalToInt(exps(0), env))))
    def twoNumOp(name:Symbol)(f: (Int,Int) => Value) = builtIn(name, (exps, env) =>
      (evalred(exps(0), env), evalred(exps(1), env)) match {
        case (ObjectValue(av:Int), ObjectValue(bv:Int)) => f(av, bv)
        case (av,bv) => die(s"${name.toString drop 1} expected two numbers, but got: $av, $bv")
      }
    )
    // todo: these need to deal with different numberic data types
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

    private val builtinLib: Env = Map(
      // primitives
      'true  -> ObjectValue(true),
      'false -> ObjectValue(false),
      'nil   -> ObjectValue(()),
      // simple builtins
      eqBuiltIn, isa, ifStat, unless, toStringPrim, printOnSameLine, printLine,
      add, sub, mult, mod, lt, lteq, gt, gteq, abs,
      'random -> DynamicValue(() => ObjectValue(math.random)),
      spawn
    )

    def lib = (loadLib("bool.mc") ::: loadLib("list.mc")).foldLeft(builtinLib)(evalDef)
  }

  object Session {
    def withStdLib(more:Env = Map()): Session = new Session(Lib.lib ++ more)
    def reloadStdLib(s:Session): Session = new Session(s.env ++ Lib.lib)
  }

  class Session (val env:Env = Map()){
    var count              = 0
    def join(moreEnv:Env)   : Session = new Session(env ++ moreEnv)
    def load(defs:List[Def]): Session = new Session(defs.foldLeft(env)(evalDef))
    def runExpr(code:String): ((String, Any), Session) = {
      def nextName: Symbol = {
        val n = Symbol(s"res$count")
        if(! env.contains(n)) n else { count = count + 1; nextName }
      }
      val (name, res) = (nextName, runProgram(parse(s"($code)")))
      (name.toString.drop(1) -> unbox(res), new Session(env + (name -> res)))
    }
    def runProgram(prog:Program): Value = evalProg(prog, env)
  }

  class Repl(val inputSession:Session = Session.withStdLib()) {
    import scala.tools.jline.console.ConsoleReader
    import scala.tools.jline.console.history.FileHistory
    import scala.tools.jline.console.completer._
    import scala.collection.JavaConversions._

    trait ReplCommand
      case object LastError extends ReplCommand
      case object Reload extends ReplCommand
      case object Load extends ReplCommand

    private val replLib: Env = Map(
      Symbol(":last-error") -> ObjectValue(LastError),
      Symbol(":reload")     -> ObjectValue(Reload),
      builtIn(Symbol(":load"), (exps, env) => {
        val f = evalToString(exps(0),env)
        ObjectValue(List(Load, f, evalLib(f, session.env)))
      })
    )

    var session = inputSession.join(replLib)

    val input = new ConsoleReader(){
      setBellEnabled(false)
      val history = {
        val historyFile = new File(s"${System.getProperty("user.home")}/.mc-history")
        historyFile.createNewFile
        new FileHistory(historyFile)
      }
      setHistory(history)
      setHistoryEnabled(true)
      //.{ArgumentCompletor, Completor, ConsoleReader, MultiCompletor, NullCompletor, SimpleCompletor}
      addCompleter(new StringsCompleter(replLib.keys.map(_.name)))
      addCompleter(new FileNameCompleter(){ override def getUserDir = stdLibDir })
      def saveHistory: Unit = history.flush
      def next = readLine("mc> ")
    }

    var lastException: Option[Exception] = None
    def run: Unit = {
      var next = input.next
      while (next != null) {
        try {
          val ((name,result), newSession) = session runExpr next
          result match {
            case LastError => lastException.foreach(_.printStackTrace())
            case Reload    => session = Session.reloadStdLib(session)
            case List(Load, file, newEnv:Env) =>
              session = session.join(newEnv)
              println(s"loaded $file")
            case _         =>
              println(s"$name: $result")
              session = newSession
          }
        } catch { case e: Exception =>
          println(s"error: ${e.getMessage}")
          lastException = Some(e)
        }
        input.saveHistory
        next = input.next
      }
    }
  }

  private def die(message:String) = sys error message
  private def die(message:String, e:Exception) = throw new RuntimeException(message, e)
}