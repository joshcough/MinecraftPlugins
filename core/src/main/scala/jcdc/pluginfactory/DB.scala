package jcdc.pluginfactory

import com.avaje.ebean.EbeanServer
import scala.collection.JavaConversions._

case class DB[T](db:EbeanServer, c: Class[T]){
  import scala.collection.JavaConversions._
  // db commands
  def insert[A](a:A): Unit = db.insert(a)
  def query = db.find(c)
  def findAll = query.findList
  def find(f: T => Boolean) = findAll.find(f)
  def where(fields:Map[String, AnyRef]) = query.where.allEq(fields).findList
  def firstWhere(fields:Map[String, AnyRef]) = query.where.allEq(fields).findList.headOption
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