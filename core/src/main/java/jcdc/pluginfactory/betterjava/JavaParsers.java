package jcdc.pluginfactory.betterjava;

import scala.Function1;
import scala.Option;
import scala.Tuple2;
import scala.runtime.AbstractFunction1;

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
      final ArgParser<T> self = this;
      return new ArgParser<Tuple2<T, U>>() {
        public ParseResult<Tuple2<T, U>> parse(List<String> args) {
          ParseResult<T> pr1 = self.parse(args);
          if(pr1.isSuccess()){
            ParseResult<U> pr2 = p2.parse(pr1.rest());
            if(pr2.isSuccess()) return new Success<Tuple2<T, U>>(
              new Tuple2<T, U>(pr1.get(), pr2.get()), pr2.rest());
            else return new Failure<Tuple2<T, U>>(pr2.error());
          }
          else return new Failure<Tuple2<T, U>>(pr1.error());
        }
      };
    }

    public ArgParser<T> or(final ArgParser<T> p2){
      final ArgParser<T> self = this;
      return new ArgParser<T>() {
        public ParseResult<T> parse(List<String> args) {
          ParseResult<T> pr1 = self.parse(args);
          if(pr1.isSuccess()) return pr1;
          else {
            ParseResult<T> pr2 = p2.parse(args);
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
}
