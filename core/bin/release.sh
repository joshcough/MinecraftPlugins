
echo "building scala code"
./sbt publish-local

echo "creating release jar"
lein uberjar
mv target/scala-plugin-api-2.10.0-RC5-0.2.1-standalone.jar target/scala-plugin-api-2.10.0-RC5-0.2.1.jar

echo "copying documentation to pages dir"
rm -rf ../../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.0-RC5-0.2.1
cp -r ./target/scala-2.10/api ../../pages/MinecraftPlugins/scaladoc/scala-plugin-api_2.10.0-RC5-0.2.1

echo "don't forget to commit the docs, and upload the release"
