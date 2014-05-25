//package com.joshcough.minecraft.examples
//
//import org.bukkit.block.Block
//import org.bukkit.entity.Player
//import com.joshcough.minecraft._
//import java.io._
//import java.net.Socket
//import org.bukkit.{Location, Material}
//import scala.Some
//import CubeModifier.PotentialChange
//
//case class CopyData(corner1: Point, corner2: Point, size: Long, data: Stream[BlockData])
//case class BlockData(x: Int, y: Int, z: Int, material: Int, data: Byte)
//
//object CopyData {
//  import BukkitEnrichment._
//
//  /**
//   * Read a CopyData from a DataInputStream
//   */
//  def read(din: DataInputStream): CopyData = {
//    val cor1 = Point(din.readInt, din.readInt, din.readInt)
//    val cor2 = Point(din.readInt, din.readInt, din.readInt)
//    val size = din.readLong
//    val blocks = (0L to size - 1).toStream.map(_ => BlockData.read(din))
//    CopyData(cor1, cor2, size, blocks)
//  }
//
//  /**
//   * Read a CopyData from a File
//   */
//  def fromFile(f: File): CopyData = read(new DataInputStream(new FileInputStream(f)))
//
//  /**
//   * Write a CopyData to a DataOutputStream
//   * @param c
//   * @param out
//   */
//  def write(c: CopyData, out: DataOutputStream): Unit = {
//    out.writeInt(c.corner1.x); out.writeInt(c.corner1.y); out.writeInt(c.corner1.z)
//    out.writeInt(c.corner2.x); out.writeInt(c.corner2.y); out.writeInt(c.corner2.z)
//    out.writeLong(c.size)
//    c.data.foreach(b => BlockData.write(b, out))
//    out.flush
//  }
//
//  /**
//   * Write a CopyData to a File
//   */
//  def toFile(cd: CopyData, outputFile: File): Unit =
//    write(cd, new DataOutputStream(new FileOutputStream(outputFile)))
//
//  /**
//   * Paste this CopyData into the world, at the given location.
//   * @param cd
//   * @param newStart
//   */
//  def paste(cd: CopyData, newStart: Location): Unit = {
//    val cdc = Cube[Point](cd.corner1, cd.corner2)(identity)
//    cd.data.map { bd =>
//      val offset = Point(bd.x - cdc.minX, bd.y - cdc.minY, bd.z - cdc.minZ)
//      new PotentialChange(
//        newStart.world(Point(newStart.x + offset.x, newStart.y + offset.y, newStart.z + offset.z)),
//        MaterialAndData(Material.getMaterial(bd.material), Some(bd.data)))
//    }
//  }
//}
//
//case object BlockData {
//  def write(b: BlockData, out: DataOutputStream): Unit = {
//    out.writeInt(b.x); out.writeInt(b.y); out.writeInt(b.z)
//    out.writeInt(b.material); out.writeByte(b.data)
//  }
//  def read(in: DataInputStream): BlockData =
//    BlockData(in.readInt, in.readInt, in.readInt, in.readInt, in.readByte)
//}
//
//object CopyServer {
//  val defaultServer = "localhost"
//  val defaultPort = 8085
//}
//
///**
// * Allows players to make copies
// * Stores them locally on the server
// * And allows other servers to copy them to their server.
// * Depends on my Scala WorldEdit, in order to get the Cube data from it.
// */
//class CopyServer extends CopyPasteCommon {
//
//  override def dependencies: List[String] = List("WorldEdit")
//  override def configs: Map[String, String] = Map(
//    "server" -> CopyServer.defaultServer,
//    "port"   -> CopyServer.defaultPort.toString
//  )
//
//  def commands = List(
//    Command(
//      name = "cs:save-cube",
//      desc = "save a copy to disk",
//      args = copyName)(
//      body = { case (p,(name, outputFile)) =>
//        if(! outputFile.exists){
//          def toBlockCopy(b: Block) = BlockData(b.x, b.y, b.z, b.getTypeId, b.getData)
//          val we = pluginManager.findPlugin("WorldEdit").get.asInstanceOf[WorldEdit]
//          val c  = we.cube(p)
//          val changes = CubeModifier.getTransformationChanges(c).map(pc => toBlockCopy(pc.b))
//          CopyData.write(CopyData(c.corner1, c.corner2, c.size.toLong, changes),
//            new DataOutputStream(new FileOutputStream(outputFile)))
//          p ! s"saved $name"
//        }
//        else p sendError "A copy with that name already exists! Use cs:delete to remove it."
//    }),
//    deleteCommand("cs")
//  )
//  override def onEnable: Unit = {
//    this.getDataFolder.mkdirs
//    spawn {
//      val ss = new java.net.ServerSocket(getConfig.getInt("port"))
//      while(true){
//        val s: Socket = ss.accept
//        spawn {
//          // read the name of the copy that they want to transfer
//          val copyName = new BufferedReader(new InputStreamReader(s.getInputStream)).readLine()
//          // TODO: if it doesn't exist, send an error message.
//          // maybe use FO, and write (Either[SomeError, CopyData])
//          val copyFile = getCopyFile(copyName)
//          new FileInputStream(copyFile).copyTo(s.getOutputStream, closeInputStream = false)
//          s.getOutputStream.flush
//        }
//      }
//    }
//  }
//}
//
///**
// * Allows for copying copies from another server to this server
// * and then pasting them locally.
// */
//class PasteClient extends CopyPasteCommon {
//
//  override def onEnable: Unit = { this.getDataFolder.mkdirs }
//
//  val serverName = anyStringAs("server")
//  val port       = int.named("port")
//  val serverInfo: Parser[(String, Int)] = ((serverName ~ port) or serverName).? ^^ {
//    case Some(Left(srvr ~ prt)) => (srvr, prt)
//    case Some(Right(srvr))      => (srvr, CopyServer.defaultPort)
//    case None                   => (CopyServer.defaultServer, CopyServer.defaultPort)
//  }
//
//  def commands = List(
//    Command(
//      name = "pc:paste",
//      desc = "paste from the contents of a copy file already transfered to this server",
//      args = copyName)(
//      body = { case (p, (copyId, inputFile)) =>
//        if (inputFile.exists){
//          CopyData.paste(CopyData.fromFile(inputFile), p.loc)
//          // TODO: hook into world edit paste and especially undo?
//          p ! s"pasted!"
//        }
//        else p sendError "No copy with that name exists!"
//    }),
//    Command(
//      name = "pc:transfer",
//      desc = "Transfer a copy from a CopyServer to this server.",
//      args = copyName ~ serverInfo)(
//      body = { case (p, (copyId, outputFile) ~ sp) =>
//        // TODO: figure out what is going on with scala parsing here...
//        val server = sp._1
//        val port   = sp._2
//        if (! outputFile.exists) {
//          // TODO: read Either[SomeError, CopyData] from server
//          val s = new Socket(server, port)
//          // write the name of the copy to be transfered
//          val out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream))
//          out.write(copyId + "\n")
//          out.flush
//          // the read the copy
//          CopyData.write(CopyData.read(new DataInputStream(s.getInputStream)),
//            new DataOutputStream(new FileOutputStream(outputFile)))
//          s.close
//
//          p ! s"$copyName transfered"
//        }
//        else p sendError "A copy with that name already exists! Use pc:delete to remove it."
//    }),
//    deleteCommand("pc")
//  )
//}
//
//trait CopyPasteCommon extends CommandsPlugin {
//
//  def deleteCommand(prefix: String) = Command(
//    name = s"$prefix:delete",
//    desc = "delete a copy from this server",
//    args = copyName)(
//    body = { case (p, (copyId, f)) => delete(p, copyId, f) }
//  )
//
//  val copyName: Parser[(String, File)] =
//    slurp.named("copy-name").
//      filterWith(validFileName)(s => s"invalid copy-name: $s").
//      map(s => (s, getCopyFile(s.replace(" ", "-"))))
//
//  def validFileName(s: String) = s.trim != "" && ! s.contains(" ")
//  def getCopyFile(copyId: String) = new File(this.getDataFolder, copyId + ".copy")
//
//  def delete(p: Player, copyId: String, outputFile: File) {
//    if (outputFile.exists){
//      outputFile.delete
//      p ! s"deleted copy: $copyId"
//    }
//    else p sendError "No copy with that name exists!"
//  }
//}