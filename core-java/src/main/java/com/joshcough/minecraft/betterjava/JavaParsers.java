package com.joshcough.minecraft.betterjava;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JavaParsers {

  static class ParserMonad {
    public <T> Parser<T> unit(final T t){
      return new Parser<T>(){
        public ParseResult<T> parse(List<String> args) {
          return new Success<T>(t, args);
        }
      };
    }
    public <T, U> Parser<U> bind(final Parser<T> p, final Function1<T, Parser<U>> f) {
      return new Parser<U>() {
        public ParseResult<U> parse(List<String> args) {
          p.parse(args).fold(
              new Function1<String, ParseResult<U>>() {
                public ParseResult<U> apply(String msg) { return new Failure<U>(msg); }
              }, new Function2<T, List<String>, ParseResult<U>>() {
                public ParseResult<U> apply(T t, List<String> rest) {
                  return f.apply(t).parse(rest);
                }
              }
          );
          return null;
        }
      };
    }
    public <T, U> Parser<U> map(final Parser<T> p, final Function1<T, U> f) {
      return bind(p, new Function1<T, Parser<U>>(){
        public Parser<U> apply(T t) { return unit(f.apply(t)); }
      });
    }
  }

  static final ParserMonad parserMonad = new ParserMonad();

  static public abstract class ParseResult<T>{
    abstract boolean isFailure();
    abstract boolean isSuccess();
    abstract T get();
    abstract String error();
    abstract List<String> rest();
    abstract <A> A fold(Function1<String, A> failF, Function2<T, List<String>, A> sucF);
  }

  static public class Failure<T> extends ParseResult<T> {
    private String message;
    public Failure(String message){ this.message = message; }
    boolean isFailure() { return true; }
    boolean isSuccess() { return false; }
    T get(){ throw new RuntimeException("cant get value from Failure"); }
    List<String> rest(){ throw new RuntimeException("cant get rest Failure"); }
    String error(){ return message; }
    <A> A fold(Function1<String, A> failF, Function2<T, List<String>, A> sucF){
      return failF.apply(message);
    }
  }

  static public class Success<T> extends ParseResult<T> {
    private final T t;
    private final List<String> rest;
    public Success(T t, List<String> rest){ this.t = t; this.rest = rest; }
    boolean isFailure() { return false; }
    boolean isSuccess() { return true; }
    T get(){ return t; }
    List<String> rest() { return rest; }
    String error(){ throw new RuntimeException("cant get error message Success"); }
    <A> A fold(Function1<String, A> failF, Function2<T, List<String>, A> sucF){
      return sucF.apply(t, rest);
    }
  }

  static public abstract class Parser<T> {
    public abstract ParseResult<T> parse(List<String> args);

    public ParseResult<T> parse(String[] args){
      return parse(new LinkedList<String>(Arrays.asList(args)));
    }

    public <U> Parser<U> bind(Function1<T, Parser<U>> f) { return parserMonad.bind(this, f); }
    public <U> Parser<U> map(final Function1<T, U> f)    { return parserMonad.map(this, f); }

    public <U> Parser<Tuple2<T, U>> and(final Parser<U> p2){ return andLazy(constant(p2)); }

    public <U> Parser<Tuple2<T, U>> andLazy(final Function0<Parser<U>> p2){
      return bind(new Function1<T, Parser<Tuple2<T, U>>>(){
        public Parser<Tuple2<T, U>> apply(final T t) {
          return p2.apply().map(new Function1<U, Tuple2<T, U>>() {
            public Tuple2<T, U> apply(U u) { return new Tuple2<T, U>(t, u); }
          });
        }
      });
    }

    private <T> Function0<T> constant(final T t){
      return new Function0<T>(){ public T apply() { return t; } };
    };

    public Parser<T> or(final Parser<T> p2){ return orLazy(constant(p2)); }

    public Parser<T> orLazy(final Function0<Parser<T>> p2){
      final Parser<T> self = this;
      return new Parser<T>() {
        public ParseResult<T> parse(List<String> args) {
          ParseResult<T> pr1 = self.parse(args);
          if(pr1.isSuccess()) return pr1;
          else {
            ParseResult<T> pr2 = p2.apply().parse(args);
            if(pr2.isSuccess()) return pr2;
            else return new Failure<T>(pr1.error() + " or " + pr2.error());
          }
        }
      };
    }

    public <U> Parser<U> outputting(final U u){
      return map(new Function1<T, U>() { public U apply(T t) { return u; } });
    }

    public Parser<List<T>> star(){
      return this.plus().orLazy(new Function0<Parser<List<T>>>(){
        public Parser<List<T>> apply() {
          return success((List<T>) (new LinkedList<T>()));
        }
      });
    }

    public Parser<List<T>> plus(){
      final Parser<T> self = this;
      return this.andLazy(new Function0<Parser<List<T>>>(){
        public Parser<List<T>> apply() { return self.star(); }
      }).map(new Function1<Tuple2<T, List<T>>, List<T>>() {
        public List<T> apply(Tuple2<T, List<T>> t) {
          LinkedList<T> ts = new LinkedList<T>(t._2());
          ts.addFirst(t._1());
          return ts;
        }
      });
    }
  }

  static private <T> Success<T> successAndDropOne(T t, List<String> orig){
    LinkedList<String> ss = new LinkedList<String>(orig);
    ss.removeFirst();
    return new Success<T>(t, ss);
  }

  static public Parser<String> match(final String s){
    return new Parser<String>(){
      public ParseResult<String> parse(List<String> args) {
        if(! args.isEmpty()) {
          if(args.get(0).equalsIgnoreCase(s)) return successAndDropOne(args.get(0), args);
          else return new Failure<String>("expected: " + s + ", but got: " + args.get(0));
        }
        else return new Failure<String>("expected: " + s + ", but got nothing");
      }
    };
  }

  static public <T> Parser<T> success(final T t){
    return new Parser<T>() {
      public ParseResult<T> parse(List<String> args) {
        return new Success<T>(t, args);
      }
    };
  }

  static public Parser<Void> nothing(){
    return new Parser<Void>() {
      public ParseResult<Void> parse(List<String> args) {
        return new Success<Void>(null, args);
      }
    };
  }

  static public <T, U> Parser<Either<T,U>> either(final Parser<T> pt, final Parser<U> pu){
    return new Parser<Either<T,U>>() {
      public ParseResult<Either<T,U>> parse(List<String> args) {
        ParseResult<T> pr1 = pt.parse(args);
        if(pr1.isSuccess())
          return new Success<Either<T, U>>(new Left<T, U>(pr1.get()), pr1.rest());
        else {
          ParseResult<U> pr2 = pu.parse(args);
          if(pr2.isSuccess())
            return new Success<Either<T, U>>(new Right<T, U>(pr2.get()), pr2.rest());
          else return new Failure<Either<T,U>>(pr1.error() + " or " + pr2.error());
        }
      }
    };
  }

  static public Parser<String> anyString = new Parser<String>() {
    public ParseResult<String> parse(List<String> args) {
      if(! args.isEmpty()) return successAndDropOne(args.get(0), args);
      else return new Failure<String>("expected a string, but didn't get any");
    }
  };

  static public <T> Parser<Option<T>> opt(final Parser<T> p){
    return new Parser<Option<T>>(){
      public ParseResult<Option<T>> parse(List<String> args) {
        ParseResult<T> pr = p.parse(args);
        if(pr.isFailure()) return new Success<Option<T>>(new None<T>(), args);
        else return new Success<Option<T>>(Option.apply(pr.get()), pr.rest());
      }
    };
  }

  static public <T> Parser<T> token(final String name, final Function1<String, Option<T>> f){
    return new Parser<T>() {
      public ParseResult<T> parse(List<String> args) {
        if(args.isEmpty()) return new Failure<T>("expected " + name + ", got nothing");
        else{
          Option<T> ot = f.apply(args.get(0));
          if(ot.isDefined()) return successAndDropOne(ot.get(), args);
          else return new Failure<T>("invalid " + name + ": " + args.get(0));
        }
      }
    };
  }

  static public Parser<Integer> integer = token("player", new Function1<String, Option<Integer>>() {
    public Option<Integer> apply(String s) {
      try{ return Option.apply(Integer.parseInt(s)); }
      catch (Exception e) { return new None<Integer>(); }
    };
  });
}
