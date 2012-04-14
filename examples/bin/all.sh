# first build core
export BUKKIT=`pwd`/lib
cd ../core
./sbt package
cd ../examples
cp ../core/target/scala-2.9.1-1/jcdc-plugin-factory_2.9.1-1-0.1.jar lib

# then build all the plugins
# TODO: this means that they dont work individually anymore...fix that...
./bin/ban-arrows.sh
./bin/block-changer.sh
./bin/curse-ban.sh
./bin/god.sh
./bin/lightning-arrows.sh
./bin/multi-player-commands.sh
./bin/no-rain.sh
./bin/thor.sh
./bin/tree-delogger.sh
./bin/warp.sh
./bin/world-edit.sh
./bin/zombie-apocalypse.sh
