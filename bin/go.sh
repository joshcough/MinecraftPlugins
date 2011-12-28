./clean.sh

sbt package
cp target/scala-2.9.1/jcdc-plugin-factory_2.9.1-0.1.jar scala-lib
cd scala-lib
jar xf jcdc-plugin-factory_2.9.1-0.1.jar
rm jcdc-plugin-factory_2.9.1-0.1.jar
rm -rf META-INF

cp curseban.yml plugin.yml
jar -cf CurseBan.jar *
mv CurseBan.jar ~/Desktop/CraftBukkit/plugins 

cp lightning-arrows.yml plugin.yml
jar -cf LightningArrows.jar *
mv LightningArrows.jar ~/Desktop/CraftBukkit/plugins 

cp block-changer.yml plugin.yml
jar -cf BlockChanger.jar *
mv BlockChanger.jar ~/Desktop/CraftBukkit/plugins 

cp multi-player-commands.yml plugin.yml
jar -cf MultiPlayerCommands.jar *
mv MultiPlayerCommands.jar ~/Desktop/CraftBukkit/plugins

cp tree-delogger.yml plugin.yml
jar -cf TreeDelogger.jar *
mv TreeDelogger.jar ~/Desktop/CraftBukkit/plugins
