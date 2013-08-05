package com.joshcough.minecraft.betterjava;

public class Right<T, U> extends Either<T, U> {
  private U u;
  public Right(U u){ this.u = u; }
  public boolean isLeft() { return false; }
  public boolean isRight() { return true; }
  public T getLeft() { throw new RuntimeException("can't getLeft from a Right."); }
  public U getRight() { return u; }
}
