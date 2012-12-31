
echo "building scala code"
./sbt publish-local

echo "creating release jar"
lein uberjar
mv target/scala-plugin-api-2.10.0-RC5-0.2.2-standalone.jar target/scala-plugin-api-2.10.0-RC5-0.2.2.jar
cp target/scala-plugin-api-2.10.0-RC5-0.2.2.jar $BUKKIT/plugins

echo "created $BUKKIT/plugins/scala-plugin-api-2.10.0-RC5-0.2.2.jar"
