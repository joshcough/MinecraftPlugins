publish-local:
	echo "building java code"
	cd ..; ./sbt 'project core-java' publish-local
	cp target/scala-2.10/java-plugin-api_2.10-0.3.1.jar $(BUKKIT)/plugins
