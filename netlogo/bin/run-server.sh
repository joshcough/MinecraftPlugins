#!/bin/bash
cd $BUKKIT
java -Djava.ext.dirs=lib -Xmx1024M -Xms512M -jar craftbukkit.jar nogui
