(ns glados.core
  (:gen-class)
  (:require [glados.controllers.expenses :as controllers.expenses])
  (:import [java.util Timer TimerTask]))

(defn updates-task []
  (controllers.expenses/updates))

(defn schedule-updates []
  (let [timer (Timer.)
        task (proxy [TimerTask] []
               (run []
                 (updates-task)))]
    (.schedule timer task 0 1000)))

(defn -main [& args]
  (schedule-updates)
  (println "Worker iniciado. Executando tarefa a cada 10 segundos..."))
