MinecraftPluginsScalaExample
============================

A simple example of how to use my Scala API for creating minecraft plugins. To run it:

## 1a / Prerequisites

These instructions assume you are on a mac, and have git installed on your machine. 
They probably aren't that difficult to translate into Windows or whatever.

* Git can be downloaded here: http://git-scm.com/downloads
* Macs can be purchased here: http://www.apple.com

## 1. Clone the example repo 

The example repo is here: https://github.com/joshcough/MinecraftPluginsScalaExample. To clone it, run:

`git clone git://github.com/joshcough/MinecraftPluginsScalaExample.git`

The example repo contains one simple plugin: BlockChanger, and the code and documentation for it can be found [here](https://github.com/joshcough/MinecraftPluginsScalaExample/blob/master/src/main/scala/BlockChanger.scala). It demonstrates how to write Listeners and Commands. 

The example repo also contains a bukkit server, and a Makefile which will automatically build everything, and run the server for you. 

## 2. make and play

Run `make` at the command line, fire up Minecraft, connect to your local server, and have fun!

## 3. Playing on your own server

If you want to play on your own server, after running `make`, copy the jars in bukkit/plugins to your bukkit plugins folder. Start your server, and play. 

## More Information

More information on writing plugins in Scala can be found on the [MinecraftPlugins/wiki](https://github.com/joshcough/MinecraftPlugins/wiki)
