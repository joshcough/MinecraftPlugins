(defproject jcdc.pluginfactory.test "0.1.0-SNAPSHOT"
  :source-paths   ["src/main/clojure"]                   ; where to find source files
  :test-paths     ["src/test/clojure"]                   ; where to find test files
  :resource-paths ["src/main/resources"]                 ; resources to include in the jar
  :compile-path   "target/clojure-1.4.0/classes"         ; for .class files
  :target-path    "target/"                              ; where to place the project's jar file
  :aot            [jcdc.pluginfactory.test.testplugin]   ; what modules to compile
  :description    "just testing"
  :repositories   [
    ["bukkit"             "http://repo.bukkit.org/content/repositories/releases/"],
    ["Sonatype Snapshots" "http://oss.sonatype.org/content/repositories/snapshots"],
    ["Sonatype Releases"  "http://oss.sonatype.org/content/repositories/releases"],
  ]
  :dependencies   [
    [org.clojure/clojure          "1.4.0"]
    [org.bukkit/craftbukkit       "1.4.5-R0.2"]
    [org.scala-lang/scala-library "2.10.2"],
    ; how do i deal with this guy? - deal with it by downloading it,, and then publishing it to local mvn
    ; "ch.spacebase" % "NPCCreatures" % "1.4" from "http://dev.bukkit.org/media/files/584/232/NPCCreatures.jar",
  ]
)
