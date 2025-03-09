(ns glados.diplomat.http-out.gemini
  (:require [clj-http.client :as client]
            [clj-time.core :as time]
            [cheshire.core :as json]
            [glados.config :as config]
            [glados.prompts :as prompts]
            [glados.adapters.updates :as adapters.updates]))

(defn now []
  (let [zone (time/time-zone-for-offset -3)]
    (time/to-time-zone (time/now) zone)))

(defn natural-language->expense
  "Convert natural language to a expense entity."
  [{:keys [text]}]
  (let [prompt (format prompts/structured-expense (now) text)
        payload {:contents [{:parts [{:text prompt}]}]}
        response (client/post (:api-url config/gemini)
                              {:query-params {"key" (:api-key config/gemini)}
                               :as :json
                               :body (json/generate-string payload)})]
    (adapters.updates/gemini->json response)))

(defn new-entry-or-question?
  "Determines if the text is a new entry or a question."
  [{:keys [text]}]
  (let [prompt (format prompts/entry-or-question text)
        payload {:contents [{:parts [{:text prompt}]}]}
        response (client/post (:api-url config/gemini)
                              {:query-params {"key" (:api-key config/gemini)}
                               :as :json
                               :body (json/generate-string payload)})]
    (adapters.updates/gemini->json response)))

(defn querier
  "Gives a question, it buils a sqlite-compatible query."
  [{:keys [text]}]
  (let [prompt (format prompts/querier text (now) text)
        payload {:contents [{:parts [{:text prompt}]}]}
        response (client/post (:api-url config/gemini)
                              {:query-params {"key" (:api-key config/gemini)}
                               :as :json
                               :body (json/generate-string payload)})]
    (adapters.updates/gemini->sql response)))

(defn humanize
  "Given a gemini query, sqlite response, current date and a question, it humanizes the response."
  [{:keys [text]} query response]
  (let [prompt (format prompts/humanize query response (now) text)
        _ (println prompt)
        payload {:contents [{:parts [{:text prompt}]}]}
        response (client/post (:api-url config/gemini)
                              {:query-params {"key" (:api-key config/gemini)}
                               :as :json
                               :body (json/generate-string payload)})]
    (adapters.updates/gemini->text response)))