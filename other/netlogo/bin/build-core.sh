# first build core
cd ../core
./sbt package publish-local
cd ../examples
cp ../core/target/scala-2.10/jcdc-plugin-factory_2.10-0.3.1.jar $BUKKIT/lib/jcdc-plugin-factory-0.3.1.jar