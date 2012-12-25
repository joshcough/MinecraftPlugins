## NetLogo Plugin

To install the and run a Minecraft server using the NetLogo plugin on a Mac or Linux:

  1. Download and run the install script I've created. It downloads the Minecraft server, NetLogo, Scala, and everything else needed to run the server. To download it, open a terminal and type this:

    `curl https://raw.github.com/joshcough/MinecraftPlugins/master/netlogo/bin/install.sh | sh`

  2. Run the run-server.sh in the bukkit directory. (`cd bukkit; ./run-server.sh`)
  3. Connect to the server with a regular Minecraft client.

Once connected, you can open most Minecraft models and run them. An example of doing so can be
found here: http://www.youtube.com/watch?v=e3i_aQ8c2OA. To open and run NetLogo models, you must
run Minecraft commands, which is done by pressing the / key, and then typing in the command like so:

    /open Flocking.nlogo

The plugin contains many commands, and a complete listing of them can be found here: https://github.com/joshcough/MinecraftPlugins/blob/master/netlogo/src/main/resources/netlogo.yml

### Opening a model

  * open filename optional(boolean)

Opens the model with the given filename. The file location is relative to the bukkit directory
in which the server is running. However, a full filepath can be given. Currently, the file must
contain no spaces in the name (hopefully this limitation that will be fixed soon).
The second argument is an optional boolean argument that specifies whether or not to update the
patches after each tick. Currently, the default is true, which means that every single patch is
inspected after every tick. I may change this in the future as models with many patches run very slowly.

Here is an example of loading a model:

    /open models/Rope.nlogo false

This assumes that you have placed a models directory in the Bukkit directory,
and it contains Rope.nlogo (which ships with the models library in the NetLogo distribution).

Here are several more examples:

    /open models/WolfSheep.nlogo

    /open models/WolfSheep.nlogo false

    /open /Users/jcough/myModels/MyModel.nlogo

    /open /Users/jcough/myModels/MyModel.nlogo false

When a model is opened, the setup procedure is run automatically by the observer.
Therefore, a model must have a setup procedure, and it must be an observer procedure.

  * /open-no-setup filename optional(boolean)

If your model doesn't have a setup procedure, you can run /open-no-setup instead.
/open-no-setup behaves exactly the same way that /open does, except that it doesn't run
the setup procedure.

### Running an open model