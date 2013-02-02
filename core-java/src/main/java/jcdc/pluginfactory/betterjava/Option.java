package jcdc.pluginfactory.betterjava;

abstract public class Option<T> {
  static public <T> Option<T> apply(T t){
    if(t == null) return new None<T>();
    else return new Some<T>(t);
  }
  public abstract boolean isDefined();
  public abstract T get();
}
