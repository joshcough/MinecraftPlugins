package jcdc.pluginfactory

import org.bukkit.ChatColor._
import org.bukkit.GameMode._
import org.bukkit.command.{CommandSender, Command => BukkitCommand}
import org.bukkit.entity.{EntityType, Player}

trait CommandsPlugin extends ScalaPlugin with ArgParsers {

  type Description = String
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
    ch.argDesc + " [Op Only]",
    (player: Player, c: BukkitCommand, args: List[String]) =>
      if (player.isOp) ch.f(player, c, args)
      else player.sendMessage(RED + "You must be an op to run /" + c.getName)
  )

  type PlayerToPlayer = (Player, Player) => Unit
  def p2p(p2pc: PlayerToPlayer): CommandBody = args(player) { case p1 ~ p2 => p2pc(p1, p2) }

  def noArgs(f: Player => Unit): CommandBody =
    CommandBody("", (p: Player, c: BukkitCommand, args: List[String]) => f(p))

  def args[T](argsParser: ArgParser[T])(f: ~[Player, T] => Unit): CommandBody =
    CommandBody(
      argsParser.describe,
      (p: Player, c: BukkitCommand, args: List[String]) => argsParser(p, args) match {
        case Failure(msg) => p ! (RED + " " + msg)
        case Success(t, _) => f(new ~(p, t))
      })

  override def yaml = {
    def commandYaml(c: Command) = "  " +
      c.name + ":\n" +
      "    description: " + c.description.getOrElse(c.name) + "\n" +
      "    usage: /<command> " + c.body.argDesc
    val commandsYaml = "commands:\n" + commands.map(commandYaml).mkString("\n")
    List(super.yaml, commandsYaml).mkString("\n")
  }
}

object ArgParsers extends ArgParsers

trait ArgParsers {
  import Pimps._

  case class ~[+A, +B](a: A, b: B) {
    override def toString = "(" + a + "~" + b + ")"
  }

  trait ParseResult[T]
  case class Failure[T](message: String) extends ParseResult[T]
  case class Success[T](t: T, rest: List[String]) extends ParseResult[T]

  trait ArgParser[T] { self =>
    def apply(p: Player, args: List[String]): ParseResult[T]
    def describe: String

    def named(name:String) = new ArgParser[T] {
      def apply(p: Player, args: List[String]) = self(p, args)
      def describe: String = name
    }

    def ^^[U](f: T => U) = new ArgParser[U] {
      def apply(p: Player, args: List[String]): ParseResult[U] = self(p, args) match {
        case Failure(m) => Failure(m)
        case Success(t, rest) => Success(f(t), rest)
      }
      def describe = self.describe
    }

    def ~[T, U](p2: => ArgParser[U]) = new ArgParser[~[T, U]] {
      def apply(p: Player, args: List[String]) = self(p, args) match {
        case Failure(m) => Failure(m)
        case Success(t: T, rest) => p2(p, rest) match {
          case Failure(m) => Failure(m)
          case Success(u, rest) => Success(new ~(t, u), rest)
        }
      }
      def describe = self.describe + "  " + p2.describe
    }

    def |(p2: => ArgParser[T]) = new ArgParser[T] {
      def apply(p: Player, args: List[String]): ParseResult[T] = self(p, args) match {
        case Success(t, rest) => Success(t, rest)
        case Failure(m1) => p2(p, args) match {
          case Success(t, rest) => Success(t, rest)
          case Failure(m2) => Failure(m1 + " or " + m2)
        }
      }
      def describe = "(" + self.describe + " or " + p2.describe + ")"
    }

    def ||[U](p2: => ArgParser[U]) = new ArgParser[Either[T, U]] {
      def apply(p: Player, args: List[String]): ParseResult[Either[T, U]] = self(p, args) match {
        case Success(t, rest) => Success(Left(t), rest)
        case Failure(m1) => p2(p, args) match {
          case Success(u, rest) => Success(Right(u), rest)
          case Failure(m2) => Failure(m1 + " or " + m2)
        }
      }
      def describe = "(" + self.describe + " or " + p2.describe + ")"
    }

    def * : ArgParser[List[T]] = {
      val x: ArgParser[~[T, List[T]]] = (this ~ (this *))
      (x ^^ {case t ~ ts => t :: ts}) | success(List[T]()).named(self.describe + "*")
    }

    def + : ArgParser[List[T]] = {
      val x: ArgParser[~[T, List[T]]] = (this ~ (this *))
      (x ^^ {case t ~ ts => t :: ts}).named(self.describe + "+")
    }
  }

  def opt[T](parser: ArgParser[T]) = new ArgParser[Option[T]] {
    def apply(p: Player, args: List[String]): ParseResult[Option[T]] = parser(p, args) match {
      case Failure(m) => Success(None: Option[T], args)
      case Success(t, rest) => Success(Some(t), rest)
    }
    def describe = "optional(" + parser.describe + ")"
  }

  def success[T](t: T) = new ArgParser[T] {
    def apply(p: Player, args: List[String]) = Success(t, args)
    def describe = t.toString
  }

  implicit def stringToParser(s: String) = new ArgParser[String] {
    def apply(p: Player, args: List[String]) = args match {
      case Nil => Failure("expected :" + s + ", but got nothing")
      case x :: xs => if (x == s) Success(x, xs) else Failure("expected: " + s + ", but got: " + x)
    }
    def describe = s
  }

  val gamemode =
    ("c" | "creative" | "1") ^^ {_ => CREATIVE} | ("s" | "survival" | "0") ^^ {_ => SURVIVAL}

  def num = token("int") { (_, s) => try Some(s.toInt) catch { case e => None }}
  def anyString = token("string") { (_, s) => Some(s) }
  def player = token("player-name") { (p, s) => p.server.findPlayer(s) }
  def material = token("material-type") { (_, s) => findMaterial(s) }

  def entity = token("entity-type") { (_, s) =>
    Option(EntityType.fromName(s.toUpperCase)).orElse(
      try Option(EntityType.fromId(s.toInt)) catch { case e => None }
    )
  }

  def token[T](name: String)(f: (Player, String) => Option[T]) = new ArgParser[T] {
    def apply(p: Player, args: List[String]) = args match {
      case Nil => Failure("expected " + name + ", got nothing")
      case x :: xs => f(p, x) match {
        case None => Failure("no such: " + name)
        case Some(t) => Success(t, xs)
      }
    }
    def describe = name
  }
}