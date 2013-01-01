cp src/main/resources/$1 src/main/resources/plugin.yml
sbt package
rm -f "$BUKKIT/plugins/$2"
echo cp target/scala-2.10/jcdc-plugin-factory-examples_2.10-0.2.2.jar "$BUKKIT/plugins/$2"
cp target/scala-2.10/jcdc-plugin-factory-examples_2.10-0.2.2.jar "$BUKKIT/plugins/$2"
