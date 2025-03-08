(ns glados.diplomat.http-out.telegram
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http.client]
   [glados.config :as config]))

(defn get-updates
  [token offset]
  (-> (http.client/get (str "https://api.telegram.org/bot" token "/getUpdates")
                       {:query-params {"offset" offset}
                        :as :json})
      :body))

(defn send-message!
  [token text]
  (http.client/post (str "https://api.telegram.org/bot" token "/sendMessage")
                    {:form-params {:chat_id (:chat-id config/telegram) :text text}}))

(defn hide-confirmation-buttons!
  [token chat-id message-id text]
  (let [url (str "https://api.telegram.org/bot" token "/editMessageText")
        payload {:chat_id chat-id
                 :message_id message-id
                 :text text
                 :reply_markup nil}]
    (http.client/post url {:form-params payload :as :json})))

(defn send-confirmation-message!
  [token text]
  (let [url (str "https://api.telegram.org/bot" token "/sendMessage")
        inline-keyboard {:inline_keyboard [[{:text "Sim" :callback_data "yes"}
                                            {:text "Não" :callback_data "no"}]]
                         :resize_keyboard true
                         :one_time_keyboard true
                         :selective true}
        payload {:chat_id (:chat-id config/telegram)
                 :text text
                 :reply_markup (json/generate-string inline-keyboard)}]
    (http.client/post url {:form-params payload :as :json})))