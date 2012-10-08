package jcdc.pluginfactory.examples

import org.scalacheck.Properties
import org.scalacheck.Prop._
import jcdc.pluginfactory.TestServer
import jcdc.pluginfactory.io.Reader

object WorldEditTest extends Properties("MinecraftParserTests") {

  val we = new WorldEdit
  import we._
  val p = TestServer.player

  val testScriptFull =
    """
     ((goto origin)
      (corners ((+ X 20) (+ Y 50) (+ Z 20)) ((- X 20) Y (- Z 20)))
      (floor stone)
      (walls brick)
     )
    """.stripMargin.trim

  property("origin")  = secure { parseLoc("origin") }
  property("XYZ")     = secure { parseLoc("XYZ") }
  property("(5 6 7)") = secure { parseLoc("(5 6 7)") }
  property("(5 Y 7)") = secure { parseLoc("(5 Y 7)") }
  property("((+ 5 10) Y 7)")        = secure { parseLoc("((+ 5 10) Y 7)") }
  property("((+ X 20) Y (+ Z 20))") = secure { parseLoc("((+ X 20) Y (+ Z 20))") }
  property("((- X 20) Y (- Z 20))") = secure { parseLoc("((- X 20) Y (- Z 20))") }
  property("((- 5 10) (+ Y 20) Z)") = secure { parseLoc("((- 5 10) (+ Y 20) Z)") }

  property("(goto origin)") = secure { run("((goto origin))") }
  property("(corners XYZ origin)") = secure { run("((corners XYZ origin))") }
  property("(corners XYZ (5 6 7))") = secure { run("((corners XYZ (5 6 7)))") }
  property("(corners ((+ X 20) Y (+ Z 20)) ((+ X 20) Y (+ Z 20)))") =
    secure { run("((corners ((+ X 20) Y (+ Z 20)) ((+ X 20) Y (+ Z 20))))") }
  property("(set stone)")   = secure { run("((set stone))") }
  property("(walls brick)") = secure { run("((walls brick))") }
  property("testScriptFull") = secure { run(testScriptFull) }

  def parseLoc(code:String): Boolean =
    attemptT(p, truthfully(println(p.parseLoc(Reader.read(code)))))
  def run(code:String): Boolean =
    attemptT(p, truthfully(println(p.parse(code))))

  def truthfully(f: => Unit) = {f; true}
}
