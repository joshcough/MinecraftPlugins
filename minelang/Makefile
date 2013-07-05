all: mlp
mlp: compile minelang-plugin
mlp-quick: minelang-plugin
test: compile run-tests

compile:
	cd ../core; ./sbt publish-local; lein uberjar;
	cp ../core/target/jcdc.pluginfactory-0.3.1-standalone.jar $(BUKKIT)/plugins/
	./sbt compile "run-main jcdc.pluginfactory.YMLGenerator"

run-tests:
	./sbt test

plugin = ./bin/plugin.sh $1 $2

minelang-plugin:
	$(call plugin, "minelangplugin.yml", "MineLangPlugin.jar")
