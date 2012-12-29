package jcdc.pluginfactory

import org.bukkit.entity.Player

trait PlayerState[T] {
  val users = collection.mutable.Map[Player, T]()
  def getPlayerState(p: Player)       = users.get(p)
  def setPlayerState(p: Player, t: T) = users += (p -> t)
  def deletePlayerState(p: Player)    = users -= p
}

