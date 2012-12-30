
echo "building scala code"
./sbt publish-local

echo "creating release jar"
lein uberjar
mv target/scala-plugin-api-2.10.0-RC5-0.2.1-standalone.jar target/scala-plugin-api-2.10.0-RC5-0.2.1.jar
cp target/scala-plugin-api-2.10.0-RC5-0.2.1.jar $BUKKIT/plugins

echo "created $BUKKIT/plugins/scala-plugin-api-2.10.0-RC5-0.2.1.jar"
