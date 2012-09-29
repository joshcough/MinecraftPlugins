package jcdc.pluginfactory

import scalaz.EphemeralStream

object Fibs {
  def log(i:Int): Int = { println("i: " + i); i }

  def fibs: Stream[Int] = {
    lazy val fibs: Stream[Int] =
      log(0) #:: log(1) #:: fibs.zip(fibs.tail).map{case (i,j) => log(i+j)}
    fibs
  }
  def nonMemoizingFibs: EphemeralStream[Int] = EphemeralStream.fromStream(fibs)

  def go {
    val x = nonMemoizingFibs
    x.take(10).map(println)
    x.take(10).map(println)
  }

}
