(ns git-review.core
  (:require [cljs.core.async :refer [chan]]
            [cljsjs.showdown]
            [clojure.string :as string]
            [git-review.state :as state]
            [git-review.ui :as ui]
            [goog.dom :as dom]
            [rum.core :as rum]))

(enable-console-print!)

;; create state plumbing

(defonce app-state (atom {}))
(def event-bus (chan))
(def git-history (state/history-cursor app-state))
(def current-commit (state/commit-cursor app-state))
(def pending? (state/pending-cursor app-state))

(defn load-initial-history []
  (state/load-initial-history event-bus))

(defn load-more-history []
  (state/load-more-history event-bus (:hash (last @git-history))))

(defn load-commit-details [hash]
  (state/load-commit-details event-bus hash))

(state/process-events
  event-bus
  app-state
  state/handle-event
  nil)

;; connect UI to state
(rum/defc commit-details < rum/reactive []
  (ui/commit-details (rum/react current-commit)))

(rum/defc commit-list <
  rum/reactive
  {:init (fn [state props] (load-initial-history) state)}
  []
  (ui/commit-list (rum/react git-history) {:active-commit (:hash (rum/react current-commit))
                                           :on-select load-commit-details
                                           :on-show-more load-more-history}))

(rum/defc page-layout < rum/reactive [view]
  (ui/page-layout view (rum/react pending?)))


(rum/mount
  (page-layout
    (ui/review-view
      (commit-list)
      (commit-details)))
  (dom/getElement "app"))

(defn ^:export inspect-state []
  (clj->js @app-state))

(defn on-js-reload []
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
