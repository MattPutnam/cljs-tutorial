(ns cljs-tutorial.utils
  (:require
    cljs.pprint
    clojure.string
    [dmohs.react :as react])
  (:require-macros
    [cljs-tutorial.utils :refer [log jslog cljslog pause]]))


(defn deep-merge [& maps]
  (doseq [x maps] (assert (or (nil? x) (map? x)) (str "not a map: " x)))
  (apply
    merge-with
    (fn [x1 x2] (if (and (map? x1) (map? x2)) (deep-merge x1 x2) x2))
    maps))


(defn ->json-string [x]
  (js/JSON.stringify (clj->js x)))


(defn parse-json-string [x]
  (js->clj (js/JSON.parse x)))


(def cards (for [number [1 2 3]
                 color [:red :green :purple]
                 fill [:open :striped :solid]
                 shape [:oval :diamond :squiggle]]
             {:number number :color color :fill fill :shape shape}))


(defn is-set? [a b c]
  (->> [a b c]
       (map #(replace % [:number :fill :color :shape]))
       (apply map list)
       (map (comp count distinct))
       (not-any? (partial = 2))))


(defn combinations [size coll]
  (if (= size 1)
    (map vector coll)
    (apply concat
           (map-indexed
             (fn [i elem]
               (map (fn [x] (conj x elem))
                    (combinations (dec size) (drop (inc i) coll))))
             coll))))


(defn find-sets [cards]
  (filter (partial apply is-set?)
          (combinations 3 cards)))


(defn with-index [coll]
  (map vector (range) coll))
