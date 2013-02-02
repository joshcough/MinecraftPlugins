package jcdc.pluginfactory.db;

import jcdc.pluginfactory.betterjava.BetterJavaPlugin;

public class WarpPluginDB extends BetterJavaPlugin {
  public WarpPluginDB(){
    addDependency("JavaPluginAPI");
    addDbClass(Warp.class);
  }
}
