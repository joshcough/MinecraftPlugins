package com.joshcough.minecraft

import collection.immutable.Stack
import ScalaEnrichment.RichBoolean

case class UndoState[T, U](undoStack: Stack[T] = Stack(), redoStack: Stack[U] = Stack()){

  override def toString =
    s"UndoState(undoStack=(${undoStack.mkString(",")}) redoState=(${redoStack.toList}))"

  /**
   * if you make a change, it goes on top of the undo stack
   * and the redo stack is cleared.
   */
  def newChange(t: T): UndoState[T, U] = copy(undoStack = undoStack.push(t), redoStack = Stack[U]())

  /**
   * undo: take off top of undo stack and put onto redo stack
   */
  def undo(f: T => U): Option[UndoState[T, U]] = undoStack.nonEmpty.toOption(
    copy(undoStack.pop, redoStack.push(f(undoStack.top)))
  )

  /**
   * redo: take off top of redo stack and put onto undo stack
   */
  def redo(f: U => T): Option[UndoState[T, U]] = redoStack.nonEmpty.toOption(
    copy(undoStack.push(f(redoStack.top)), redoStack.pop)
  )
}
