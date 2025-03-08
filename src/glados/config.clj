(ns glados.config)

(def telegram
  {:token   (System/getenv "TELEGRAM_TOKEN")
   :chat-id (System/getenv "TELEGRAM_CHAT_ID")})

(def gemini
  {:api-url (System/getenv "GEMINI_URL")
   :api-key (System/getenv "GEMINI_API_KEY")})