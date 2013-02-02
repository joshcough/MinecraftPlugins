cp src/main/resources/$1 src/main/resources/plugin.yml
rm target/scala-2.10/java-examples_2.10-0.3.0.jar
sbt package
rm -f "$BUKKIT/plugins/$2"
echo cp target/scala-2.10/java-examples_2.10-0.3.0.jar "$BUKKIT/plugins/$2"
cp target/scala-2.10/java-examples_2.10-0.3.0.jar "$BUKKIT/plugins/$2"
