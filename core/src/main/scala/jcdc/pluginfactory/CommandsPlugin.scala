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
      try Option(EntityType.fromId(s.toInt)) catch { case e => None }
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
    for (ch <- lowers.get(cmd.getName.toLowerCase))
      ch.body.f(sender.asInstanceOf[Player], cmd, args.toList)
    true
  }

  def opOnly(ch: CommandBody): CommandBody = CommandBody(
    ch.argDesc + " [Op Only]", (player: Player, c: BukkitCommand, args: List[String]) =>
      if (player.isOp) ch.f(player, c, args)
      else player.sendMessage(RED + "You must be an op to run /" + c.getName)
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

  override def yml = {
    def commandYaml(c: Command) = "  " +
      c.name + ":\n" +
      "    description: " + c.description.getOrElse(c.name) + "\n" +
      "    usage: /<command> " + c.body.argDesc
    val commandsYaml = "commands:\n" + commands.map(commandYaml).mkString("\n")
    List(super.yml, commandsYaml).mkString("\n")
  }
}
