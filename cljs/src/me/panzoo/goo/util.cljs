(ns me.panzoo.goo.util
  (:require
    [clojure.string :as string]
    [goog.dom :as dom]))

(defn text [s]
  (dom/createTextNode s))

(defn node [tag & [attrs & children]]
  (apply
    dom/createDom
    tag
    (when attrs (.strobj (into {} (for [[k v] attrs] [(name k) v]))))
    (map #(if (string? %) (text %) %) children)))

;; CSS
;; TODO move to own project

(defn css
  ([[selector & rules]]
   (str
     (string/join
       "," (map name
                (if (and (not (string? selector))
                         (seq? selector))
                  selector
                  [selector])))
     "{"
     (string/join
       ";" (for [[attr value] (partition 2 rules)]
             (str (name attr) ":" value)))
     "}"))
  ([& decls]
   (string/join (map css decls))))
