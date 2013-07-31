; i use this to uberjar up a no op plugin, which is then used as a
; project dependency for all my other plugins,
; so that scala is on the classpath for all my plugins.

(defproject scala-library-plugin "2.10.2"
  ; puts plugin.xml, and all jcdc.pluginfactory classes in the jar.
  :resource-paths ["target/scala-2.10/classes/"]
  ; put the jar file in the target dir.
  :target-path    "target/"
  :dependencies   [
    [org.scala-lang/scala-library               "2.10.2"]
    [org.scalaz/scalaz-core_2.10                "7.0.2"]
    [org.scalaz/scalaz-concurrent_2.10          "7.0.2"]
    [org.scalaz/scalaz-effect_2.10              "7.0.2"]
    [org.scalaz/scalaz-iterv_2.10               "7.0.2"]
    [log4j/log4j                                "1.2.14"]
  ]
)
