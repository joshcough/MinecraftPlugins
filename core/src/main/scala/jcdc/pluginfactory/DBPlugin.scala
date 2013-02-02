package jcdc.pluginfactory

import org.squeryl.adapters.DerbyAdapter
import org.squeryl.PrimitiveTypeMode
import org.squeryl.{Session, Schema}

object DB {
  Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance()
  def session(connectionURL: String) = Session.create(
    java.sql.DriverManager.getConnection(connectionURL), new DerbyAdapter
  )
}

trait DBPlugin extends ScalaPlugin {

  val db: Schema
  private def dbFile = new java.io.File(this.getDataFolder, this.name + ".db")
  private def connectionURL = s"jdbc:derby:${dbFile.getAbsolutePath};create=true"
  def newSession: Session = DB.session(connectionURL)

  def initializeDB: Unit = {
    this.getDataFolder.mkdirs
    PrimitiveTypeMode.transaction(newSession)(try db.create catch { case e: Exception => })
    logInfo("database initialized")
  }

  def transaction[A](a: => A): A = PrimitiveTypeMode.transaction(newSession)(a)
  def runQuery[A](a: => A): A = transaction(a)

  override def onEnable(){
    initializeDB
    super.onEnable()
  }
}
