# builds core, the netlogo plugin, and a few other plugins that are always handy.
rm -rf ../core/target
rm -rf target
./bin/build-core.sh

# then build all the plugins
# TODO: this means that they dont work individually anymore...fix that...
./bin/multi-player-commands.sh
./bin/netlogo.sh
./bin/world-edit.sh
