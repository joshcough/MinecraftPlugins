(defproject jcdc.pluginfactory.test "0.1.0-SNAPSHOT"
  :source-paths ["src/main/clojure"]             ; where to find source files
  :test-paths   ["src/test/clojure"]             ; where to find test files
  :resource-paths ["src/main/resources"]         ; resources to include in the jar
  :compile-path "target/clojure-1.4.0/classes"   ; for .class files
  :target-path "target/"                         ; where to place the project's jar file
  :aot [jcdc.pluginfactory.test.testplugin]            ; what modules to compile
  :description "just testing"
  :repositories [["bukkit" "http://repo.bukkit.org/content/repositories/releases/"]]
  :dependencies [
    [org.clojure/clojure "1.4.0"]
    [org.bukkit/craftbukkit "1.3.2-R2.0"]
  ]
)
