package com.joshcough.minecraft

import ScalaEnrichment.RichBoolean

case class UndoState[T, U](undoStack: List[T] = List(), redoStack: List[U] = List()){

  override def toString =
    s"UndoState(undoStack=(${undoStack.mkString(",")}) redoState=(${redoStack}))"

  /**
   * if you make a change, it goes on top of the undo stack
   * and the redo stack is cleared.
   */
  def newChange(t: T): UndoState[T, U] = copy(undoStack = t :: undoStack, redoStack = List[U]())

  /**
   * undo: take off top of undo stack and put onto redo stack
   */
  def undo(f: T => U): Option[UndoState[T, U]] = undoStack.nonEmpty.toOption(
    copy(undoStack.tail, f(undoStack.head) :: redoStack)
  )

  /**
   * redo: take off top of redo stack and put onto undo stack
   */
  def redo(f: U => T): Option[UndoState[T, U]] = redoStack.nonEmpty.toOption(
    copy(f(redoStack.head) :: undoStack, redoStack.tail)
  )
}
