(ns git-review.state
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as string]
            [cljs-http.client :as http]
            [cljs.core.async :as async]
            [rum.core :as rum]))

(defonce app-state (atom {:history []
                          :commit nil}))
(def history-cursor (rum/cursor-in app-state [:history]))
(def commit-cursor (rum/cursor-in app-state [:commit]))

(defn commit-history []
  history-cursor)

(defn current-commit []
  commit-cursor)

(defn process-events [c app-state handle-event handle-event-post]
  (go (while true
        (let [event (<! c)]
          (swap! app-state handle-event event)
          (when handle-event-post (handle-event-post @app-state))))))


(defn summarize-commit [commit]
  (let [{:keys [message]} commit]
    (assoc commit :message (first (string/split message #"\n")))))

(defn summarize-history [history]
  (map summarize-commit history))

(defn update-history-state [c state]
  (go
    (let [{:keys [status body]} (async/<! c)
          history (get-in body [:data :history])]
      (swap! state assoc :history (summarize-history history)))))

(defn append-history-state [c state]
  (go
    (let [{:keys [status body]} (async/<! c)
          history (get-in body [:data :history])
          history (summarize-history history)
          oldHistory (get-in @state [:history])
          combinedHistory (concat oldHistory history)
          ]
      (swap! state assoc :history combinedHistory))))

(defn update-diff-state [c state]
  (go
    (let [{:keys [status body]} (async/<! c)
          commit (get-in body [:data :commit])]
      (swap! state assoc :commit commit))))

(def history-query
  (str "query History($hash: String){"
       "history(first: 10, after: $hash){"
       "date "
       "hash "
       "message "
       "author { name email }"
       "}"
       "}"))

(defn load-history-from-api []
  (-> (http/post "http://localhost:8080/graphql"
            {:with-credentials? false
             :json-params {:query history-query}})
      (update-history-state app-state)))

(defn load-more-history-from-api [hash]
  (-> (http/post "http://localhost:8080/graphql"
            {:with-credentials? false
             :json-params {:query history-query
                           :variables {:hash hash}}})
      (append-history-state app-state)))

(def diff-query
  (str "query CommitWithDiff($hash: String!){"
       "commit(Hash: $hash){"
       "date "
       "hash "
       "message "
       "author { name email }"
       "changes { diff action }"
       "}"
       "}"))

(defn load-diff-from-api [hash]
  (-> (http/post "http://localhost:8080/graphql"
            {:with-credentials? false
             :json-params {:query diff-query
                           :variables {:hash hash}}})
      (update-diff-state app-state)))
