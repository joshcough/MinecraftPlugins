(ns com.joshcough.minecraft.test.testplugin_test
  (:use clojure.test
        com.joshcough.minecraft.test.testplugin))

(deftest a-test (testing "I'm fixed." (is (= 0 0))))

(deftest t1
  (testing "create something"
    (is (= (.toString (new com.joshcough.minecraft.test.testplugin)) "hi"))
  )
)