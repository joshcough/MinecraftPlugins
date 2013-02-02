echo cp src/main/resources/$1 src/main/resources/plugin.yml
cp src/main/resources/$1 src/main/resources/plugin.yml
echo sbt compile "run-main jcdc.pluginfactory.examples.YMLGenerator $1" package
sbt compile "run-main jcdc.pluginfactory.examples.YMLGenerator $1" package
rm -f "$BUKKIT/plugins/$2"
echo cp target/scala-2.10/jcdc-plugin-factory-examples_2.10-0.3.0.jar "$BUKKIT/plugins/$2"
cp target/scala-2.10/jcdc-plugin-factory-examples_2.10-0.3.0.jar "$BUKKIT/plugins/$2"
