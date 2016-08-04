(ns soccer-stats.core
  (:require 
     [clojure.java.io :as io]
     [clojure.edn :as edn]
     [clojure.set :as set]
     [com.rpl.specter :as specter]
     [clojure.pprint :as pp]))

;; subl repl keyboard shortcuts:
;; Press CTRL and , release both and then press either s, l, b, f for sending selection, line, block or file to repl

(def positions
  (edn/read-string (slurp (io/resource "nederlandse-competitie-eindstanden.txt"))))

(map set/map-invert (vals positions))

(defn transform-positions [m k reps]
  (zipmap (vals (k m)) (map #(repeat reps %) (keys (k m)))))

(transform-positions positions :2006-2007 2)

{"Ajax" '(1 2 2) "PSV" '(2 1 3)}

(map reverse (vals positions))

(defn avg [coll] (double (/ (reduce + coll) (count coll))))
(defn map-val-fun [m f]
  (into {} (for [[k v] m] [k (f v)])))

(def club-ranks (apply merge-with (comp flatten list) (map set/map-invert (vals positions))))

(map set/map-invert (vals positions))


club-ranks

(filter #{"Ajax"} club-ranks)

(merge-with (comp flatten list) (map set/map-invert (vals positions)))

(-> 16 list)

(map-val-fun {"RKC Waalwijk" '(9 16 17 14 18), "Sparta" 12} (comp avg flatten list))

((comp avg flatten list) 1)

(map-val-fun club-ranks avg)
(map-val-fun {"A" '(1 2 3) "B" '(2 3)} avg)

(["A" '(1 2 3)])

(vector '(1))

(vector 1 2)

(merge-with (comp flatten list) {:a 1} {:a 2} {:a 3} {:b 1})

((comp flatten list) 1)


(defn specter-sorted-positions [positions]
  (->> positions
      vals
      (map set/map-invert)             ;; '({"Club" 1} {"Club" 2})
      (transform [ALL ALL LAST] list)  ;; '({"Club" '(1)} {"Club" '(2)})
      (apply merge-with concat)        ;; {"Club" '(1 2)}
      (transform [MAP-VALS] avg)       ;; {"Club" 1.5}
      (sort-by second)))

(specter-sorted-positions2 positions)

(def weighted-positions (merge-with concat 
  (transform-positions positions :2006-2007 1) 
  (transform-positions positions :2007-2008 2)
  (transform-positions positions :2008-2009 3)
  (transform-positions positions :2009-2010 4)
  (transform-positions positions :2010-2011 5)
  (transform-positions positions :2011-2012 6)
  (transform-positions positions :2012-2013 7)
  (transform-positions positions :2013-2014 8)
  (transform-positions positions :2014-2015 9)
  (transform-positions positions :2015-2016 10)))



(def sorted-positions (sort-by val < (zipmap (keys weighted-positions) (map #(avg %) (vals weighted-positions)))))

(pp/pprint sorted-positions)
