(ns git-review.core
  (:require [clojure.string :as string]
            [git-review.state :refer [commit-history current-commit load-diff-from-api load-history-from-api load-more-history-from-api]]
            [git-review.crypt :as crypt]
            [cljs-http.client :as http]
            [goog.dom :as dom]
            [rum.core :as rum]))

(enable-console-print!)

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
    [:li {:on-click (fn [_] (load-diff-from-api hash))}
     (avatar author)
     [:.commit-details
      [:.commit-msg message]
      [:.commit-hash hash]
      [:.commit-date (format-date date)]]]))

(rum/defc commit-list <
  rum/reactive
  {:will-mount (fn [state] (load-history-from-api) state)}
  []
  [:.commit-list
   (let [history (rum/react (commit-history))]
     (if (empty? history)
       [:p "history is empty"]
       [:.commit-list-entries
        [:ul (mapv commit-entry history)]
        [:.show-more {:on-click (fn [_] (load-more-history-from-api ((last history):hash)))} "show more"]
       ]
     )
   )
  ]
 )


(rum/defc change-diff < {:key-fn (fn [change] (:action change))}
  [change]
  [:.change {:dangerouslySetInnerHTML
         {:__html (.getPrettyHtml js/Diff2Html
                                  (:diff change)
                                  (clj->js {:outputFormat "side-by-side"}))}}])

(rum/defc commit-diff <
  rum/reactive
  []
  (let [commit (rum/react (current-commit))]
    [:.diffs
     (when commit
       (map change-diff (:changes commit)))]))

(rum/defc app []
  [:div.layout
   [:header "git-review"]
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
