package jcdc.pluginfactory

import com.avaje.ebean.EbeanServer

case class DB[T](db:EbeanServer, c: Class[T]){
  import scala.collection.JavaConversions._
  // db commands
  def insert[A](a:A): Either[String, Unit] =
    try Right(db.insert(a)) catch { case e => Left(e.toString) }
  def query = db.find(c)
  def findAll = query.findList
  def foreach[U](f: T => U) = findAll.foreach(f)
  def delete(a:AnyRef){ db.delete(a) }
}

trait SingleClassDBPlugin[T] extends ScalaPlugin {
  // db stuff
  val dbClass: Class[T]
  override def dbClasses = List(dbClass)
  lazy val db = new DB(getDatabase, dbClass)
}