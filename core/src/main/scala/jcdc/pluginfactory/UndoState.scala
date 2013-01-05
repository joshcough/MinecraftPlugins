package jcdc.pluginfactory

import collection.immutable.Stack
import ScalaEnrichment.RichBoolean

case class UndoState[T](undoStack: Stack[T] = Stack(), redoStack: Stack[T] = Stack()){

  override def toString =
    s"UndoState(undoStack=(${undoStack.mkString(",")}) redoState=(${redoStack.toList}))"

  /**
   * if you make a change, it goes on top of the undo stack
   * and the redo stack is cleared.
   */
  def newChange(t: T): UndoState[T] = copy(undoStack = undoStack.push(t), redoStack = Stack[T]())

  /**
   * undo: take off top of undo stack and put onto redo stack
   */
  def undo(f: T => T): (Option[(T, UndoState[T])]) = undoStack.nonEmpty.toOption(
    (undoStack.top, copy(undoStack.pop, redoStack.push(f(undoStack.top))))
  )

  /**
   * redo: take off top of redo stack and put onto undo stack
   */
  def redo(f: T => T): (Option[(T, UndoState[T])]) = redoStack.nonEmpty.toOption(
    (redoStack.top, copy(undoStack.push(f(redoStack.top)), redoStack.pop))
  )
}
