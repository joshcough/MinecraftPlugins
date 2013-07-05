cp src/main/resources/netlogo.yml src/main/resources/plugin.yml
sbt package
echo rm "$BUKKIT/plugins/$2"
rm "$BUKKIT/plugins/$2"
echo cp target/scala-2.10/netlogo-minecraft-plugin_2.10-0.3.1.jar "$BUKKIT/plugins/"
cp target/scala-2.10/netlogo-minecraft-plugin_2.10-0.3.1.jar "$BUKKIT/plugins/"
