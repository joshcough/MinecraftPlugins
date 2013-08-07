package com.joshcough.minecraft.betterjava;

abstract public class Either<T, U> {
  public abstract boolean isLeft();
  public abstract boolean isRight();
  public abstract T getLeft();
  public abstract U getRight();
}
