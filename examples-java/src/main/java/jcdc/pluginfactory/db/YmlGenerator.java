package jcdc.pluginfactory.db;

public class YmlGenerator {
  static public void main(String[] args){
    new jcdc.pluginfactory.db.WarpPluginDB().writeYML("Josh Cough", "0.3.1");
    new jcdc.pluginfactory.db.WarpPluginJava().writeYML("Josh Cough", "0.3.1");
  }
}
