(ns glados.controllers.expenses
  (:require [glados.config :as config]
            [glados.adapters.updates :as adapters.updates]
            [glados.diplomat.http-out.gemini :as http-out.gemini]
            [glados.logic.updates :as logic.updates]
            [glados.diplomat.storage.expenses :as storage.expenses]
            [glados.diplomat.http-out.telegram :as http-out.telegram]))

(def offset (atom 0))

(defn- save-and-feedback!
  "Sent a confirmation message when clicked the yes option."
  [{:keys [chat-id message-id text entity]}]
  (http-out.telegram/hide-confirmation-buttons! (get config/telegram :token) chat-id message-id text)
  (storage.expenses/insert! entity)
  (http-out.telegram/send-message! (get config/telegram :token) "Feito!"))

(defn- negative-feedback!
  "Gives the user a negative message when clicked the negative option."
  []
  (http-out.telegram/send-message! (get config/telegram :token) "Provavelmente errei na interpretação, escreva novamente..."))

(defn- answer!
  "It builds a query and returns a response based on a message."
  [message]
  (let [generated-query (http-out.gemini/querier message)
        response (storage.expenses/select generated-query)
        humanized-response (http-out.gemini/humanize message generated-query response)]
    (http-out.telegram/send-message! (get config/telegram :token) humanized-response)))

(defn- evaluate!
  "Evaluate a string message into a JSON response"
  [{:keys [message]}]
  (let [entry-or-question (http-out.gemini/new-entry-or-question? message)]
    (cond
      (= "question" (:type entry-or-question))
      (answer! message)

      (= "not-identified" (:type entry-or-question))
      (negative-feedback!)

      (= "entry" (:type entry-or-question))
      (let [expense (http-out.gemini/natural-language->expense message)]
        (cond
          (contains? expense :error)
          (http-out.telegram/send-message! (get config/telegram :token) (:error expense))

          :else
          (http-out.telegram/send-confirmation-message! (get config/telegram :token) expense))))))

(defn updates []
  (let [updates (http-out.telegram/get-updates (get config/telegram :token) @offset)
        last-update (last (:result updates))]

    (doseq [update (:result updates)]
      (let [trigger (adapters.updates/update->trigger update)]
        (cond
          (logic.updates/confirmation? trigger)
          (save-and-feedback! trigger)

          (logic.updates/rejection? trigger)
          (negative-feedback!)

          (logic.updates/direct-message? trigger)
          (evaluate! trigger))))

    (when last-update
      (swap! offset (constantly (inc (:update_id last-update)))))))

