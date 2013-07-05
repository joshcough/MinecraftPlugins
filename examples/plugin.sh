cd ..
echo sbt 'project examples' "run-main jcdc.pluginfactory.examples.YMLGenerator $1" package
sbt 'project examples' "run-main jcdc.pluginfactory.examples.YMLGenerator $1" package
cd examples
rm -f "$BUKKIT/plugins/$2"
echo cp target/scala-2.10/jcdc-plugin-factory-examples_2.10-0.3.1.jar "$BUKKIT/plugins/$2"
cp target/scala-2.10/jcdc-plugin-factory-examples_2.10-0.3.1.jar "$BUKKIT/plugins/$2"
