package com.joshcough.minecraft.betterjava;

abstract public class Option<T> {
  static public <T> Option<T> apply(T t){
    return t == null ? new None<>() : new Some<>(t);
  }
  public abstract boolean isDefined();
  public abstract T get();
}
