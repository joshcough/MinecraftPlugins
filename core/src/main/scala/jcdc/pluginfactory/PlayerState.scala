package jcdc.pluginfactory

import org.bukkit.entity.Player

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

  private lazy val state = default.fold(
    collection.mutable.Map[Player, T]())(t =>
    collection.mutable.Map[Player, T]().withDefaultValue(t))

  /**
   * Get the state for the player
   * Unsafe, unless you specified a default
   */
  def getPlayerState     (p: Player): T          = state(p)

  /**
   * Get the state for the player, if it exists.
   */
  def getPlayerStateMaybe(p: Player): Option[T]  = state.get(p)

  /**
   * Set the state for the given player
   */
  def setPlayerState     (p: Player, t: T): Unit = state += (p -> t)

  /**
   * Delete the state for the given player,
   * and get back the state that was deleted.
   */
  def deletePlayerState  (p: Player): T = {
    val t = getPlayerState(p)
    state -= p
    t
  }

  /**
   * Delete the state for the given player,
   * and get back the state that was deleted, if it was there.
   */
  def deletePlayerStateMaybe(p: Player): Option[T] = {
    val t = getPlayerStateMaybe(p)
    state -= p
    t
  }
}

