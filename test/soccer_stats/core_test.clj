(ns soccer-stats.core-test
  (:require [clojure.test :refer :all]
            [soccer-stats.core :refer :all]))

(def test-weights 
  {:2015-2016 3 
   :2014-2015 2})

(def test-historic-ranks 
  {:2015-2016 {1 "PSV" 2 "AJA" 3 "TWE"} 
   :2014-2015 {1 "AJA" 2 "PSV" 3 "FEY"}})

(def inverted-historic-ranks
  {:2015-2016 {"PSV" 1 "AJA" 2 "TWE" 3}
   :2014-2015 {"AJA" 1 "PSV" 2 "FEY" 3}})

(deftest test-inversion
  (testing "should result in map of same size"
    (is (= (count (invert test-historic-ranks)) (count test-historic-ranks)))))

(deftest test-rank-inversion
  (testing "should make clubs the keys of the map and rank the values"
    (is (= (:2015-2016 (invert test-historic-ranks)) {"PSV" 1 "AJA" 2 "TWE" 3}))
    (is (= (:2014-2015 (invert test-historic-ranks)) {"AJA" 1 "PSV" 2 "FEY" 3}))
    (is (= (:2015-2016 (invert test-historic-ranks)) (:2015-2016 inverted-historic-ranks)))
    (is (= (:2014-2015 (invert test-historic-ranks)) (:2014-2015 inverted-historic-ranks)))))

(deftest test-apply-weight-count
  (testing "should result in map of same size"
    (is (= 
          (count (apply-weight (:2015-2016 inverted-historic-ranks) (:2015-2016 test-weights))) 
          (count (:2015-2016 inverted-historic-ranks))))))

(deftest test-apply-weight
  (testing "should apply the weightfactor by repeating the rank <weight> number of times"
    (is (= (apply-weight (:2015-2016 inverted-historic-ranks) 2) {"PSV" '(1 1) "AJA" '(2 2) "TWE" '(3 3)}))
    (is (= (apply-weight (:2014-2015 inverted-historic-ranks) 1) {"AJA" '(1) "PSV" '(2) "FEY" '(3)}))))

(deftest test-calculate-weighted-ranks
  (testing "should apply weights to all rankmaps which are reffered to in the weights variable"
    (is (= 
          (calculate-weighted-ranks inverted-historic-ranks test-weights)
          {:2015-2016 {"PSV" '(1 1 1) "AJA" '(2 2 2) "TWE" '(3 3 3)}
           :2014-2015 {"AJA" '(1 1) "PSV" '(2 2) "FEY" '(3 3)}}))))

(deftest test-merge-ranks
  (testing "should merge all years into one map with club keys and weighted rank values"
    (is (= 
          (merge-ranks 
            {:2015-2016 {"PSV" '(1 1 1) "AJA" '(2 2 2) "TWE" '(3 3 3)}
             :2014-2015 {"AJA" '(1 1) "PSV" '(2 2) "FEY" '(3 3)}})
          {"PSV" '(1 1 1 2 2) "AJA" '(2 2 2 1 1) "FEY" '(3 3) "TWE" '(3 3 3)}))))

(deftest test-calculate-max-ranks
  (testing "should compute the max number of item in the values of a merged-ranks map"
    (is (= (calculate-max-ranks {"PSV" '(1 1 1 2 2) "AJA" '(2 2 2 1 1) "FEY" '(3 3) "TWE" '(3 3 3)}) 5))))

(deftest test-filler
  (testing "should find the number of teams in the competition and increment that number"
    (= (filler test-historic-ranks test-weights) 4)))

(deftest test-apply-filler
  (testing "should fill a collection up to a specified size with a specified fill-value"
    (is (= (apply-filler '(1 2 3) 5 42) '(1 2 3 42 42)))))

(deftest test-fill-ranks
  (testing "should fill all ranks upto a specified size with a specified fill-value"
    (is (= 
          (fill-ranks {"PSV" '(1 1 1 2 2) "AJA" '(2 2 2 1 1) "FEY" '(3 3) "TWE" '(3 3 3)} 5 42)
          {"PSV" '(1 1 1 2 2) "AJA" '(2 2 2 1 1) "FEY" '(3 3 42 42 42) "TWE" '(3 3 3 42 42)}))))
