package:
	echo "building scala code"
	./sbt package
	cp target/scala-2.10/scala-plugin-api_2.10-0.3.0.jar $(BUKKIT)/plugins

publish-local:
	echo "building scala code"
	./sbt publish-local
	cp target/scala-2.10/scala-plugin-api_2.10-0.3.0.jar $(BUKKIT)/plugins

release-f:
	echo "copying documentation to pages dir"
	rm -rf ../../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.0
	cp -r ./target/scala-2.10/api ../../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.0
	echo "don't forget:"
	echo "  * commit the scaladocs after adding a link in scaladoc/index.html"
	echo "  * upload the release"
	echo "  * apply git tag for 0.3.0"
	echo "  * bring MinecraftPluginsScalaExample up to date"
	echo "  * consider automating some of these!"

release: publish-local release-f
