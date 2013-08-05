(ns com.joshcough.minecraft.test.testplugin
  (:gen-class :extends org.bukkit.plugin.java.JavaPlugin))

(defn -toString [this] "hi")

(defn -onEnable [this]
  (print "test clojure plugin enabled!")
)

(defn -main [greetee] (println (str "Hello " greetee "!")))
