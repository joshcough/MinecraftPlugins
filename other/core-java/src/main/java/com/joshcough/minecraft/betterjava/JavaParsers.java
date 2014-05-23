package com.joshcough.minecraft.betterjava;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JavaParsers {

  static class ParserMonad {
    public <T> Parser<T> unit(final T t){
      return args -> new Success<>(t, args);
    }
    public <T, U> Parser<U> bind(final Parser<T> p, final Function1<T, Parser<U>> f) {
      return args -> p.parse(args).fold(Failure::new, (t, rest) -> f.apply(t).parse(rest));
    }
    public <T, U> Parser<U> map(final Parser<T> p, final Function1<T, U> f) {
      return bind(p, t -> unit(f.apply(t)));
    }
  }

  static final ParserMonad parserMonad = new ParserMonad();

  static public abstract class ParseResult<T>{
    abstract <A> A fold(Function1<String, A> failF, Function2<T, List<String>, A> sucF);
    abstract void foldVoid(Function1V<String> failF, Function2V<T, List<String>> sucF);
  }

  static public class Failure<T> extends ParseResult<T> {
    private String message;
    public Failure(String message){ this.message = message; }
    <A> A fold(Function1<String, A> failF, Function2<T, List<String>, A> sucF){
      return failF.apply(message);
    }
    void foldVoid(Function1V<String> failF, Function2V<T, List<String>> sucF){
      failF.apply(message);
    }
  }

  static public class Success<T> extends ParseResult<T> {
    private final T t;
    private final List<String> rest;
    public Success(T t, List<String> rest){ this.t = t; this.rest = rest; }
    <A> A fold(Function1<String, A> failF, Function2<T, List<String>, A> sucF){
      return sucF.apply(t, rest);
    }
    void foldVoid(Function1V<String> failF, Function2V<T, List<String>> sucF){
      sucF.apply(t, rest);
    }
  }

  static public interface Parser<T> {
    public abstract ParseResult<T> parse(List<String> args);

    default ParseResult<T> parse(String[] args){
      return parse(new LinkedList<>(Arrays.asList(args)));
    }

    default <U> Parser<U> bind(Function1<T, Parser<U>> f) { return parserMonad.bind(this, f); }
    default <U> Parser<U> map(final Function1<T, U> f)    { return parserMonad.map(this, f); }

    default <U> Parser<Tuple2<T, U>> and(final Parser<U> p2){ return andLazy(Function0.constant(p2)); }

    default <U> Parser<Tuple2<T, U>> andLazy(final Function0<Parser<U>> p2){
      return bind(t -> p2.apply().map(u -> new Tuple2<>(t, u)));
    }

    default Parser<T> or(final Parser<T> p2){ return orLazy(Function0.constant(p2)); }

    default Parser<T> orLazy(final Function0<Parser<T>> p2){
      return args -> this.parse(args).fold(
        err1 -> p2.apply().parse(args).fold(
          err2 -> new Failure<>(err1 + " or " + err2),
          Success::new
        ),
        Success::new
      );
    }

    default <U> Parser<U> outputting(final U u){ return map(t -> u); }

    default Parser<List<T>> star(){
      return this.plus().orLazy(() -> success((List<T>) (new LinkedList<T>())));
    }

    default Parser<List<T>> plus(){
      return this.andLazy(this::star).map(t -> {
        LinkedList<T> ts = new LinkedList<>(t._2());
        ts.addFirst(t._1());
        return ts;
      });
    }
  }

  static private <T> Success<T> successAndDropOne(T t, List<String> orig){
    LinkedList<String> ss = new LinkedList<>(orig);
    ss.removeFirst();
    return new Success<>(t, ss);
  }

  static public Parser<String> match(final String s){
    return args -> (! args.isEmpty()) ?
      args.get(0).equalsIgnoreCase(s) ?
        successAndDropOne(args.get(0), args) :
        new Failure<>("expected: " + s + ", but got: " + args.get(0)) :
      new Failure<>("expected: " + s + ", but got nothing");
  }

  static public <T> Parser<T> success(final T t){
    return args -> new Success<>(t, args);
  }

  static public Parser<Void> nothing(){
    return args -> new Success<>(null, args);
  }

  static public <T, U> Parser<Either<T,U>> either(final Parser<T> pt, final Parser<U> pu){
    return args -> pt.parse(args).fold(
      err1 -> pu.parse(args).fold(
        err2 -> new Failure<>(err1 + " or " + err2),
        (t2, rest2) -> new Success<>(new Right<>(t2), rest2)),
      (t1, rest1) -> new Success<>(new Left<>(t1), rest1)
    );
  }

  static public Parser<String> anyString = args -> ! args.isEmpty() ?
    successAndDropOne(args.get(0), args) :
    new Failure<>("expected a string, but didn't get any");

  static public <T> Parser<Option<T>> opt(final Parser<T> p){
    return args -> p.parse(args).fold(
      err -> new Success<>(new None<T>(), args),
      (t, rest) -> new Success<>(Option.apply(t), rest));
  }

  static public <T> Parser<T> token(final String name, final Function1<String, Option<T>> f){
    return args -> args.isEmpty() ?
      new Failure<>("expected " + name + ", got nothing") :
      f.apply(args.get(0)).fold(
        () -> new Failure<>("invalid " + name + ": " + args.get(0)),
        t -> successAndDropOne(t, args));
  }

  static public Parser<Integer> integer = token("player", s -> {
    try{ return Option.apply(Integer.parseInt(s)); }
    catch (Exception e) { return new None<>(); }
  });
}
