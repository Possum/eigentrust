(ns possum.eigentrust
  (:require [clojure.math :as math])
  (:import [java.math BigDecimal]))

(defn- to-fixed
  "Converts a number to a BigDecimal with a scale of 10 for deterministic math"
  [n]
  (with-precision 10 (.setScale (bigdec n) 10 BigDecimal/ROUND_HALF_UP)))

(defn- m* [a b] ;; Deterministic multiplication
  (to-fixed (* (to-fixed a) (to-fixed b))))

(defn- dot-product [vector-a vector-b]
  (reduce + (map m* vector-a vector-b)))

(defn- transpose [m]
  (if (seq m)
    (apply mapv vector m)
    []))

;; LAFF matrix-vector multiply algorithm
(defn- matrix-multiply
  "Multiplies a Matrix (list of rows) by a Vector"
  [matrix vector]
  (mapv #(dot-product % vector) matrix))

(defn- safe-div [num denom]
  (if (zero? denom)
    0M
    (.divide (to-fixed num) (to-fixed denom) BigDecimal/ROUND_HALF_UP)))

(defn- normalize-rows [matrix pre-trust]
  (mapv (fn [row]
          (let [total (reduce + row)]
            (if (pos? total)
              (mapv #(safe-div % total) row)
              pre-trust))) ;; Dead-ends trust the seeds
        matrix))

(defn- normalize-vector [v]
  (let [total (reduce + v)]
    (if (pos? total)
      (mapv #(safe-div % total) v)
      ;; Fallback if passed an all-zero vector: distribute trust equally
      (let [cnt (count v)]
        (vec (repeat cnt (safe-div 1 cnt)))))))

(def default-options
  {:alpha 0.1M
   :epsilon 0.0001M
   :max-iterations 50})

;; Public API

(defn calculate-scores
  "Computes deterministic EigenTrust scores for a given network topology.
   
   Inputs:
   - outgoing-trust-matrix: A square matrix representing trust links
   - pre-trust: A normalized vector representing the trusted seed nodes
   - opts: Optional map configuration (:alpha, :epsilon, :max-iterations)
   
   Returns a normalized vector of global trust scores."
  ([matrix pre-trust] 
   (calculate-scores matrix pre-trust {}))
  ([outgoing-trust-matrix pre-trust opts]
   (let [{:keys [alpha epsilon max-iterations]} (merge default-options opts)
         ;; Normalize outgoing trust (rows sum to 1.0) and transpose
         ;; so that incoming trust drives the scores (Columns sum to 1.0)
         pre-trust (normalize-vector pre-trust)
         C (transpose (normalize-rows outgoing-trust-matrix pre-trust))]
     (loop [t-current pre-trust
            iterations 0]
       (let [t-next (matrix-multiply C t-current)
             t-stabilized (mapv #(+ (m* (- 1.0M alpha) %1) (m* alpha %2))
                                t-next pre-trust)
             delta (apply + (map #(Math/abs (double (- %1 %2))) t-stabilized t-current))]
         (if (or (< delta epsilon) (> iterations max-iterations))
           t-stabilized
           (recur t-stabilized (inc iterations))))))))
