all:
	echo "building scala library plugin"
	./sbt publish-local
	echo "creating release jar"
	lein uberjar
	mv target/scala-library-plugin-2.10.0-standalone.jar target/scala-library-plugin-2.10.0.jar
	cp target/scala-library-plugin-2.10.0.jar $(BUKKIT)/plugins
