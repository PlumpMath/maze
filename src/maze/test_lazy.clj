(ns maze.test-lazy)

; It's a big question,

(def aaa 23)

(defn postitve-numbers
  ([] (postitve-numbers 1))
  ([n] (cons n (lazy-seq (do
                           (println n)
                           (postitve-numbers (inc n)))))))
