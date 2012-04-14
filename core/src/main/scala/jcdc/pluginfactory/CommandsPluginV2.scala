package jcdc.pluginfactory

import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.command.{CommandSender, Command}
import org.bukkit.entity.{EntityType, Player}

trait CommandsPluginV2 extends ScalaPlugin {

  type CommandHandler = (Player, Command, List[String]) => Unit
  val commands: Map[String, CommandHandler]
  private def lowers: Map[String, CommandHandler] = commands.map{ case (k,v) => (k.toLowerCase, v)}
  override def onEnable(){
    super.onEnable()
    lowers.keys.foreach{ k => logInfo("["+name+"] command: " + k) }
  }
  override def onCommand(sender:CommandSender, cmd:Command, commandLabel:String, args:Array[String]) = {
    for(ch <- lowers.get(cmd.getName.toLowerCase))
      ch(sender.asInstanceOf[Player], cmd, args.toList)
    true
  }

  def opOnly(ch:CommandHandler) = (player: Player, c:Command, args:List[String]) =>
    if(player.isOp) ch(player, c, args)
    else player.sendMessage(RED + "You must be an op to run /" + c.getName)

  type PlayerToPlayer = (Player, Player) => Unit
  def p2p(p2pc:PlayerToPlayer): CommandHandler = args(player){ case p1 ~ p2 => p2pc(p1, p2) }
  def noArgs(f: Player => Unit) = (p:Player, c:Command, args:List[String]) => f(p)

  def args[T](argsParser: ArgParser[T])(f: ~[Player,T] => Unit): CommandHandler =
    (p:Player, c:Command, args:List[String]) => argsParser(p, args) match {
      case Failure(msg) => p ! (RED + " " + msg)
      case Success(t, _) => f(new ~(p,t))
    }

  case class ~[+A, +B](a: A, b: B) {
    override def toString = "("+ a + "~" + b +")"
  }

  trait ParseResult[T]
  case class Failure[T](message: String) extends ParseResult[T]
  case class Success[T](t:T, rest: List[String]) extends ParseResult[T]

  trait ArgParser[T]{ self =>
    def apply(p:Player, args: List[String]): ParseResult[T]

    def ^^[U](f: T => U) = new ArgParser[U] {
      def apply(p:Player, args: List[String]): ParseResult[U] = self(p, args) match {
        case Failure(m)       => Failure(m)
        case Success(t, rest) => Success(f(t), rest)
      }
    }

    def ~[T,U](p2: => ArgParser[U]) = new ArgParser[~[T,U]] {
      def apply(p:Player, args: List[String]) = self(p, args) match {
        case Failure(m)         => Failure(m)
        case Success(t:T, rest)   => p2(p, rest) match {
          case Failure(m)       => Failure(m)
          case Success(u, rest) => Success(new ~(t, u), rest)
        }
      }
    }

    def |(p2: => ArgParser[T]) = new ArgParser[T] {
      def apply(p:Player, args: List[String]): ParseResult[T] = self(p, args) match {
        case Success(t, rest)   => Success(t, rest)
        case Failure(m1)        => p2(p, args) match {
          case Success(t, rest) => Success(t, rest)
          case Failure(m2)      => Failure(m1 + " or " + m2)
        }
      }
    }

    def ||[U](p2: => ArgParser[U]) = new ArgParser[Either[T,U]] {
      def apply(p:Player, args: List[String]): ParseResult[Either[T,U]] = self(p, args) match {
        case Success(t, rest)   => Success(Left(t), rest)
        case Failure(m1)        => p2(p, args) match {
          case Success(u, rest) => Success(Right(u), rest)
          case Failure(m2)      => Failure(m1 + " or " + m2)
        }
      }
    }

    def * : ArgParser[List[T]] = {
      val x: ArgParser[~[T,List[T]]]  = (this ~ (this*))
      (x ^^ { case t ~ ts => t :: ts }) | success(List[T]())
    }

    def + : ArgParser[List[T]] = {
      val x: ArgParser[~[T,List[T]]]  = (this ~ (this*))
      (x ^^ { case t ~ ts => t :: ts })
    }
  }

  def opt[T](parser: ArgParser[T]) = new ArgParser[Option[T]] {
    def apply(p:Player, args: List[String]): ParseResult[Option[T]] = parser(p, args) match {
      case Failure(m)       => Success(None: Option[T], args)
      case Success(t, rest) => Success(Some(t), rest)
    }
  }

  def success[T](t:T) = new ArgParser[T] {
    def apply(p:Player, args: List[String]) = Success(t, args)
  }

  def num = new ArgParser[Int] {
    def apply(p:Player, args: List[String]) = args match {
      case Nil => Failure("expected int argument")
      case x :: xs => try Success(x.toInt, xs) catch { case e => Failure("not a number: " + x) }
    }
  }

  implicit def stringToParser(s: String) = new ArgParser[String]{
    def apply(p:Player, args: List[String]) = args match {
      case Nil => Failure("expected :" + s + ", but got nothing")
      case x :: xs => if (x==s) Success(x, xs) else Failure("expected: " + s + ", but got: " + x)
    }
  }

  def anyString = new ArgParser[String] {
    def apply(p:Player, args: List[String]) = args match {
      case Nil => Failure("expected string argument")
      case x :: xs => Success(x, xs)
    }
  }

  def player = new ArgParser[Player] {
    def apply(p:Player, args: List[String]) = args match {
      case Nil => Failure("expected player name")
      case x :: xs => p.server.findPlayer(name).fold[ParseResult[Player]](
        Failure("couldnt find player: " + x))(Success(_, xs))
    }
  }

  def material = new ArgParser[Material] {
    def apply(p:Player, args: List[String]) = args match {
      case Nil => Failure("expected player name")
      case x :: xs => p.findMaterial(x).fold[ParseResult[Material]](
        Failure("no such material: " + x))(Success(_, xs))
    }
  }

  def entity = new ArgParser[EntityType] {
    def findEntity(nameOrId:String) = Option(EntityType.fromName(nameOrId)).orElse(
      try Option(EntityType.fromId(nameOrId.toInt)) catch { case e => None }
    )
    def apply(p:Player, args: List[String]) = args match {
      case Nil => Failure("expected player name")
      case x :: xs => findEntity(x).fold[ParseResult[EntityType]](
        Failure("no such entity type: " + x))(Success(_, xs))
    }
  }
}
