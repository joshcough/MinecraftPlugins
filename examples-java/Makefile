all: compile warp-plugin-db warp-plugin-java

core: compile-core

compile-core:
	cd ../core-java; make publish-local

compile-examples:
	./sbt run publish-local

compile: compile-core compile-examples

go:
	cd $(BUKKIT); java -Xmx2048M -Xms512M -jar bukkit.jar nogui

plugin = ./plugin.sh $1 $2

warp-plugin-db:
	$(call plugin, "warpplugindb.yml", "WarpPluginDB.jar")

warp-plugin-java:
	$(call plugin, "warppluginjava.yml", "WarpPluginJava.jar")
