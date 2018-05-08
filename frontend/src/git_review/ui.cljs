(ns git-review.ui
  (:require [cljsjs.showdown]
            [clojure.string :as string]
            [git-review.crypt :as crypt]
            [rum.core :as rum]))

(rum/defc spinner [spinning?]
  (when spinning?
    [:.spinner
     [:i.fa.fa-spinner.fa-spin]]))

(rum/defc header [pending?]
  [:header
   (spinner pending?)
   [:.logo "git-review"]])

(rum/defc footer []
  [:footer "visit us on " [:a {:href "https://github.com/natascore/git-review"} "GitHub"]])

(rum/defc page-layout [main pending?]
  [:.container
   (header pending?)
   [:main main]
   (footer)])

(rum/defc review-view [history details]
  [:.review-view
   [:.commit-history history]
   details])

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

(defn format-date [isodate]
  (-> isodate
    (string/replace "T" " @ ")
    (string/replace #":[0-9]{2}(Z|\+.*)$" "")))

(rum/defc commit-summary [commit active?]
  (let [{:keys [author date message]} commit
        base-class "commit-summary"
        classname (if active?
                    [base-class "active"]
                    [base-class])]
    [:div {:class classname}
     (avatar author)
     [:.message message]
     [:.date (format-date date)]]))

(rum/defc commit-list [history {:keys [active-commit on-select on-show-more]}]
  [:.commit-list
   [:ul
    (mapv (fn [commit]
            (let [{:keys [hash]} commit]
              [:li {:key hash
                    :on-click (fn [_]
                                (on-select hash))}
               (commit-summary commit (= active-commit hash))]))
          history)]
   [:button.show-more {:on-click (fn [_] (on-show-more))} "show more"]])

(rum/defc markdown-message
  [classname message]
  (let [converter (js/showdown.Converter.)
        html (.makeHtml converter message)]
    [(keyword (str "." classname)) {:dangerouslySetInnerHTML
                                    {:__html html}}]))

(rum/defc commit-header [commit]
  (let [{:keys [author date hash message]} commit
        summary-message (first (string/split-lines message))
        long-message (string/trim (string/join "\n" (rest (string/split message #"\n"))))]
    [:.commit-header
     (avatar author)
     [:.date (str "on " (format-date date))]
     [:.author (:name author)]
     [:.summary-message summary-message]
     (markdown-message "long-message" long-message)]))

(rum/defc diff-view < {:key-fn :key}
  [{:keys [classname diff-text side-by-side?]}]
  [(keyword (str "." classname))
   {:dangerouslySetInnerHTML
    {:__html (.getPrettyHtml js/Diff2Html
                             diff-text
                             (clj->js {:outputFormat (if side-by-side?
                                                       "side-by-side"
                                                       "line-by-line")
                                       :showFiles true}))}}])

(rum/defc commit-changes [changes]
  [:.commit-changes
   (when changes
     (mapv (fn [change]
             (diff-view {:key (:action change)
                         :classname  "diff"
                         :diff-text (:diff change)
                         :side-by-side? false}))
           changes))])

(rum/defc commit-details [commit]
  (if commit
    [:.commit-details
     (commit-header commit)
     (commit-changes (:changes commit))]
    [:.commit-details]))
