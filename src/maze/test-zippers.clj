(ns maze.test-zippers
  (:require (clojure [zip :as z])))

;;(require '[clojure.zip :as z])

(def my-vector [:a :b [:c :d [:n :m]] :e])

(def my-map {:a "Hello" :b "man" :c {:m "My" :n "Number"}})

(def my-list '(:a :b '(:n :m)))

(def my-set #{:a :b #{:m :n}})

(def my-vector-vector-zip (z/vector-zip my-vector))

(def my-vector-seq-zip (z/seq-zip (seq my-vector)))

(def my-vector-seq-zip-2 (z/seq-zip my-vector))

(def my-map-seq-zip (z/seq-zip (seq my-map)))
