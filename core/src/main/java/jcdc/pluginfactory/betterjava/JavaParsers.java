package jcdc.pluginfactory.betterjava;

import scala.Function0;
import scala.Function1;
import scala.Option;
import scala.Tuple2;
import scala.runtime.AbstractFunction0;
import scala.runtime.AbstractFunction1;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class JavaParsers {

  static public abstract class ParseResult<T>{
    abstract boolean isFailure();
    abstract boolean isSuccess();
    abstract T get();
    abstract String error();
    abstract List<String> rest();
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
  }

  static public class Failure<T> extends ParseResult<T> {
    private String message;
    public Failure(String message){ this.message = message; }
    boolean isFailure() { return true; }
    boolean isSuccess() { return false; }
    T get(){ throw new RuntimeException("cant get value from Failure"); }
    List<String> rest(){ throw new RuntimeException("cant get rest Failure"); }
    String error(){ return message; }
  }

  static public abstract class ArgParser<T> {
    public abstract ParseResult<T> parse(List<String> args);

    public ParseResult<T> parse(String[] args){
      return parse(new LinkedList<String>(Arrays.asList(args)));
    }

    public <U> ArgParser<Tuple2<T, U>> and(final ArgParser<U> p2){
      return andLazy(constant(p2));
    }

    public <U> ArgParser<Tuple2<T, U>> andLazy(final Function0<ArgParser<U>> p2){
      final ArgParser<T> self = this;
      return new ArgParser<Tuple2<T, U>>() {
        public ParseResult<Tuple2<T, U>> parse(List<String> args) {
          ParseResult<T> pr1 = self.parse(args);
          if(pr1.isSuccess()){
            ParseResult<U> pr2 = p2.apply().parse(pr1.rest());
            if(pr2.isSuccess()) return new Success<Tuple2<T, U>>(
              new Tuple2<T, U>(pr1.get(), pr2.get()), pr2.rest());
            else return new Failure<Tuple2<T, U>>(pr2.error());
          }
          else return new Failure<Tuple2<T, U>>(pr1.error());
        }
      };
    }

    private <T> Function0<T> constant(final T t){
      return new AbstractFunction0<T>(){ public T apply() { return t; } };
    };

    public ArgParser<T> or(final ArgParser<T> p2){ return orLazy(constant(p2)); }

    public ArgParser<T> orLazy(final Function0<ArgParser<T>> p2){
      final ArgParser<T> self = this;
      return new ArgParser<T>() {
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

    public <U> ArgParser<U> map(final Function1<T, U> f1){
      final ArgParser<T> self = this;
      return new ArgParser<U>() {
        public ParseResult<U> parse(List<String> args) {
          ParseResult<T> pr = self.parse(args);
          if(pr.isSuccess()) return new Success<U>(f1.apply(pr.get()), pr.rest());
          else return (Failure<U>)pr;
        }
      };
    }

    public <U> ArgParser<U> outputting(final U u){
      return map(new AbstractFunction1<T, U>() { public U apply(T t) { return u; } });
    }

    public ArgParser<List<T>> star(){
      return this.plus().orLazy(new AbstractFunction0<ArgParser<List<T>>>(){
        public ArgParser<List<T>> apply() {
          return success((List<T>) (new LinkedList<T>()));
        }
      });
    }

    public ArgParser<List<T>> plus(){
      final ArgParser<T> self = this;
      return this.andLazy(new AbstractFunction0<ArgParser<List<T>>>(){
        public ArgParser<List<T>> apply() { return self.star(); }
      }).map(new AbstractFunction1<Tuple2<T, List<T>>, List<T>>() {
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

  static public ArgParser<String> match(final String s){
    return new ArgParser<String>(){
      public ParseResult<String> parse(List<String> args) {
        if(! args.isEmpty()) {
          if(args.get(0).equalsIgnoreCase(s)) return successAndDropOne(args.get(0), args);
          else return new Failure<String>("expected: " + s + ", but got: " + args.get(0));
        }
        else return new Failure<String>("expected: " + s + ", but got nothing");
      }
    };
  }

  static public <T> ArgParser<T> success(final T t){
    return new ArgParser<T>() {
      public ParseResult<T> parse(List<String> args) {
        return new Success<T>(t, args);
      }
    };
  }

  static public ArgParser<Void> nothing(){
    return new ArgParser<Void>() {
      public ParseResult<Void> parse(List<String> args) {
        return new Success<Void>(null, args);
      }
    };
  }

  static public <T, U> ArgParser<Either<T,U>> either(final ArgParser<T> pt, final ArgParser<U> pu){
    return new ArgParser<Either<T,U>>() {
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

  static public ArgParser<String> anyString = new ArgParser<String>() {
    public ParseResult<String> parse(List<String> args) {
      if(! args.isEmpty()) return successAndDropOne(args.get(0), args);
      else return new Failure<String>("expected a string, but didn't get any");
    }
  };

  static public <T> ArgParser<Option<T>> opt(final ArgParser<T> p){
    return new ArgParser<Option<T>>(){
      public ParseResult<Option<T>> parse(List<String> args) {
        ParseResult<T> pr = p.parse(args);
        if(pr.isFailure()) return new Success<Option<T>>(Option.<T>empty(), args);
        else return new Success<Option<T>>(Option.apply(pr.get()), pr.rest());
      }
    };
  }

  static public <T> ArgParser<T> token(final String name, final Function1<String, Option<T>> f){
    return new ArgParser<T>() {
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

  static public ArgParser<Integer> integer = token("player", new AbstractFunction1<String, Option<Integer>>() {
    public Option<Integer> apply(String s) {
      try{ return Option.apply(Integer.parseInt(s)); }
      catch (Exception e) { return Option.empty(); }
    };
  });
}
