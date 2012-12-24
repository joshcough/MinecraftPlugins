; i use this to uberjar up jcdc-pluginfactory, which is then used as a
; project dependency for all my other scala plugins, so that scala is on their classpath.
(defproject jcdc.pluginfactory "0.1.0"
  :resource-paths ["target/scala-2.10/classes/"]   ; resources to include in the jar
  :target-path    "target/"                        ; where to place the project's jar file
  :dependencies   [
    [org.scala-lang/scala-library "2.10.0-RC5"],
  ]
)
