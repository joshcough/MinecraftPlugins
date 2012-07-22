package jcdc.pluginfactory

import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, Command => BukkitCommand}
import org.bukkit.GameMode._
import org.bukkit.entity.{EntityType, Player}
import Pimps._

trait MinecraftParsers extends ParserCombinators[Player] {
  val gamemode =
    ("c" | "creative" | "1") ^^ {_ => CREATIVE} | ("s" | "survival" | "0") ^^ {_ => SURVIVAL}
  def player = token("player-name") { (p, s) => p.server.findPlayer(s) }
  def material = token("material-type") { (_, s) => findMaterial(s) }
  def entity = token("entity-type") { (_, s) =>
    Option(EntityType.fromName(s.toUpperCase)).orElse(
      try Option(EntityType.fromId(s.toInt)) catch { case e: Exception => None }
    )
  }
}

trait CommandsPlugin extends ScalaPlugin with MinecraftParsers {

  case class CommandBody(argDesc: String, f:(Player, BukkitCommand, List[String]) => Unit)

  object Command {
    def apply(name: String, desc: String, body: CommandBody) = new Command(name, Some(desc), body)
    def apply(name: String, body: CommandBody) = new Command(name, None, body)
  }

  case class Command(name: String, description: Option[String], body: CommandBody)
  val commands: List[Command]

  private def lowers = commands.map(c => (c.name.toLowerCase, c)).toMap

  override def onEnable() {
    super.onEnable()
    lowers.foreach { case (name, _) => logInfo("command: " + name) }
  }

  override def onCommand(sender: CommandSender, cmd: BukkitCommand,
                         commandLabel: String, args: Array[String]) = {
    val p = sender.asInstanceOf[Player]
    for (ch <- lowers.get(cmd.getName.toLowerCase))
      try ch.body.f(p, cmd, args.toList)
      catch { case e: Exception =>
        p ! e.getMessage
        p ! e.getStackTraceString
        e.printStackTrace
      }
    true
  }

  def opOnly(ch: CommandBody): CommandBody = CommandBody(
    s"${ch.argDesc} [Op Only]", (player: Player, c: BukkitCommand, args: List[String]) =>
      if (player.isOp) ch.f(player, c, args)
      else player.sendMessage(RED + s"You must be an op to run /${c.getName}")
  )

  type PlayerToPlayer = (Player, Player) => Unit
  def p2p(p2pc: PlayerToPlayer): CommandBody = args(player) { case p1 ~ p2 => p2pc(p1, p2) }

  def noArgs(f: Player => Unit): CommandBody =
    CommandBody("", (p: Player, c: BukkitCommand, args: List[String]) => f(p))

  def args[T](argsParser: Parser[T])(f: ~[Player, T] => Unit): CommandBody =
    CommandBody(
      argsParser.describe,
      (p: Player, c: BukkitCommand, args: List[String]) => argsParser(p, args) match {
        case Failure(msg) => p ! (RED + " " + msg)
        case Success(t, _) => f(new ~(p, t))
      })

  override def yml(author:String, version: String) = {
    def commandYml(c: Command) = "  " +
      c.name + ":\n" +
      "    description: " + c.description.getOrElse(c.name) + "\n" +
      "    usage: /<command> " + c.body.argDesc
    val commandsYml = "commands:\n" + commands.map(commandYml).mkString("\n")
    List(super.yml(author, version), commandsYml).mkString("\n")
  }
}
