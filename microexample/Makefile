go: setup run-server

compile:
	./sbt package
	mkdir -p bukkit/plugins
	cp target/scala-2.11/block-changer-plugin_2.11-0.3.4.jar bukkit/plugins

run-server:
	cd bukkit && ./run-server.sh

setup: compile
	cp ~/.ivy2/cache/com.joshcough/scala-minecraft-plugin-api_2.11/jars/scala-minecraft-plugin-api_2.11-0.3.4.jar bukkit/plugins/
	curl -L "http://dl.bintray.com/joshcough/maven/com/joshcough/scala-minecraft-scala-library_2.11/0.3.4/scala-minecraft-scala-library_2.11-0.3.4-assembly.jar" -o bukkit/plugins/scala-minecraft-scala-library_2.11-0.3.4-assembly.jar

clean:
	rm -rf target
	rm -rf bukkit/plugins/*
