
all: compile arena ban-arrows block-changer block-changer-gold farmer god lightning-arrows mpc no-rain thor tree-delogger world-edit yellow-brick-road zombie-apocalypse

mlp: clean-plugins compile mpc world-edit minelang-plugin
we: compile world-edit
test: compile run-tests

warpq: warp-plugin

demo: plugin-commander block-changer-gold block-changer world-edit

perms: clean-plugins compile permissions-test

clean-plugins:
	cd $(BUKKIT)/plugins; rm -f *.jar

compile:
	cd ../core; make publish-local

run-tests:
	cd ..; ./sbt 'project examples' test

go:
	cd $(BUKKIT); java -Djava.ext.dirs=/Library/Java/Home/lib/ext:lib -Xmx2048M -Xms512M -jar bukkit.jar nogui

plugin = ./plugin.sh $1 $2

arena:
	$(call plugin, "Arena")

ban-arrows:
	$(call plugin, "BanArrows")

block-changer:
	$(call plugin, "BlockChanger")

block-changer-gold:
	$(call plugin, "BlockChangerGold")

#copy-server:
#	$(call plugin, "copyserver", "CopyServer.jar")

#curse-ban:
#	$(call plugin, "curseban", "CurseBan.jar")

farmer:
	$(call plugin, "Farmer")

god:
	$(call plugin, "God")

lawn:
	$(call plugin, "GetOffMyLawn")

lightning-arrows:
	$(call plugin, "LightningArrows")

mpc:
	$(call plugin, "MultiPlayerCommands")

minelang-plugin:
	$(call plugin, "MineLangPlugin")

no-rain:
	$(call plugin, "NoRain")

#permissions-test:
#	$(call plugin, "permissionstest", "PermissionsTest.jar")

#paste-client:
#	$(call plugin, "pasteclient", "PasteClient.jar")

plugin-commander:
	$(call plugin, "PluginCommander")

thor:
	$(call plugin, "Thor")

tree-delogger:
	$(call plugin, "TreeDelogger")

#warp-plugin:
#	$(call plugin, "warpplugin", "WarpPlugin.jar")

world-edit:
	$(call plugin, "WorldEdit")

yellow-brick-road:
	$(call plugin, "YellowBrickRoad")

zombie-apocalypse: 
	$(call plugin, "ZombieApocalypse")
