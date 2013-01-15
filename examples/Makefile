
all: compile arena ban-arrows block-changer block-changer-gold curse-ban farmer god lightning-arrows mpc no-rain thor tree-delogger warp-plugin world-edit yellow-brick-road zombie-apocalypse

mlp: clean-plugins compile mpc world-edit minelang-plugin
we: compile world-edit
weq: compile-examples world-edit
test: compile run-tests

perms: clean-plugins compile permissions-test

clean-plugins:
	cd $(BUKKIT)/plugins; rm -f *.jar

core: compile-core
compile-core:
	cd ../core; make publish-local

compile-examples:
	./sbt compile "run-main jcdc.pluginfactory.examples.YMLGenerator"

compile: compile-core compile-examples

run-tests:
	./sbt test

go:
	cd $(BUKKIT); java -Xmx2048M -Xms512M -jar bukkit.jar nogui

plugin = ./plugin.sh $1 $2

arena:
	$(call plugin, "arena.yml", "Arena.jar")

ban-arrows:
	$(call plugin, "banarrows.yml", "BanArrows.jar")

block-changer:
	$(call plugin, "blockchanger.yml", "BlockChanger.jar")

block-changer-gold:
	$(call plugin, "blockchangergold.yml", "BlockChangerGold.jar")

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

minelang-plugin:
	$(call plugin, "minelangplugin.yml", "MineLangPlugin.jar")

no-rain:
	$(call plugin, "norain.yml", "NoRain.jar")

nyc-demo:
	$(call plugin, "nycdemo.yml", "NYCDemo.jar")

permissions-test:
	$(call plugin, "permissionstest.yml", "PermissionsTest.jar")

plugin-commander:
	$(call plugin, "plugincommander.yml", "PluginCommander.jar")

thor:
	$(call plugin, "thor.yml", "Thor.jar")

tree-delogger:
	$(call plugin, "treedelogger.yml", "TreeDelogger.jar")

warp-plugin:
	$(call plugin, "warpplugin.yml", "WarpPlugin.jar")

world-edit:
	$(call plugin, "worldedit.yml", "WorldEdit.jar")

yellow-brick-road:
	$(call plugin, "yellowbrickroad.yml", "YellowBrickRoad.jar")

zombie-apocalypse: 
	$(call plugin, "zombieapocalypse.yml", "ZombieApocalyspse.jar")
