package com.joshcough.minecraft

import org.bukkit.entity.Player
import collection.mutable.Map

/**
 * Wrapper functions around a collection.mutable.Map[Player, T]
 * @tparam T the type of the state stored for the player
 */
trait PlayerState[T] {
  /**
   * The default value that gets stored in the Map
   * If None, the Map will have no default value.
   */
  val default: Option[T] = None

  lazy val state = default.fold(Map[Player, T]())(t => Map[Player, T]().withDefaultValue(t))

  /**
   * Get the state for the player
   * Unsafe, unless you specified a default
   * @param p
   * @return
   */
  def apply(p: Player): T = state(p)

  /**
   * Get the state for the player, if it exists.
   */
  def get(p: Player): Option[T] = state.get(p)

  /**
   * Set the state for the given player
   * @param pt
   * @return
   */
  def += (pt: (Player, T)): T = { state += (pt._1 -> pt._2); pt._2 }

  /**
   * Delete the state for the given player,
   * and get back the state that was deleted.
   * @param p
   * @return
   */
  def -= (p: Player): T = { val t = this(p); state -= p; t }

  /**
   * Delete the state for the given player,
   * and get back the state that was deleted, if it was there.
   */
  def deletePlayerStateMaybe(p: Player): Option[T] = { val t = get(p); state -= p; t }
}
