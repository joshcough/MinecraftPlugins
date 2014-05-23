package com.joshcough.minecraft.betterjava;

abstract public class Option<T> {
  static public <T> Option<T> apply(T t){
    return t == null ? new None<>() : new Some<>(t);
  }
  public abstract boolean isDefined();
  public abstract T get();

  public <U> U fold(Function0<U> empty, Function1<T,U> full){
    return isDefined() ? full.apply(get()) : empty.apply();
  }

  public <U> Option<U> map(Function1<T,U> f){
    return fold(None::new, t -> new Some<>(f.apply(t)));
  }

  public T getOrElse(Function0<T> f){ return fold(f, t -> t); }
}
