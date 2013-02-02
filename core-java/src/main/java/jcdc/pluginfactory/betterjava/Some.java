package jcdc.pluginfactory.betterjava;

public class Some<T> extends Option<T> {
  private T t;
  public Some(T t){ this.t = t; }
  public T get(){ return t; }
  public boolean isDefined() { return true; }
}
