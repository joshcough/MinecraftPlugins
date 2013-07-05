publish-local:
	echo "building scala code"
	cd ..; ./sbt 'project core' publish-local
	cp target/scala-2.10/scala-plugin-api_2.10-0.3.1.jar $(BUKKIT)/plugins

release-f:
	echo "copying documentation to pages dir"
	rm -rf ../../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.1
	cp -r ./target/scala-2.10/api ../../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.1
	echo "don't forget:"
	echo "  * commit the scaladocs after adding a link in scaladoc/index.html"
	echo "  * upload the release"
	echo "  * apply git tag for 0.3.1"
	echo "  * bring MinecraftPluginsScalaExample up to date"
	echo "  * consider automating some of these!"

release: publish-local release-f
