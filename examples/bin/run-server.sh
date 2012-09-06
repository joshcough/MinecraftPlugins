#!/bin/bash
cd $BUKKIT
java -Djava.ext.dirs=lib:/Library/Java/Home/lib/ext/ -Xmx1024M -Xms512M -jar craftbukkit.jar nogui
