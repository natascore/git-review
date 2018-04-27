(ns git-review.core
  (:require [clojure.string :as string]
            [git-review.crypt :as crypt]
            [goog.dom :as dom]
            [rum.core :as rum]))

(enable-console-print!)

(def history-fixture [{:hash "4b64c830fad432bbc465f2fb5bb2f63bac6a09db",
                       :author {:name "Oliver Esser", :email "oliver.esser@actano.de"},
                       :message "chore(backend): update Gopck.lock file",
                       :date "2018-04-25T07:25:21Z"}
                      {:hash "203167f101360eea5fb73a72a54a1a8eb556eb10",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message "feat(backend): add committer attribute",
                       :date "2018-04-23T07:57:42Z"}
                      {:hash "8f210b18161bb98262ce49a757c08377a30c4877",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message "feat(backend): add author to `commit`",
                       :date "2018-04-23T07:50:47Z"}
                      {:hash "34f72faefa32c651d907b8a23e9adcc6782b99d0",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message "feat(backend): add date to commit type",
                       :date "2018-04-23T07:34:32Z"}
                      {:hash "7a3130ee3052ad9e92a3004efd6012e00b82e4df",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message "feat(backend): adds message to commit type",
                       :date "2018-04-22T11:44:02Z"}
                      {:hash "37596210681ac5a8ec3c30103f05a38e362ba6b5",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message "feat(backend): retrieve history from current git dir",
                       :date "2018-04-22T11:42:40Z"}
                      {:hash "63c7a048c04b7a15d22cae134ee0c95973df9f7f",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message
                       "feat(backend): adds helper for checking errors and logging",
                       :date "2018-04-22T11:38:09Z"}
                      {:hash "1ad77d840a6b6fe3c53894204577372ec01ace18",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message
                       "feat(backend): create initial schema for retrieving commitlog",
                       :date "2018-04-21T07:08:34Z"}
                      {:hash "0e6d346ccf7b2c9db4430291880a9d4baeb9e2e3",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message "docs(backend): adds resolver comment for helloWorld",
                       :date "2018-04-21T06:50:01Z"}
                      {:hash "b2504fa399c2046ff688ca9f91442591c1d6a7a7",
                       :author {:name "Daniel Paschke", :email "paschdan@gmail.com"},
                       :message "style(backend): remove superfluous comment",
                       :date "2018-04-20T10:07:10Z"}])

(defonce app-state (atom {:history history-fixture}))

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

(rum/defc commit-list < rum/reactive []
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
