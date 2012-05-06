#!/bin/bash
cd $BUKKIT
java -Xmx1024M -Xms512M -cp "craftbukkit-1.2.5-R1.0.jar:scala-library-2.9.1-1.jar:jcdc-plugin-factory_2.9.1-1-0.1.jar:npccreatures-dev-SNAPSHOT.jar:NetLogo.jar" org.bukkit.craftbukkit.Main
