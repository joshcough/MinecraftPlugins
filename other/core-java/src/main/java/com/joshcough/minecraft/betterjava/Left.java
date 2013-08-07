package com.joshcough.minecraft.betterjava;

public class Left<T, U> extends Either<T, U> {
  private T t;
  public Left(T t){ this.t = t; }
  public boolean isLeft() { return true; }
  public boolean isRight() { return false; }
  public T getLeft() { return t; }
  public U getRight() { throw new RuntimeException("can't getRight from a Left."); }
}
