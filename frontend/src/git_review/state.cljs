(ns git-review.state
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async]
            [clojure.string :as string]
            [rum.core :as rum]))

(defn history-cursor [app-state]
  (rum/cursor-in app-state [:history]))

(defn commit-cursor [app-state]
  (rum/cursor-in app-state [:commit]))


(defmulti handle-event (fn [state event] (first event)))

(defmethod handle-event :default [state event]
  (println "No handler defined for event:" event)
  state)

(defn summarize-commit [commit]
  (let [{:keys [message]} commit]
    (assoc commit :message (first (string/split message #"\n")))))

(defn summarize-history [history]
  (mapv summarize-commit history))

(defmethod handle-event :initial-history-ready [state [_ initial-history]]
  (assoc state :history (summarize-history initial-history)))

(defmethod handle-event :more-history-ready [state [_ more-history]]
  (let [current-history (:history state)]
    (assoc state :history (apply conj current-history
                                 (summarize-history more-history)))))

(defmethod handle-event :commit-details-ready [state [_ commit-details]]
  (assoc state :commit commit-details))

(defn process-events [c app-state handle-event handle-event-post]
  (go (while true
        (let [event (<! c)]
          (swap! app-state handle-event event)
          (when handle-event-post (handle-event-post @app-state))))))


(def history-query
  (str "query History($hash: String){"
       "history(first: 10, after: $hash){"
       "date "
       "hash "
       "message "
       "author { name email }"
       "}"
       "}"))

(defn load-initial-history [c]
  (async/put! c [:initial-history-pending])
  (async/take! (http/post "http://localhost:8080/graphql"
                          {:with-credentials? false
                           :json-params {:query history-query}})
               (fn [response]
                 (async/put! c [:initial-history-ready
                                (get-in response [:body :data :history])]))))

(defn load-more-history [c hash]
  (async/put! c [:more-history-pending])
  (async/take! (http/post "http://localhost:8080/graphql"
                          {:with-credentials? false
                           :json-params {:query history-query
                                         :variables {:hash hash}}})
               (fn [response]
                 (async/put! c [:more-history-ready
                                (get-in response [:body :data :history])]))))

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

(defn load-commit-details [c hash]
  (async/put! c [:commit-details-pending])
  (async/take! (http/post "http://localhost:8080/graphql"
                          {:with-credentials? false
                           :json-params {:query diff-query
                                         :variables {:hash hash}}})
               (fn [response]
                 (async/put! c [:commit-details-ready
                                (get-in response [:body :data :commit])]))))

