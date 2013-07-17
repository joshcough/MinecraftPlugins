all:
	echo "building ermine library plugin"
	./sbt publish-local
	echo "creating release jar"
	#lein uberjar
        #mv target/ermine-library-plugin-2.10.2-standalone.jar target/ermine-library-plugin-0.1.jar
	#cp target/ermine-library-plugin-2.10.2.jar $(BUKKIT)/plugins
	./hack-jar.sh
