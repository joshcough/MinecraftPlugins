; i use this to uberjar up a no op plugin, which is then used as a
; project dependency for all my other plugins,
; so that ermine is on the classpath for all my plugins.

(defproject ermine-library-plugin "0.1"
  ; puts plugin.xml, and all jcdc.pluginfactory classes in the jar.
  :resource-paths ["target/scala-2.10/classes/"]
  ; put the jar file in the target dir.
  :target-path    "target/"
  :dependencies   [ [org.clarifi/ermine "0.1"] ]
)
