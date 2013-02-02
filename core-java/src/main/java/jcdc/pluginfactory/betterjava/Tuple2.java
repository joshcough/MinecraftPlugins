package jcdc.pluginfactory.betterjava;

public class Tuple2<A, B> {
  private A a;
  private B b;

  public Tuple2(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public A _1(){ return a; }
  public B _2(){ return b; }
}
