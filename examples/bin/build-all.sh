rm -rf ../core/target
rm -rf target
./bin/build-core.sh

# then build all the plugins
# TODO: this means that they dont work individually anymore...fix that...
./bin/arena.sh
./bin/ban-arrows.sh
./bin/block-changer.sh
./bin/curse-ban.sh
./bin/farmer.sh
./bin/god.sh
./bin/lightning-arrows.sh
./bin/multi-player-commands.sh
./bin/no-rain.sh
./bin/npc.sh
./bin/thor.sh
./bin/tree-delogger.sh
./bin/warp.sh
./bin/world-edit.sh
./bin/zombie-apocalypse.sh
