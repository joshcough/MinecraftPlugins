
all: compile arena ban-arrows block-changer block-changer-gold curse-ban farmer god lightning-arrows mpc no-rain thor tree-delogger warp-plugin world-edit yellow-brick-road zombie-apocalypse

mlp: clean-plugins compile mpc world-edit minelang-plugin
we: compile world-edit
weq: compile-examples world-edit
test: compile run-tests

warpq: warp-plugin

perms: clean-plugins compile permissions-test

clean-plugins:
	cd $(BUKKIT)/plugins; rm -f *.jar

core: compile-core
compile-core:
	cd ../core; make publish-local

compile-examples:
	cd ..; ./sbt 'project examples' compile

compile: compile-core compile-examples

run-tests:
	cd ..; ./sbt 'project examples' test

go:
	cd $(BUKKIT); java -Djava.ext.dirs=/Library/Java/Home/lib/ext:lib -Xmx2048M -Xms512M -jar bukkit.jar nogui

plugin = ./plugin.sh $1 $2

arena:
	$(call plugin, "arena", "Arena.jar")

ban-arrows:
	$(call plugin, "banarrows", "BanArrows.jar")

block-changer:
	$(call plugin, "blockchanger", "BlockChanger.jar")

block-changer-gold:
	$(call plugin, "blockchangergold", "BlockChangerGold.jar")

copy-server:
	$(call plugin, "copyserver", "CopyServer.jar")

curse-ban:
	$(call plugin, "curseban", "CurseBan.jar")


farmer:
	$(call plugin, "farmer", "Farmer.jar")

god:
	$(call plugin, "god", "God.jar")

lawn:
	$(call plugin, "getoffmylawn", "GetOffMyLawn.jar")

lightning-arrows:
	$(call plugin, "lightningarrows", "LightningArrows.jar")

mpc:
	$(call plugin, "multiplayercommands", "MultiPlayerCommands.jar")

minelang-plugin:
	$(call plugin, "minelangplugin", "MineLangPlugin.jar")

no-rain:
	$(call plugin, "norain", "NoRain.jar")

permissions-test:
	$(call plugin, "permissionstest", "PermissionsTest.jar")

paste-client:
	$(call plugin, "pasteclient", "PasteClient.jar")

plugin-commander:
	$(call plugin, "plugincommander", "PluginCommander.jar")

thor:
	$(call plugin, "thor", "Thor.jar")

tree-delogger:
	$(call plugin, "treedelogger", "TreeDelogger.jar")

warp-plugin:
	$(call plugin, "warpplugin", "WarpPlugin.jar")

world-edit:
	$(call plugin, "worldedit", "WorldEdit.jar")

yellow-brick-road:
	$(call plugin, "yellowbrickroad", "YellowBrickRoad.jar")

zombie-apocalypse: 
	$(call plugin, "zombieapocalypse", "ZombieApocalyspse.jar")
