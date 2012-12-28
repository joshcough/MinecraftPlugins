package jcdc.pluginfactory

import java.io.File
import io.Source

/**
 * Adds a bunch of missing functions to Scala classes.
 */
trait ScalaEnrichment {

  val unit = ()

  /**
   * Allows for F# style pipelining
   * x |> f is the same as f(x).
   * TODO: double check that x |> f |> g is the same as g(f(x)).
   */
  implicit class RichT[T](t:T){
    def |> [U](f: T => U) = f(t)
  }

  /**
   * Enrich a Function1.
   */
  implicit class RichFunction1[A,B,R](f: A => R) {
    def of(a:A): R = f(a)
  }

  /**
   * Enrich a Function1 that returns a Function1.
   */
  implicit class RichFunction1Function1[A,B,R](f: A => B => R) {
    /**
     * Turn A -> B -> R into B -> A -> R
     */
    def flip: B => A => R = b => a => f(a)(b)
  }

  implicit class RichFunction2[A,B,R](f: (A, B) => R) {
    /**
     * Turn (A -> B) -> R into (B -> A) -> R
     */
    def flip: (B, A) => R = (b, a) => f(a, b)
  }

  implicit class RichOption[T](o: Option[T]){
    /**
     * Fold with the argument order reversed, because sometimes its nice to put the
     * success case first, and the failure case last.
     */
    def flipFold[B]: (T => B) => B => B = ((b: B) => (f: T => B) => o.fold(b)(f)).flip
  }

  // alias for identity.
  def id[T](t:T) = identity(t)

  implicit class RichBoolean(b1:Boolean) {
    def or (b2: => Boolean) = b1 || b2
    def and(b2: => Boolean) = b1 && b2
  }

  implicit class RichInt(i: Int){
    def isEven = i % 2 == 0
    def isOdd  = ! isEven
  }

  implicit class RichFile(f:File){
    def child(name:String): File = new File(f, name)
    def slurp: String            = Source.fromFile(f).getLines().mkString("\n")
  }
}
