sbt compile
cp src/main/resources/$1 src/main/resources/plugin.yml
sbt package
echo rm "$BUKKIT/plugins/$2"
echo cp target/scala-2.9.1-1/jcdc-plugin-factory_2.9.1-1-0.1.jar "$BUKKIT/plugins/$2"
cp target/scala-2.9.1-1/jcdc-plugin-factory_2.9.1-1-0.1.jar "$BUKKIT/plugins/$2"
