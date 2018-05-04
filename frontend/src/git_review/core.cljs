(ns git-review.core
  (:require [cljs.core.async :refer [chan]]
            [cljsjs.showdown]
            [clojure.string :as string]
            [git-review.crypt :as crypt]
            [git-review.state :as state]
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

(defn load-more-history [hash]
  (state/load-more-history event-bus hash))

(defn load-commit-details [hash]
  (state/load-commit-details event-bus hash))

(state/process-events
  event-bus
  app-state
  state/handle-event
  nil)

;; UI related stuff from here on out

(defn format-date [isodate]
  (-> isodate
    (string/replace "T" " ")
    (string/replace #":[0-9]{2}(Z|\+.*)$" "")))

(defn gravatar-url [email]
  (str
    "https://gravatar.com/avatar/"
    (crypt/md5 (string/trim email))
    "?s=64"))

(rum/defc avatar [user]
  (let [{:keys [name email]} user]
    [:.avatar
     [:img
      {:src (gravatar-url email)
       :alt name}]]))

(rum/defc commit-entry [entry]
  (let [{:keys [hash author message date]} entry]
    [:li {:on-click (fn [_] (load-commit-details hash))}
     (avatar author)
     [:.commit-details
      [:.commit-msg message]
      [:.commit-hash hash]
      [:.commit-date (format-date date)]]]))

(rum/defc commit-list <
  rum/reactive
  {:will-mount (fn [state] (load-initial-history) state)}
  []
  [:.commit-list
   (let [history (rum/react git-history)
         last-commit (:hash (last history))]
     (if (empty? history)
       [:p "history is empty"]
       [:.commit-list-entries
        [:ul (mapv commit-entry history)]
        [:.show-more
         {:on-click (fn [_] (load-more-history last-commit))}
         "show more"]]))])


(rum/defc change-diff < {:key-fn (fn [change] (:action change))}
  [change]
  [:.change {:dangerouslySetInnerHTML
             {:__html (.getPrettyHtml js/Diff2Html
                                      (:diff change)
                                      (clj->js {:outputFormat "line-by-line"
                                                :showFiles true}))}}])

(rum/defc markdown-message
  [message]
  (let [converter (js/showdown.Converter.)
        markdown (.makeHtml converter (str "## " message))]
    [:.message {:dangerouslySetInnerHTML
                {:__html markdown}}]))

(rum/defc commit-diff <
  rum/reactive
  []
  (let [commit (rum/react current-commit)
        {:keys [message author date hash changes]} commit]
    [:.diffs
     [:div
      (markdown-message message)
      [:.author (avatar author) (:name author)]
      [:.date (format-date date)]
      [:.hash hash]]
     (when commit
       (map change-diff changes))]))

(rum/defc spinner <
  rum/reactive
  []
  (when (rum/react pending?)
    [:.spinner
     [:i.fa.fa-spinner.fa-spin]]))


(rum/defc app []
  [:div.layout
   [:header (spinner) "git-review"]
   [:main
    (commit-list)
    (commit-diff)]
   [:footer "visit us on " [:a {:href "https://github.com/natascore/git-review"} "GitHub"]]])

(rum/mount
  (app)
  (dom/getElement "app"))

(defn on-js-reload []
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
