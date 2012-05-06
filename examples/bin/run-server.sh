#!/bin/bash
cd $BUKKIT
java -Xmx1024M -Xms512M -cp "craftbukkit.jar:scala-library-2.9.1-1.jar:jcdc-plugin-factory-0.1.jar:NPCCreatures.jar:NetLogo.jar" org.bukkit.craftbukkit.Main
