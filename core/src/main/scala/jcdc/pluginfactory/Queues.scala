package jcdc.pluginfactory

object Queues{
  def main(args:Array[String]){
    println("queues with traits: lock then log")
    QueuesWithTraitLinearization.lockThenLogQ.push(7)
    println(QueuesWithTraitLinearization.lockThenLogQ.pop())
    println("\n")
    println("queues with traits: log then lock")
    QueuesWithTraitLinearization.logThenLockQ.push(7)
    println(QueuesWithTraitLinearization.logThenLockQ.pop())
    println("\n")

    println("queues with functions: lock then log")
    QueuesWithFunctions.lockThenLogQ._1(7)
    println(QueuesWithFunctions.lockThenLogQ._2())
    println("\n")
    println("queues with functions: log then lock")
    QueuesWithFunctions.logThenLockQ._1(7)
    println(QueuesWithFunctions.logThenLockQ._2())
    println("\n")

    println("queues with functions and case class: lock then log")
    QueuesWithFunctionsAndCaseClass.lockThenLogQ.push(7)
    println(QueuesWithFunctionsAndCaseClass.lockThenLogQ.pop())
    println("\n")
    println("queues with functions and case class: log then lock")
    QueuesWithFunctionsAndCaseClass.logThenLockQ.push(7)
    println(QueuesWithFunctionsAndCaseClass.logThenLockQ.pop())
    println("\n")
  }
}

object QueuesWithTraitLinearization {
  trait Queue[T] {
    def push(t:T)
    def pop(): T
  }

  class SizeOneQueue[T] extends Queue[T] {
    var ot: Option[T] = None
    def push(t:T) = ot match {
      case None => ot = Some(t)
      case _ => sys.error("boom")
    }
    def pop(): T = ot match {
      case None => sys.error("boom")
      case Some(t) => ot = None; t
    }
  }

  trait LockingQueue[T] extends Queue[T]{
    abstract override def push(t:T) {
      this.synchronized{println("locked"); super.push(t)}
    }
    abstract override def pop(): T = this.synchronized{
      println("locked"); super.pop()
    }
  }

  trait LoggingQueue[T] extends Queue[T]{
    abstract override def push(t:T) {
      println("logging q: pushing: " + t); super.push(t)
    }
    abstract override def pop(): T = {
      println("logging q: popping"); super.pop()
    }
  }

  val lockThenLogQ = new SizeOneQueue[Int] with LoggingQueue[Int] with LockingQueue[Int]
  val logThenLockQ = new SizeOneQueue[Int] with LockingQueue[Int] with LoggingQueue[Int]
}

object QueuesWithFunctionsAndCaseClass {
  case class Queue[T](push: T => Unit, pop: () => T)

  def sizeOneQueue[T]: Queue[T] = {
    var ot: Option[T] = None
    Queue(
      push = (t:T) => ot match {
        case None => ot = Some(t)
        case _ => sys.error("boom")
      },
      pop = () => ot match {
        case None => sys.error("boom")
        case Some(t) => ot = None; t
      }
    )
  }

  def loggingQueue[T](q:Queue[T]) = Queue(
    push = (t:T) => { println("logging q: pushing: " + t); q.push(t) },
    pop = () => { println("logging q: popping"); q.pop() }
  )
  def lockingQueue[T](q:Queue[T]) = Queue(
    push = (t:T) => { println("locked"); q.synchronized(q.push(t)) },
    pop = () => { println("locked"); q.synchronized(q.pop()) }
  )
  
  val lockThenLogQ = lockingQueue(loggingQueue(sizeOneQueue[Int]))
  val logThenLockQ = loggingQueue(lockingQueue(sizeOneQueue[Int]))
}

object QueuesWithFunctions {
  type Push[T] = T => Unit
  type Pop[T] = () => T
  type Queue[T] = (Push[T], Pop[T])

  def sizeOneQueue[T]: Queue[T] = {
    var ot: Option[T] = None
    val push: Push[T] = (t:T) => ot match {
      case None => ot = Some(t) case _ => sys.error("boom")
    }
    val pop: Pop[T] = () => ot match {
      case None => sys.error("boom") case Some(t) => ot = None; t
    }
    (push, pop)
  }

  def loggingQueue[T](q:Queue[T]): Queue[T] = {
    val loggedPush = (t:T) => { println("logging q: pushing: " + t); q._1(t) }
    val loggedPop = () => { println("logging q: popping"); q._2() }
    (loggedPush, loggedPop)
  }

  def lockingQueue[T](q:Queue[T]): Queue[T] = {
    val lockedPush = (t:T) => { println("locked"); q.synchronized(q._1(t)) }
    val lockedPop = () => { println("locked"); q.synchronized(q._2()) }
    (lockedPush, lockedPop)
  }

  val lockThenLogQ = lockingQueue(loggingQueue(sizeOneQueue[Int]))
  val logThenLockQ = loggingQueue(lockingQueue(sizeOneQueue[Int]))
}
