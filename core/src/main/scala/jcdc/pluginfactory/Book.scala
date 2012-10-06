package jcdc.pluginfactory

import net.minecraft.server.{NBTTagCompound, NBTTagList, NBTTagString}
import org.bukkit.Material
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import EnrichmentClasses._

// http://pastebin.com/NcMUtdEM
case class Book(author:String, title:Option[String], pages:List[String])

object Book {
  def apply(author: Player, title: Option[String], pages:String*): Book =
    Book(author.name, title, pages.toList)

  def fromHand(p:Player): Book = from(p, p.holding)

  implicit def toItemStack(b:Book): ItemStack = {
    val stack = new CraftItemStack(new ItemStack(Material.WRITTEN_BOOK, 1))
    val tags: NBTTagCompound = {
      stack.getHandle.setTag(new NBTTagCompound)
      stack.getHandle.getTag
    }
    tags.set("pages", {
      val ps = new NBTTagList("pages")
      if  (b.pages.isEmpty) ps.add(new NBTTagString("1", ""))
      else b.pages.zipWithIndex.foreach{ case (p,i) => ps.add(new NBTTagString(i.toString, p)) }
      ps
    })
    tags.setString("author", b.author)
    b.title.foreach(t => tags.setString("title",  t))
    stack
  }

  def from(p:Player, stack: ItemStack): Book = {
    val item =
      if(stack.isInstanceOf[CraftItemStack]) stack.asInstanceOf[CraftItemStack]
      else if (stack.isInstanceOf[org.bukkit.inventory.ItemStack]) new CraftItemStack(stack)
      else throw new IllegalArgumentException(stack.toString)
    val tags: NBTTagCompound = {
      if(item.getHandle.getTag == null) item.getHandle.setTag(new NBTTagCompound)
      item.getHandle.getTag
    }
    Book(
      Option(tags.getString("author")).getOrElse(p.name),
      Option(tags.getString("title")),
      Option(tags.getList("pages")).fold(List[String]())(ps =>
        (0 until ps.size()).map(ps.get).map(_.toString).toList
      )
    )
  }
}

//case class Book(stack:CraftItemStack) {
//  private val tags: NBTTagCompound = {
//    if(stack.getHandle.getTag == null) stack.getHandle.setTag(new NBTTagCompound)
//    stack.getHandle.getTag
//  }
//  // getters
//  def pages: List[String] = Option(tags.getList("pages")).fold(List[String]())(ps =>
//    (0 until ps.size()).map(ps.get).map(_.toString).toList
//  )
//  def author: Option[String] = Option(tags.getString("author"))
//  def title : Option[String] = Option(tags.getString("title"))
//  // setters
//  def pages_=(newPages:List[String]): Unit = tags.set("pages", {
//    val ps = new NBTTagList("pages")
//    if (newPages.isEmpty) ps.add(new NBTTagString("1", "yo dawg?"))
//    else newPages.zipWithIndex.foreach{ case (p,i) => ps.add(new NBTTagString(i.toString, p)) }
//    ps
//  })
//  def addPages_=(newPages:List[String]): Unit = pages = pages ::: newPages
//  def author_=  (a:String): Unit = tags.setString("author", a)
//  def title_=   (t:String): Unit = tags.setString("title",  t)
//}
//def apply(author: Player, title: Option[String], pages:String*): Book =
//Book(p.name, title, pages.toList)
//val b    = Book(new CraftItemStack(new ItemStack(Material.WRITTEN_BOOK, 1)))
//b.pages  = pages.toList
//b.author = author.name
//title.foreach(t => b.title = t)
//b
//}