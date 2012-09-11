all: compile-core compile-examples arena ban-arrows block-changer curse-ban farmer god lightning-arrows mpc no-rain thor tree-delogger warp-plugin world-edit

most: compile-core compile-examples

compile-core:
	cd ../core; rm -rf ../target; ./sbt package publish-local
	cp ../core/target/scala-2.10/jcdc-plugin-factory_2.10-0.1.jar $(BUKKIT)/lib/jcdc-plugin-factory-0.1.jar

compile-examples:
	rm -rf target
	./sbt compile "run-main jcdc.pluginfactory.examples.YMLGenerator"

compile-fast:
	cd ../core; ./sbt package publish-local
	./sbt compile 

plugin = ./bin/plugin.sh $1 $2

arena:
	$(call plugin, "arena.yml", "Arena.jar")

ban-arrows:
	$(call plugin, "banarrows.yml", "BanArrows.jar")

block-changer:
	$(call plugin, "blockchanger.yml", "BlockChanger.jar")

curse-ban:
	$(call plugin, "curseban.yml", "CurseBan.jar")

farmer:
	$(call plugin, "farmer.yml", "Farmer.jar")

god:
	$(call plugin, "god.yml", "God.jar")

lawn:
	$(call plugin, "getoffmylawn.yml", "GetOffMyLawn.jar")

lightning-arrows:
	$(call plugin, "lightningarrows.yml", "LightningArrows.jar")

mpc:
	$(call plugin, "multiplayercommands.yml", "MultiPlayerCommands.jar")

no-rain:
	$(call plugin, "norain.yml", "NoRain.jar")

thor:
	$(call plugin, "thor.yml", "Thor.jar")

tree-delogger:
	$(call plugin, "treedelogger.yml", "TreeDelogger.jar")

warp-plugin:
	$(call plugin, "warpplugin.yml", "WarpPlugin.jar")

world-edit:
	$(call plugin, "worldedit.yml", "WorldEdit.jar")

zombie-apocalypse: 
	$(call plugin, "zombieapocalypse.yml", "ZombieApocalyspse.jar")

