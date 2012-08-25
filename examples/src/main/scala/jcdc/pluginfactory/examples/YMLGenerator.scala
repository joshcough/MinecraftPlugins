package jcdc.pluginfactory.examples

import jcdc.pluginfactory.ScalaPlugin

object YMLGenerator {

  def main(args: Array[String]) = {
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
    val ymlFile = s"./src/main/resources/${plugin.name.toLowerCase}.yml"
    val ymlContents = plugin.yml("Josh Cough", "0.1")
    val f = new java.io.FileWriter(ymlFile)
    f.write(ymlContents)
    f.close
  }

}
