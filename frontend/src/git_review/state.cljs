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

(defn pending-cursor [app-state]
  (rum/cursor-in app-state [:pending]))

(defmulti handle-event (fn [state event] (first event)))

(defmethod handle-event :default [state event]
  (println "No handler defined for event:" event)
  state)

(defn set-pending [state]
  (assoc state :pending true))

(defn clear-pending [state]
  (dissoc state :pending))

(defmethod handle-event :initial-history-pending [state _]
  (set-pending state))

(defmethod handle-event :more-history-pending [state _]
  (set-pending state))

(defmethod handle-event :commit-details-pending [state _]
  (set-pending state))

(defn summarize-commit [commit]
  (let [{:keys [message]} commit]
    (assoc commit :message (first (string/split message #"\n")))))

(defn summarize-history [history]
  (mapv summarize-commit history))

(defmethod handle-event :initial-history-ready [state [_ initial-history]]
  (-> state
    (assoc :history (summarize-history initial-history))
    (clear-pending)))

(defmethod handle-event :more-history-ready [state [_ more-history]]
  (let [current-history (:history state)]
    (-> state
        (assoc :history (apply conj current-history
                               (summarize-history more-history)))
        (clear-pending))))

(defmethod handle-event :commit-details-ready [state [_ commit-details]]
  (-> state
      (assoc :commit commit-details)
      (clear-pending)))

(defn process-events [c app-state handle-event handle-event-post]
  (go (while true
        (let [event (<! c)]
          (swap! app-state handle-event event)
          (when handle-event-post (handle-event-post @app-state))))))


(defn graphql-query [c query opts]
  (let [{:keys [variables pre-event post-event post]} opts]
    (when pre-event (async/put! c [pre-event]))
    (async/take! (http/post "http://localhost:8080/graphql"
                          {:with-credentials? false
                           :json-params {:query query
                                         :variables variables}})
               (fn [response]
                 (when post-event
                 (async/put! c (if post [post-event (post response)]
                                 [post-event])))))))

(def history-query
  (str "query History($hash: String){"
       "history(first: 10, after: $hash){"
       "date "
       "hash "
       "message "
       "author { name email }"
       "}"
       "}"))

(defn response->history [response]
  (get-in response [:body :data :history]))

(defn load-initial-history [c]
  (graphql-query c history-query {:pre-event :initial-history-pending
                                  :post-event :initial-history-ready
                                  :post response->history}))

(defn load-more-history [c hash]
  (graphql-query c history-query {:variables {:hash hash}
                                  :pre-event :more-history-pending
                                  :post-event :more-history-ready
                                  :post response->history}))

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
  (graphql-query c diff-query {:variables {:hash hash}
                               :pre-event :commit-details-pending
                               :post-event :commit-details-ready
                               :post (fn [response]
                                       (get-in response [:body :data :commit]))}))

