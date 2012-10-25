(defproject jcdc.pluginfactory.test "0.1.0-SNAPSHOT"
  :source-paths ["src/main/clojure"]             ; where to find source files
  :test-paths   ["src/test/clojure"]             ; where to find test files
  :compile-path "target/clojure-1.4.0/classes"   ; for .class files
  :target-path "target/"                         ; where to place the project's jar file
  :aot [jcdc.pluginfactory.test.core]            ; what modules to compile
  :description "just testing"
  :dependencies [[org.clojure/clojure "1.4.0"]])
