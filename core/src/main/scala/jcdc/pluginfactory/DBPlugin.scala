//package jcdc.pluginfactory
//
//import org.squeryl.adapters.DerbyAdapter
//import org.squeryl.{Table, PrimitiveTypeMode, Session, Schema}
//import PrimitiveTypeMode._
//import org.bukkit.entity.Player
//import org.squeryl.internals.DatabaseAdapter
//
//object DB {
//  Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance()
//  def session(connectionURL: String, adapter: DatabaseAdapter = new DerbyAdapter) =
//    Session.create(
//      java.sql.DriverManager.getConnection(connectionURL), adapter
//    )
//}
//
//trait DBPlugin extends ScalaPlugin {
//
//  val db: Schema
//  private def dbFile = new java.io.File(this.getDataFolder, this.name + ".db")
//  private def connectionURL = s"jdbc:derby:${dbFile.getAbsolutePath};create=true"
//  def newSession: Session = DB.session(connectionURL)
//
//  def initializeDB: Unit = {
//    this.getDataFolder.mkdirs
//    PrimitiveTypeMode.transaction(newSession)(try db.create catch { case e: Exception => })
//    logInfo("database initialized")
//  }
//
//  def transaction[A](a: => A): A = PrimitiveTypeMode.transaction(newSession)(a)
//  def runQuery[A](a: => A): A = transaction(a)
//
//  override def onEnable(){
//    initializeDB
//    super.onEnable()
//  }
//}
//
//trait DBPluginWithCommands extends DBPlugin with CommandsPlugin {
//  def DBCommand(name: String, desc: String)(body: Player => Unit): Command =
//    DBCommand(name, desc, eof){ case (p, _) => body(p) }
//
//  def DBCommand[T](name: String, desc: String, args: Parser[T])
//                   (body: ((Player, T)) => Unit): Command =
//    Command(name, desc, args){ case (p, t) => transaction(body(p, t)) }
//}