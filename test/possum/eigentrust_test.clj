(ns possum.eigentrust-test
  (:require [clojure.test :refer [deftest is testing]]
            [possum.eigentrust :refer :all :as et]))

(deftest test-normalization
  (testing "Rows should sum to 1.0 after normalization"
    (let [matrix [[10 10] [0 0]]
          pre-trust [0.5M 0.5M]
          normalized (@#'et/normalize-rows matrix pre-trust)]
      (is (= 1.0M (reduce + (first normalized))))
      (is (= 0.5M (first (second normalized))))))) ;; Dead-end case 

(deftest test-convergence
  (testing "A simple 2-node mutual trust should converge"
    (let [matrix [[0 1] [1 0]]
          pre-trust [0.5M 0.5M]
          scores (et/calculate-scores matrix pre-trust {:epsilon 0.001M})]
      (is (= 2 (count scores)))
      ;; Total trust in a closed system should remain ~1.0
      (is (< (abs (- 1.0 (reduce + scores))) 0.01)))))
