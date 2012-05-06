# first build core
cd ../core
./sbt package
cd ../examples
cp ../core/target/scala-2.9.1-1/jcdc-plugin-factory_2.9.1-1-0.1.jar $BUKKIT/lib/jcdc-plugin-factory-0.1.jar
