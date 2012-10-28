(ns jcdc.pluginfactory.test.testplugin_test
  (:use clojure.test
        jcdc.pluginfactory.test.testplugin))

(deftest a-test (testing "I'm fixed." (is (= 0 0))))

(deftest t1
  (testing "create something"
    (is (= (.toString (new jcdc.pluginfactory.test.testplugin)) "hi"))
  )
)