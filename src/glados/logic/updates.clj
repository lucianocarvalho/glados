(ns glados.logic.updates)

(defn confirmation?
  [trigger]
  (= "yes" (:callback-response trigger)))

(defn rejection?
  [trigger]
  (= "no" (:callback-response trigger)))

(defn direct-message?
  [trigger]
  (contains? trigger :message))