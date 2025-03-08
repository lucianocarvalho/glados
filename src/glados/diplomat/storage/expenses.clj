(ns glados.diplomat.storage.expenses
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec
  {:dbtype "sqlite"
   :dbname "/app/sqlite/glados.db"})

(defn insert!
  [expense]
  (jdbc/insert! db-spec :expenses expense))

(defn select
  [query]
  (into [] (jdbc/query db-spec [query])))