(ns glados.adapters.updates
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn update->trigger
  [update]
  (cond-> {}
    (contains? update :message)
    (assoc :message (select-keys (:message update) [:date :text]))

    (contains? update :callback_query)
    (assoc :callback-response (get-in update [:callback_query :data])
           :chat-id (get-in update [:callback_query :message :chat :id])
           :message-id (get-in update [:callback_query :message :message_id])
           :text (get-in update [:callback_query :message :text])
           :entity (edn/read-string (get-in update [:callback_query :message :text])))))

(defn gemini->json [response]
  (let [text (-> response :body :candidates first :content :parts first :text)
        removed-markdown (str/replace text #"```json\n?|```" "")]
    (json/parse-string removed-markdown true)))

(defn gemini->sql
  [response]
  (let [text (-> response :body :candidates first :content :parts first :text)]
    (str/replace text #"```sql\n?|```" "")))

(defn gemini->text
  [response]
  (-> response :body :candidates first :content :parts first :text))