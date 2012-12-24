package jcdc.pluginfactory

import jcdc.pluginfactory.ScalaPlugin

object YMLGenerator {

  val plugins = List[ScalaPlugin](
    new Arena,
    new GetOffMyLawn,
    new LightningArrows,
    new MultiPlayerCommands,
    new NPCTest,
    new BanArrows,
    new BlockChanger,
    new BlockChangerGold,
    new CurseBan,
    new CursePreventer,
    new Farmer,
    new God,
    new NoRain,
    new PermissionsTest,
    new PluginCommander,
    new Thor,
    new TreeDelogger,
    new ZombieApocalypse,
    new WarpPlugin,
    new WorldEdit,
    new WorldEditV2,
    new YellowBrickRoad)

  def main(args: Array[String]): Unit =
    plugins.foreach(_.writeYML("Josh Cough", "0.1"))
}
