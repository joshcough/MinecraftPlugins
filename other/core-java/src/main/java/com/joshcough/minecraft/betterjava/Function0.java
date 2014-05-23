package com.joshcough.minecraft.betterjava;

public interface Function0<A> {
  abstract public A apply();

  public static <T> Function0<T> constant(final T t){ return () -> t; }

}
