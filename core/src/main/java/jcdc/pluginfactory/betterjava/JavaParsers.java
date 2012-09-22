package jcdc.pluginfactory.betterjava;

import scala.Function1;
import scala.Option;
import scala.runtime.AbstractFunction1;

import java.util.Arrays;
import java.util.LinkedList;

public class JavaParsers {

  static public abstract class ParseResult<T>{
    abstract boolean isFailure();
    abstract boolean isSuccess();
    abstract T get();
    abstract String error();
  }

  static public class Success<T> extends ParseResult<T> {
    private final T t;
    private final LinkedList<String> rest;
    public Success(T t, LinkedList<String> rest){
      this.t = t;
      this.rest = rest;
    }
    boolean isFailure() { return false; }
    boolean isSuccess() { return true; }
    T get(){ return t; }
    String error(){ throw new RuntimeException("cant get error message Success"); }
  }

  static public  class Failure<T> extends ParseResult<T> {
    private String message;
    public Failure(String message){
      this.message = message;
    }
    boolean isFailure() { return true; }
    boolean isSuccess() { return false; }
    T get(){ throw new RuntimeException("cant get from Failure"); }
    String error(){ return message; }
  }

  static public abstract class ArgParser<T> {
    public abstract ParseResult<T> parse(LinkedList<String> args);

    public ParseResult<T> parse(String[] args){
      return parse(new LinkedList<String>(Arrays.asList(args)));
    }

    public ArgParser<T> or(final ArgParser<T> p2){
      final ArgParser<T> self = this;
      return new ArgParser<T>() {
        public ParseResult<T> parse(LinkedList<String> args) {
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
        public ParseResult<U> parse(LinkedList<String> args) {
          ParseResult<T> pr = self.parse(args);
          if(pr.isSuccess()) {
            LinkedList<String> ss = new LinkedList<String>(args);
            ss.removeFirst();
            return new Success<U>(f1.apply(pr.get()), ss);
          }
          else return (Failure<U>)pr;
        }
      };
    }

    <U> ArgParser<U> outputting(final U u){
      return map(new AbstractFunction1<T, U>() {
        public U apply(T t) { return u; }
      });
    }
  }

  static public ArgParser<String> match(final String s){
    return new ArgParser<String>(){
      public ParseResult<String> parse(LinkedList<String> args) {
        if(args.size() > 0) {
          if(args.getFirst().equalsIgnoreCase(s)){
            LinkedList<String> ss = new LinkedList<String>(args);
            return new Success<String>(ss.removeFirst(), ss);
          }
          else return new Failure<String>("expected: " + s + ", but got: " + args.getFirst());
        }
        else return new Failure<String>("expected: " + s + ", but got nothing");
      }
    };
  }

  static public ArgParser<String> anyString = new ArgParser<String>() {
    public ParseResult<String> parse(LinkedList<String> args) {
      if(args.size() > 0) {
        LinkedList<String> ss = new LinkedList<String>(args);
        return new Success<String>(ss.removeFirst(), ss);
      }
      else return new Failure<String>("expected a string, but didn't get any");
    }
  };

  static public <T> ArgParser<Option<T>> opt(final ArgParser<T> p){
    return new ArgParser<Option<T>>(){
      public ParseResult<Option<T>> parse(LinkedList<String> args) {
        ParseResult<T> pr = p.parse(args);
        if(pr.isFailure()) return new Success<Option<T>>(Option.<T>empty(), args);
        else {
          LinkedList<String> ss = new LinkedList<String>(args);
          ss.removeFirst();
          return new Success<Option<T>>(Option.apply(pr.get()), ss);
        }
      }
    };
  }

  static public <T> ArgParser<T> token(final String name, final Function1<String, Option<T>> f){
    return new ArgParser<T>() {
      public ParseResult<T> parse(LinkedList<String> args) {
        if(args.isEmpty()) return new Failure<T>("expected " + name + ", got nothing");
        else{
          Option<T> ot = f.apply(args.getFirst());
          LinkedList<String> ss = new LinkedList<String>(args);
          ss.removeFirst();
          if(ot.isDefined()) return new Success<T>(ot.get(), ss);
          else return new Failure<T>("invalid " + name + ": " + args.getFirst());
        }
      }
    };
  }
}
