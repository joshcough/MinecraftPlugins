package com.joshcough.minecraft.ermine

import com.clarifi.reporting._
import ermine.session._

import com.clarifi.reporting.ermine.{Bottom, Prim, Runtime}
import com.clarifi.reporting.ermine.session.foreign.SortedMap.{FD => ErMap}
import com.clarifi.reporting.util.PimpedLogger._
import Session.{Filesystem, Resource, SourceFile}
import com.clarifi.reporting.util.IOUtils._

import scalaz._
import scalaz.std.indexedSeq.{toNel => _, _}
import scalaz.std.option._
import scalaz.std.string._
import scalaz.std.tuple._
import scalaz.std.list._
import scalaz.syntax.id._
import scalaz.syntax.show._
import scalaz.syntax.traverse._
import scalaz.syntax.std.function2._
import scalaz.syntax.std.option._
import scalaz.syntax.std.tuple._


// TODO: add initialEnv to ermine-legacy and remove all of this code.

/**
 * Evaluator for some Ermine code.
 * Runs with prelude already loaded, so loads are reasonably fast.
 * Note: inherited from in com.clarifi.reporting.bridge.BridgeErmineEvaluator
 */
trait ErmineEvaluator {
  protected def initialEnv: SessionEnv = new SessionEnv

  implicit val supply = Supply.create
  val printer = Printer.simple
  val loadPaths : List[String]
  // create a base session with prelude loaded so that we don't have to
  // repeat that work for every report that is loaded.
  val baseEnv: SessionEnv = {
    implicit val e : SessionEnv = initialEnv
    Lib.preamble
    Session.loadModules(preloads ++ List("Prelude"))(e, supply, printer)
    e.loadFile = SourceFile.inOrder(e.loadFile :: (loadPaths map (dir => SourceFile.filesystem(dir)(_))):_*)
    e
  }

  /** Extra modules to preload and share. */
  def preloads: List[String] = List.empty

  // evaluate some ermine code.
  def eval(module:String, expr:String): Exception \/ Runtime =
    try \/-(Session.evalInContext(module, expr, "<Whatever>")(baseEnv.copy, supply.split, printer))
    catch { case e: Exception => -\/(e) }
}

abstract class ReportsCache extends ErmineEvaluator {

  type Report

  private[ermine] val reports = collection.mutable.Map[Report, (Long, Runtime)]()

  protected def evalReport(r: Report): Exception \/ (Long, Need[Exception \/ Runtime])

  def showReport: Show[Report]

  /**
   * Searches for, loads, and caches a report.
   *
   * If the report is found, checks to see if the report is in cache
   *   if it is in cache, checks to see if the report has expired
   *     if it hasn't expired, simply returns it
   *     if it has, reloads it, recaches it, and then returns it
   *   if it isn't in cache, loads it, caches it, and returns it
   * If the report is not found, returns Left.
   */
  def getReport(r: Report): Exception \/ Runtime =
    evalReport(r) flatMap {case (timestamp, rt) => addReport(r, timestamp, rt.value) }

  // loads and caches a report
  private[this]
  def addReport(r: Report, timestamp: Long, fresh: => (Exception \/ Runtime)): Exception \/ Runtime =
    (reports get r filter (_._1 >= timestamp) map (v => \/-(v._2)) getOrElse
      (fresh map {scmhjs =>
        reports.put(r, (timestamp, scmhjs))
        scmhjs
      }))
}

object ReportsCache {
  /** An expression to invoke in the context of a module so-named.
    *
    * @tparam A How modules are identified.
    */
  final case class ModuleExpr[A](module: A, expression: String)

  object ModuleExpr {
    implicit def showModuleExpr[A: Show]: Show[ModuleExpr[A]] =
      Show.show{case ModuleExpr(m, e) => Cord(m.show, ":", e)}
  }
}

abstract class LoaderReportsCache[A](val loadFile: SourceFile.ALoader[A])
  extends ReportsCache {
  import ReportsCache.ModuleExpr

  protected def toME(r: Report): ModuleExpr[A]

  protected def evalReport(r: Report) = {
    val ModuleExpr(module, expr) = toME(r)
    (loadFile(module)
      \/> new Exception("unable to find module: " + module))
      .map(sf => (sf.lastModified getOrElse 0,
      Need(eval(sf.contents, expr))))
  }
}
