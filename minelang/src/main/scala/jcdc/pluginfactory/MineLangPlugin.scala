package jcdc.pluginfactory

import jcdc.pluginfactory.ClojureInScala._
  import Reader._
  import AST._
  import Parser._
import jcdc.pluginfactory.MineLang._
import java.io.File

class MineLangPlugin extends CommandsPlugin {

  // todo: we should be able to load more than one file
  // and this directory might be wrong now.
  val houseDefs = new File("src/main/resources/minelang/house.mc")
  var defs: List[Def] = parseDefs(read(houseDefs))

  val commands = List(
    Command("import", "import some defs", existingFile){ case (_, codeFile) =>
      defs = defs ::: parseDefs(read(codeFile))
    },
    Command("run", "run a program", slurp){ case (p, code) =>
      runProgram(Program(defs, parseExpr(read(code))), p)
    },
    Command("reload-code", "run a program")(p => defs = parseDefs(read(houseDefs)))
  )
}