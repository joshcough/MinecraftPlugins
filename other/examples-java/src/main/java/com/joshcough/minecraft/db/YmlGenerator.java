package com.joshcough.minecraft.db;

public class YmlGenerator {
  static public void main(String[] args){
    new com.joshcough.minecraft.db.WarpPluginDB().writeYML("Josh Cough", "0.3.2");
    new com.joshcough.minecraft.db.WarpPluginJava().writeYML("Josh Cough", "0.3.2");
  }
}
