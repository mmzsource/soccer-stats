;; subl repl keyboard shortcuts:
;; Press CTRL and , release both and then press either s, l, b, f for sending selection, line, block or file to repl

;; Sublime workflow:
;; Open project.clj and only then open sublime repl (cmd+shift+p repl)
;; Open core.clj and send complete file to repl (ctrl+, f)

(ns soccer-stats.core
  (:require
     [clojure.java.io :as io]
     [clojure.set :as set]
     [clojure.edn :as edn]
     [clojure.pprint :as pp]))

(def file2edn (edn/read-string (slurp (io/resource "nederlandse-competitie-eindstanden.txt"))))

(defn -main [] (prn "Prints predicted ranks based on (weighted) historic rank data"))

(def weights (:weights file2edn))

(def historic-ranks (:ranks file2edn))

(def club-filter (:filter file2edn))

(defn invert [ranks]
  (zipmap (keys ranks) (map set/map-invert (vals ranks))))

(defn apply-weight [year-ranks year-weight]
  (zipmap (keys year-ranks) (map #(repeat year-weight %) (vals year-ranks))))

(defn calculate-weighted-ranks [ranks weights]
  (let [weight-keys (keys weights)]
    (zipmap weight-keys (map #(apply-weight (% ranks) (% weights)) weight-keys))))

(defn merge-ranks [ranks] (apply merge-with (cons concat (vals ranks))))

(defn calculate-max-ranks [merged-ranks] (apply max (map count (vals merged-ranks))))

(defn avg [coll] (double (/ (reduce + coll) (count coll))))

(def weighted-ranks (calculate-weighted-ranks (invert historic-ranks) weights))

(def merged-ranks (merge-ranks weighted-ranks))

(def max-ranks (calculate-max-ranks merged-ranks))

(defn filler [ranks weights] (inc (count ((first (keys weights)) ranks))))

(def fill-value (filler historic-ranks weights))

(defn apply-filler [coll size fill-value]
  (take size (concat coll (repeat fill-value))))

(defn fill-ranks [ranks size fill-value]
  (zipmap (keys ranks) (map #(apply-filler % size fill-value) (vals ranks))))

(def filled-up-ranks (fill-ranks merged-ranks max-ranks fill-value))

;; if you want to spy on all calculated ranks enable the next line and disable the one after that
;;(defn calculate-average-ranks [ranks] #spy/p (zipmap (keys ranks) (map #(avg %) (vals ranks))))
(defn calculate-average-ranks [ranks] (zipmap (keys ranks) (map #(avg %) (vals ranks))))

(def filtered-ranks (select-keys (calculate-average-ranks filled-up-ranks) club-filter))

(def filtered-sorted-ranks (sort-by val < filtered-ranks))

(def new-clubs (clojure.set/difference club-filter (into #{} (keys filtered-ranks))))

(if (empty? new-clubs)
  (pp/pprint filtered-sorted-ranks)
  (pp/pprint (concat filtered-sorted-ranks (map #(vector % "???") new-clubs))))
