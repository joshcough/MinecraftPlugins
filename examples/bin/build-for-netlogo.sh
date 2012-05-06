# rebuilds core, then the netlogo plugin
rm -rf ../core/target
rm -rf target
./bin/build-core.sh
./bin/netlogo.sh
