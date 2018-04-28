(ns git-review.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as string]
            [git-review.crypt :as crypt]
            [cljs-http.client :as http]
            [cljs.core.async :as async]
            [goog.dom :as dom]
            [rum.core :as rum]))

(enable-console-print!)

(defonce app-state (atom {:history []}))

(defn format-date [isodate]
  (-> isodate
    (string/replace "T" " ")
    (string/replace #":[0-9]{2}Z$" "")))

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
    [:li
     (avatar author)
     [:.commit-details
      [:.commit-msg message]
      [:.commit-hash hash]
      [:.commit-date (format-date date)]]]))


(defn summarize-commit [commit]
  (let [{:keys [message]} commit]
    (assoc commit :message (first (string/split message #"\n")))))

(defn summarize-history [history]
  (map summarize-commit history))

(defn update-state [c state]
  (go
    (let [{:keys [status body]} (async/<! c)
          history (get-in body [:data :history])]
      (reset! state {:history (summarize-history history)}))))

(def api-query
  (str "query {"
  "history(first: 10){"
  "date "
  "hash "
  "message "
  "author { name email }"
  "}"
  "}"))

(defn load-from-api []
  (-> (http/post "http://localhost:8080/graphql"
            {:with-credentials? false
             :json-params {:query api-query}})
      (update-state app-state)))

(rum/defc commit-list <
  rum/reactive
  {:will-mount (fn [state] (load-from-api) state)}
  []
  [:.commit-list
   (let [history (:history (rum/react app-state))]
     (if (empty? history)
       [:p "history is empty"]
       [:ul (mapv commit-entry history)]))])

(rum/defc app []
  [:div.layout
   [:header "git-review"]
   [:main (commit-list)]
   [:footer "visit us on " [:a {:href "https://github.com/natascore/git-review"} "GitHub"]]])


(rum/mount
  (app)
  (dom/getElement "app"))


(defn on-js-reload []
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
