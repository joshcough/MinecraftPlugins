# put everything in a bukkit dir so we don't pollute the users current directory.
mkdir -p bukkit/lib
cd bukkit

# get bukkit (the minecraft server)
curl -L "http://dl.bukkit.org/latest-rb/craftbukkit.jar" -o craftbukkit.jar
cp craftbukkit.jar lib/
cd lib

# get scala (not getting it anymore because its in netlogo. leaving this here for reference though)
#curl "http://repo1.maven.org/maven2/org/scala-lang/scala-library/2.9.1-1/scala-library-2.9.1-1.jar" -o scala-library-2.9.1-1.jar

# get npc creatures (needed by the netlogo plugin)
curl "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar" -o NPCCreatures.jar

# get NetLogo (which nicely happens to include scala, so we don't have to download that anymore)
curl "http://ccl.northwestern.edu/netlogo/5.0.1/netlogo-5.0.1.tar.gz" -o netlogo-5.0.1.tar.gz

# get jcdc plugin factor library
curl "https://github.com/downloads/joshcough/MinecraftPlugins/jcdc-plugin-factory-0.1.jar" -o jcdc-plugin-factory-0.1.jar

# unpack NetLogo
tar xfvz netlogo-5.0.1.tar.gz
cp netlogo-5.0.1/NetLogo.jar .
cp -r netlogo-5.0.1/lib/* .
