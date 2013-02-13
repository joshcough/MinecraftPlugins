package jcdc.pluginfactory.examples

import org.bukkit.block.Block
import org.bukkit.entity.Player
import jcdc.pluginfactory.{BukkitEnrichment, CommandsPlugin, Cube, Point}
import java.io._
import java.net.Socket
import org.bukkit.{Location, Material, World}

object CopyData {
  import BukkitEnrichment._
  def write(c: CopyData, out: DataOutputStream): Unit = {
    out.writeInt(c.corner1.x); out.writeInt(c.corner1.y); out.writeInt(c.corner1.z)
    out.writeInt(c.corner2.x); out.writeInt(c.corner2.y); out.writeInt(c.corner2.z)
    out.writeLong(c.size)
    c.data.foreach(b => BlockData.write(b, out))
    out.flush()
  }
  def read(din: DataInputStream): CopyData = {
    val cor1 = Point(din.readInt, din.readInt, din.readInt)
    val cor2 = Point(din.readInt, din.readInt, din.readInt)
    val size = din.readLong
    val blocks = (0L to size - 1).toStream.map(_ => BlockData.read(din))
    CopyData(cor1, cor2, size, blocks)
  }
  def paste(cd: CopyData, newStart: Location, world: World): Unit = {
    val cdc = new Cube[Point](cd.corner1, cd.corner2)(identity)
    def offsetFromOriginalMin(p: Point) = Point(p.x - cdc.minX, p.y - cdc.minY, p.z - cdc.minZ)
    cd.data.foreach { bd =>
      val offset = offsetFromOriginalMin(Point(bd.x, bd.y, bd.z))
      val newPoint = Point(newStart.x + offset.x, newStart.y + offset.y, newStart.z + offset.z)
      MaterialAndData(Material.getMaterial(bd.material), Some(bd.data)) update world(newPoint)
    }
  }
}
case object BlockData {
  def write(b: BlockData, out: DataOutputStream): Unit = {
    out.writeInt(b.x); out.writeInt(b.y); out.writeInt(b.z)
    out.writeInt(b.material); out.writeByte(b.data)
  }
  def read(in: DataInputStream): BlockData =
    BlockData(in.readInt, in.readInt, in.readInt, in.readInt, in.readByte)
}
case class CopyData(corner1: Point, corner2: Point, size: Long, data: Stream[BlockData])
case class BlockData(x: Int, y: Int, z: Int, material: Int, data: Byte)

object CopyServer {
  val defaultServer = "localhost"
  val defaultPort = 8085
}

class CopyServer extends CommandsPlugin {
  override def dependencies: List[String] = List("WorldEdit")
  override def configs: Map[String, String] = Map(
    "server" -> CopyServer.defaultServer,
    "port"   -> CopyServer.defaultPort.toString
  )
  private def getCopyFile(copyId: Int) = new File(this.getDataFolder, copyId + ".copy")
  def commands = List(
    Command(name = "copy", desc = "save a copy to disk"){ p =>
      val nextId =
        getDataFolder.listFiles.map(_.getName.takeWhile(_ != '.').toInt).sorted.lastOption.getOrElse(0)
      def toBlockCopy(b: Block): BlockData = BlockData(b.x, b.y, b.z, b.getTypeId, b.getData)
      val we = pluginManager.findPlugin("WorldEdit").get.asInstanceOf[WorldEdit]
      val c = we.cube(p)
      val changes = CubeModifier.getTransformationChanges(c).map(pc => toBlockCopy(pc.b))
      CopyData.write(CopyData(c.corner1, c.corner2, c.size.toLong, changes),
        new DataOutputStream(new FileOutputStream(getCopyFile(nextId))))
    }
  )
  override def onEnable: Unit = {
    this.getDataFolder.mkdirs()
    spawn {
      val ss = new java.net.ServerSocket(getConfig.getInt("port"))
      while(true){
        val s: Socket = ss.accept()
        spawn{
          val copyId   = s.getInputStream.read
          val copyFile = getCopyFile(copyId)
          new FileInputStream(copyFile).copyTo(s.getOutputStream, closeInputStream = false)
          s.getOutputStream.flush()
        }
      }
    }
  }
}

class PasteClient extends CommandsPlugin {
  val copyId     = int.named("copyId")
  val serverName = anyStringAs("server")
  val port       = int.named("port")
  def commands = List(
    Command(name = "paste", desc = "paste", args = copyId ~ ((serverName ~ port) or serverName).?){
      case (p, cid ~ Some(Left(srvr ~ prt))) => paste(p, cid, srvr, prt)
      case (p, cid ~ Some(Right(srvr)))      => paste(p, cid, srvr)
      case (p, cid ~ None)                   => paste(p, cid)
    }
  )
  def paste(p: Player, copyId: Int,
            server: String = CopyServer.defaultServer,
            port: Int = CopyServer.defaultPort) = {
    val s = new Socket(server, port)
    s.getOutputStream.write(copyId)
    CopyData.paste(CopyData.read(new DataInputStream(s.getInputStream)), p.loc, p.world)
    s.close()
//    p ! s"corner1: ${copy.corner1.toString}"
//    p ! s"corner2: ${copy.corner2.toString}"
//    p ! s"copy.data.size = ${copy.data.size}"
//    copy.data.foreach(bc => p ! bc.toString)
  }
}
