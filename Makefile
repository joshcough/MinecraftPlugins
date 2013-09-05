core-release-f:
	echo "copying documentation to pages dir"
	rm -rf ../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.1
	cp -r core/target/scala-2.10/api ../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.2-0.3.1
	echo "don't forget:"
	echo "  * commit the scaladocs after adding a link in scaladoc/index.html"
	echo "  * upload the release"
	echo "  * apply git tag for 0.3.1"
	echo "  * bring MinecraftPluginsScalaExample up to date"
	echo "  * consider automating some of these!"

core-release: publish-local release-f

go-ermine:
	sbt 'project erminecraft' publishLocal
	cd $(BUKKIT); java -Djava.ext.dirs=/Library/Java/Home/lib/ext:lib -Xmx2048M -Xms512M -jar bukkit.jar nogui

go:
	cd $(BUKKIT); java -Djava.ext.dirs=/Library/Java/Home/lib/ext:lib -Dorg.nlogo.noGenerator=true -Xmx2048M -Xms512M -jar bukkit.jar nogui
