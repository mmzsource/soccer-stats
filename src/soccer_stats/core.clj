(ns soccer-stats.core
  (:require 
     [clojure.java.io :as io]
     [clojure.edn :as edn]
     [clojure.pprint :as pp]))

;; subl repl keyboard shortcuts:
;; Press CTRL and , release both and then press either s, l, b, f for sending selection, line, block or file to repl

(def positions
  (edn/read-string (slurp (io/resource "nederlandse-competitie-eindstanden.txt"))))

(defn transform-positions [m k reps]
  (zipmap (vals (k m)) (map #(repeat reps %) (keys (k m)))))

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

(defn avg [coll] (double (/ (reduce + coll) (count coll))))

(def sorted-positions (sort-by val < (zipmap (keys weighted-positions) (map #(avg %) (vals weighted-positions)))))

(pp/pprint sorted-positions)
