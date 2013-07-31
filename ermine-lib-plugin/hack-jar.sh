cd target/scala-2.10
mkdir tmp
cp -r classes tmp
cd tmp/classes
cp ~/.ivy2/local/com.clarifi/ermine-legacy_2.10/0.1/jars/ermine-legacy_2.10.jar .
cp ~/.ivy2/local/machines/machines_2.10/0.1-SNAPSHOT/jars/machines_2.10.jar .
jar xf ermine-legacy_2.10.jar
jar xf machines_2.10.jar
rm ermine-legacy_2.10.jar
rm machines_2.10.jar
jar cf ermine-library-plugin_2.10-0.1.jar *
cp ermine-library-plugin_2.10-0.1.jar  ~/.ivy2/local/jcdc.pluginfactory/ermine-library-plugin_2.10/0.1/jars/ermine-library-plugin_2.10.jar
cp ermine-library-plugin_2.10-0.1.jar ~/work/MinecraftPlugins/bukkit/plugins
cd ../..
rm -rf tmp
