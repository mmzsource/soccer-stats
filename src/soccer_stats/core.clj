;; subl repl keyboard shortcuts:
;; Press CTRL and , release both and then press either s, l, b, f for sending selection, line, block or file to repl

;; Sublime workflow:
;; Open project.clj and only then open sublime repl (cmd+shift+p repl)
;; Open core.clj and send complete file to repl (ctrl+, f)

(ns soccer-stats.core
  (:require 
     [clojure.java.io :as io]
     [clojure.edn :as edn]
     [clojure.pprint :as pp]))

(def file2edn
  (edn/read-string (slurp (io/resource "nederlandse-competitie-eindstanden.txt"))))

(def weights (:weights file2edn))

(def historic-ranks (:ranks file2edn))

(defn invert [ranks]
  (zipmap (keys ranks) (map clojure.set/map-invert (vals ranks))))

(defn apply-weight [year-ranks year-weight]
  (zipmap (keys year-ranks) (map #(repeat year-weight %) (vals year-ranks))))

(defn calculate-weighted-ranks [ranks weights] 
  (let [weight-keys (keys weights)]
    (zipmap weight-keys (map #(apply-weight (% ranks) (% weights)) weight-keys))))

(defn merge-ranks [ranks] (apply merge-with (cons concat (vals ranks))))

(defn avg [coll] (double (/ (reduce + coll) (count coll))))

(def weighted-ranks (calculate-weighted-ranks (invert historic-ranks) weights))

(def merged-ranks (merge-ranks weighted-ranks))

(def predicted-ranks (sort-by val < (zipmap (keys merged-ranks) (map #(avg %) (vals merged-ranks)))))

(pp/pprint predicted-ranks)