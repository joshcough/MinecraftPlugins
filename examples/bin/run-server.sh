#!/bin/bash
cd $BUKKIT
java -Xmx512M -Xms256M -cp "craftbukkit-1.2.5-R1.0.jar:scala-library-2.9.1-1.jar:jcdc-plugin-factory_2.9.1-1-0.1.jar:NetLogo.jar:netlogoLib/asm-all-3.3.1.jar:netlogoLib/gluegen-rt-1.1.1.jar:netlogoLib/jhotdraw-6.0b1.jar:netlogoLib/jmf-2.1.1e.jar:netlogoLib/jogl-1.1.1.jar:netlogoLib/log4j-1.2.16.jar:netlogoLib/mrjadapter-1.2.jar:netlogoLib/parboiled-core-1.0.2.jar:netlogoLib/parboiled-java-1.0.2.jar:netlogoLib/pegdown-1.1.0.jar:netlogoLib/picocontainer-2.13.6.jar:netlogoLib/quaqua-7.3.4.jar" org.bukkit.craftbukkit.Main
