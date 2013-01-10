package jcdc.pluginfactory

import org.bukkit.{Location, Material}
import org.bukkit.entity.Player
import ClojureInScala._
  import AST._
  import Parser._
  import Interpreter._
import org.bukkit.block.Block

object MineLangRepl {
  def main(args:Array[String]): Unit = new Repl(MineLang.mineLangSession(TestServer.player)).run
}

object MineLang {

  def mineLangSession(p:Player) = Session.withStdLib(new WorldEditExtension(p).lib)
  def run       (code:String,  p:Player): Any = runProgram(parse(code), p)
  def runExpr   (code:String,  p:Player): Any = runProgram(parse(s"($code)"), p)
  def runProgram(prog:Program, p:Player): Any = unbox(mineLangSession(p).runProgram(prog))

  case class WorldEditExtension(p:Player) extends BukkitEnrichment {
    def evalToLocation(e:Expr, env:Env): Location =
      evalTo(e,env,"location"){ case ObjectValue(l:Location) => l }
    def evalToMaterial(e:Expr, env:Env): Material =
      evalTo(e,env,"material"){
        case ObjectValue(m:Material) => m
        case ObjectValue(s:String) => MinecraftParsers.material(s).fold(sys error _)((m, _) => m)
      }
    def evalToCube(e:Expr, env:Env): Cube[Block] =
      evalTo(e,env,"cube"){ case ObjectValue(c:Cube[Block]) => c }

    val getMaterial = builtIn('material, (exps, env) => {
      eval(exps(0),env) match {
        case ObjectValue(s:String) => ObjectValue(
          MinecraftParsers.material(s).fold(sys error _)((m, _) => m)
        )
        case ObjectValue(i:Int) => ObjectValue(
          MinecraftParsers.material(i.toString).fold(sys error _)((m, _) => m)
        )
        case ev                    => sys error s"not a material: $ev"
      }
    })
    val goto = builtInNil('goto, (exps, env) => {
      val loc = evalToLocation(exps(0),env)
      p ! s"teleported to: ${loc.xyz}"; p.teleport(loc)
    })
    val loc = builtIn('loc, (exps, env) => {
      val (xe,ye,ze) = (evalAndUnbox(exps(0),env),evalAndUnbox(exps(1),env),evalAndUnbox(exps(2),env))
      if (allNumbers(List(xe,ye,ze)))
        ObjectValue(new Location(p.world,toInt(xe),toInt(ye),toInt(ze)))
      else sys error s"bad location data: ${(xe,ye,ze)}"
    })
    // here are all the cube block mutation functions.
    def builtInCube(name:Symbol, eval: (List[Expr], Env) => Cube[Block]) =
      (name -> BuiltinFunction(name, (es, env) => { ObjectValue(eval(es,env)) }))
    val setAll = builtInCube(Symbol("cube:set-all"), (exps, env) => {
      val c = evalToCube(exps(0), env)
      val m = evalToMaterial(exps(1), env)
      for(b <- c.toStream) b changeTo m
      p ! s"setting all in $c to $m"
      c
    })
    val changeSome = builtInCube(Symbol("cube:change"), ((exps, env) => {
      val c    = evalToCube(exps(0), env)
      val oldM = evalToMaterial(exps(1),env)
      val newM = evalToMaterial(exps(2),env)
      for(b <- c.toStream; if(b is oldM)) b changeTo newM
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
      c.floor.toStream.foreach(_ changeTo m)
      p ! s"set floor in $c to: $m"
      c
    }))
    val message = builtInNil('message, (exps, env) =>
      p ! (exps.map(e => evalAndUnbox(e, env).toString).mkString("\n"))
    )

    val lib: Env = Map(
      loc, goto,
      'MAXY   -> ObjectValue(255),
      'MINY   -> ObjectValue(0),
      builtInNoArg('X,      ObjectValue(p.x)),
      builtInNoArg('Y,      ObjectValue(p.blockOn.y)),
      builtInNoArg('Z,      ObjectValue(p.z)),
      builtInNoArg('XYZ,    ObjectValue(p.blockOn.loc)),
      builtInNoArg('origin, ObjectValue(p.world.getHighestBlockAt(0,0))),
      // material functions
      getMaterial,
      // mutable world edit functions
      setAll, changeSome, setWalls, setFloor,
      // send a message to the player
      message
    )
  }
}
