#!/bin/bash
cd $BUKKIT
java -Djava.ext.dirs=lib:/Library/Java/Home/lib/ext/ -Xmx1024M -Xms512M -jar craftbukkit-1.3.2-R1.0.jar nogui
