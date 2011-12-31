sbt compile
cp src/main/resources/no-rain.yml src/main/resources/plugin.yml
sbt package
rm ~/Desktop/CraftBukkit/plugins/NoRain.jar
cp target/scala-2.9.1/jcdc-plugin-factory_2.9.1-0.1.jar ~/Desktop/CraftBukkit/plugins/NoRain.jar

