; i use this to uberjar up jcdc-pluginfactory, which is then used as a
; project dependency for all my other scala plugins, so that scala is on their classpath.
(defproject jcdc.pluginfactory "0.1.0"
  :resource-paths ["target/scala-2.10/classes/"]   ; puts plugin.xml in the jar.
  :target-path    "target/"                        ; put the jar file in the target dir.
  :dependencies   [
    [org.clojure/clojure          "1.4.0"]
    [org.scala-lang/scala-library "2.10.0-RC5"]
  ]
)
