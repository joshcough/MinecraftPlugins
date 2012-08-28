package jcdc.pluginfactory.examples

import jcdc.pluginfactory.ScalaPlugin
import java.io.{File, FileWriter}

object YMLGenerator {

  def main(args: Array[String]): Unit = {
    List[ScalaPlugin](
      new Arena,
      new GetOffMyLawn,
      new LightningArrows,
      new MultiPlayerCommands,
      new NPCTest,
      new BanArrows,
      new BlockChanger,
      new CurseBan,
      new CursePreventer,
      new Farmer,
      new God,
      new NoRain,
      new Thor,
      new TreeDelogger,
      new ZombieApocalypse,
      new WarpPlugin,
      new WorldEdit,
      new WorldEditV2).foreach(writeYML)
  }

  def writeYML(plugin: ScalaPlugin): Unit = {
    val resources = new File("./src/main/resources")
    resources.mkdir
    val ymlFile = new File(resources, s"${plugin.name.toLowerCase}.yml")
    val ymlContents = plugin.yml("Josh Cough", "0.1")
    val f = new FileWriter(ymlFile)
    f.write(ymlContents)
    f.close
  }
}
