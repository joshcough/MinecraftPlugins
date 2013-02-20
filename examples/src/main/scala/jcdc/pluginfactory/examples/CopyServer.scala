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
    out.flush
  }
  def read(din: DataInputStream): CopyData = {
    val cor1 = Point(din.readInt, din.readInt, din.readInt)
    val cor2 = Point(din.readInt, din.readInt, din.readInt)
    val size = din.readLong
    val blocks = (0L to size - 1).toStream.map(_ => BlockData.read(din))
    CopyData(cor1, cor2, size, blocks)
  }
  def fromFile(f: File): CopyData = read(new DataInputStream(new FileInputStream(f)))
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

/**
 * Allows players to make copies
 * Stores them locally on the server
 * And allows other servers to copy them to their server.
 */
class CopyServer extends CopyPasteCommon {

  override def dependencies: List[String] = List("WorldEdit")
  override def configs: Map[String, String] = Map(
    "server" -> CopyServer.defaultServer,
    "port"   -> CopyServer.defaultPort.toString
  )

  def commands = List(
    Command(
      name = "cs:save-cube",
      desc = "save a copy to disk",
      args = copyName)(
      body = { case (p,(name, outputFile)) =>
        if(! outputFile.exists){
          def toBlockCopy(b: Block): BlockData = BlockData(b.x, b.y, b.z, b.getTypeId, b.getData)
          val we = pluginManager.findPlugin("WorldEdit").get.asInstanceOf[WorldEdit]
          val c  = we.cube(p)
          val changes = CubeModifier.getTransformationChanges(c).map(pc => toBlockCopy(pc.b))
          CopyData.write(CopyData(c.corner1, c.corner2, c.size.toLong, changes),
            new DataOutputStream(new FileOutputStream(outputFile)))
        }
        else p sendError "A copy with that name already exists! Use cs:delete to remove it."
    }),
    Command(
      name = "cs:delete",
      desc = "delete a copy from this server",
      args = copyName)(
      body = { case (p, (copyId, f)) => delete(p, copyId, f) }
    )
  )
  override def onEnable: Unit = {
    this.getDataFolder.mkdirs
    spawn {
      val ss = new java.net.ServerSocket(getConfig.getInt("port"))
      while(true){
        val s: Socket = ss.accept
        spawn {
          val copyName = new DataInputStream(s.getInputStream).readUTF
          // TODO: if it doesn't exist, send an error message. (Either[SomeError, CopyData])
          val copyFile = getCopyFile(copyName)
          new FileInputStream(copyFile).copyTo(s.getOutputStream, closeInputStream = false)
          s.getOutputStream.flush
        }
      }
    }
  }
}

/**
 * Allows for copying copies from another server to this server
 * and then pasting them locally.
 */
class PasteClient extends CopyPasteCommon {

  override def onEnable: Unit = { this.getDataFolder.mkdirs }

  val serverName = anyStringAs("server")
  val port       = int.named("port")
  val serverInfo: Parser[(String, Int)] = ((serverName ~ port) or serverName).? ^^ {
    case Some(Left(srvr ~ prt)) => (srvr, prt)
    case Some(Right(srvr))      => (srvr, CopyServer.defaultPort)
    case None                   => (CopyServer.defaultServer, CopyServer.defaultPort)
  }

  def commands = List(
    Command(
      name = "pc:paste",
      desc = "paste from the contents of a copy file already transfered to this server",
      args = copyName)(
      body = { case (p, (copyId, inputFile)) =>
        if (! inputFile.exists)
          CopyData.paste(CopyData.fromFile(inputFile), p.loc, p.world)
        else
          p sendError "No copy with that name exists!"
    }),
    Command(
      name = "pc:transfer",
      desc = "Transfer a copy from a CopyServer to this server.",
      args = copyName ~ serverInfo)(
      body = { case (p, (copyId, outputFile) ~ sp) =>
        // TODO: figure out what is going on with scala parsing here...
        val server = sp._1
        val port   = sp._2
        if (! outputFile.exists) {
          // TODO: read Either[SomeError, CopyData] from server
          val s = new Socket(server, port)
          new DataOutputStream(s.getOutputStream).writeUTF(copyId)
          CopyData.write(CopyData.read(new DataInputStream(s.getInputStream)),
            new DataOutputStream(new FileOutputStream(outputFile)))
          s.close
        }
        else p sendError "A copy with that name already exists! Use pc:delete to remove it."
    }),
    Command(
      name = "pc:delete",
      desc = "delete a copy from this server",
      args = copyName)(
      body = { case (p, (copyId, f)) => delete(p, copyId, f) }
    )
  )
}

trait CopyPasteCommon extends CommandsPlugin {
  val copyName: Parser[(String, File)] =
    slurp.named("copy-name").
      filterWith(validFileName)(s => s"invalid copy-name: $s").
      map(s => (s, getCopyFile(s.replace(" ", "-"))))

  def validFileName(s: String) = true
  def getCopyFile(copyId: String) = new File(this.getDataFolder, copyId + ".copy")

  def delete(p: Player, copyId: String, outputFile: File) {
    if (outputFile.exists){
      outputFile.delete
      p ! s"deleted copy: $copyId"
    }
    else p sendError "No copy with that name exists!"
  }
}