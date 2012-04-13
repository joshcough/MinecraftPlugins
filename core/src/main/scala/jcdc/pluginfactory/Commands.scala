package jcdc.pluginfactory

import org.bukkit.entity.Player
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandSender, Command}

case class CommandArguments(cmd:Command, args: List[String])

// Command combinators.
trait CommandsPlugin extends ScalaPlugin {

  val commands: Map[String, CommandHandler]
  private def lowers: Map[String, CommandHandler] = commands.map{ case (k,v) => (k.toLowerCase, v)}
  override def onEnable(){
    super.onEnable()
    lowers.keys.foreach{ k => logInfo("["+name+"] command: " + k) }
  }
  override def onCommand(sender:CommandSender, cmd:Command, commandLabel:String, args:Array[String]) = {
    for(ch <- lowers.get(cmd.getName.toLowerCase))
      ch(sender.asInstanceOf[Player], CommandArguments(cmd, args.toList))
    true
  }

  type CommandHandler = (Player, CommandArguments) => Unit
  type PlayerToPlayerCommand = (Player, Player, CommandArguments) => Unit

  def opOnly(ch:CommandHandler) = (player: Player, args:CommandArguments) =>
    if(player.isOp) ch(player, args) else player.sendMessage(RED + "You must be an op to run /" + args.cmd.getName)

  def minArgs(n:Int, ch:CommandHandler) = (player: Player, cmd: CommandArguments) =>
    if(cmd.args.length >= n) ch(player, cmd) else player.sendUsage(cmd.cmd)

  def command(ch:CommandHandler)       = minArgs(0, ch)
  def oneArg (ch:CommandHandler)       = minArgs(1, ch)
  def oneOrMoreArgs(ch:CommandHandler) = oneArg(ch)

  def p2p(p2pc:PlayerToPlayerCommand): CommandHandler = oneOrMoreArgs((sender, cmd) =>
    sender.findPlayer(cmd.args(0)) { receiver => p2pc(sender, receiver, cmd) })

  def p2pMany(p2pc:PlayerToPlayerCommand): CommandHandler = (sender: Player, args: CommandArguments) =>
    sender.findPlayers(args.args.toList) { receiver => p2pc(sender, receiver, args) }
}
