package jcdc.pluginfactory

import com.avaje.ebean.EbeanServer

case class DB[T](db:EbeanServer, c: Class[T]){
  import scala.collection.JavaConversions._
  // db commands
  def insert[A](a:A): Unit = db.insert(a)
  def query = db.find(c)
  def findAll = query.findList
  def foreach[U](f: T => U) = findAll.foreach(f)
  def delete(t:T){ db delete t }
  def update(t:T){ db update t }
}

trait SingleClassDBPlugin[T] extends ScalaPlugin {
  // db stuff
  val dbClass: Class[T]
  override def dbClasses = List(dbClass)
  lazy val db = new DB(getDatabase, dbClass)
}