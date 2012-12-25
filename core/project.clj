; i use this to uberjar up jcdc-pluginfactory, which is then used as a
; project dependency for all my other plugins,
; so that scala, clojure and all the jcdc.pluginfactory classes are available (on the classpath)
; for all my plugins.
(defproject jcdc.pluginfactory "0.2.0"
  ; puts plugin.xml, and all jcdc.pluginfactory classes in the jar.
  :resource-paths ["target/scala-2.10/classes/"]
  ; put the jar file in the target dir.
  :target-path    "target/"
  :dependencies   [
    [org.clojure/clojure          "1.4.0"]
    [org.scala-lang/scala-library "2.10.0-RC5"]
  ]
)
