core-release:
	echo "copying documentation to pages dir"
	rm -rf ../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.2
	cp -r scala/core/target/scala-2.10/api ../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.2
	echo "don't forget:"
	echo "  * commit the scaladocs after adding a link in scaladoc/index.html"
	echo "  * upload the release"
	echo "  * apply git tag for 0.3.2"
	echo "  * bring MinecraftPluginsScalaExample up to date"
	echo "  * consider automating some of these!"

go:
	cd $(BUKKIT); java -Djava.ext.dirs=/Library/Java/Home/lib/ext:lib -Dorg.nlogo.noGenerator=true -Xmx2048M -Xms512M -jar bukkit.jar nogui
