#!/bin/bash
cd $BUKKIT
#java -Djava.ext.dirs=lib:/Library/Java/Home/lib/ext/ -Xmx1024M -Xms512M -jar bukkit.jar nogui
java -Djava.ext.dirs=lib:/Library/Java/Home/lib/ext/ -Xmx512M -Xms256M -jar bukkit.jar nogui
