(ns git-review.core
  (:require [garden.def :refer [defstyles]]))

(def header-styles
  [[:header {:height "3em"
             :display "flex"
             :justify-content "flex-end"
             :align-items "center"}]
   [:.logo {:font-family "monospace"
            :font-size "1.2rem"
            :margin-left "0.5rem"}]])

(def footer-styles
  [:footer {:font-size "0.8rem"
            :text-align "right"}])

(def main-styles
  [:main {:margin "0.5rem 0"}])

(def review-view-styles
  [[:.review-view {:display "flex"}
    [:.commit-history {:display "block"
                       :width "20rem"}]
    [:.commit-details {:flex "1 1"
                       :background "#ffffff"
                       :padding "0.8em"
                       :border-radius "10px"
                       :min-width 0}]]])

(def avatar-styles
  [:.avatar :img {:width "100%"
                  :height "100%"}])

(def commit-summary-avatar-size "2.8em")
(def commit-summary-styles
  [[:.commit-summary.active {:background "#ffffff"
                             :border-radius "5px 0 0 5px"}]
   [:.commit-summary {:display "grid"
                      :grid-template-columns (str commit-summary-avatar-size " 1fr")
                      :grid-column-gap "0.5em"
                      :padding "0.2em"}
    [:.avatar {:grid-column "1"
               :grid-row "1/3"
               :width commit-summary-avatar-size
               :height commit-summary-avatar-size}]
    [:.message {:grid-column "2"
                :grid-row "1"
                :align-self "center"
                :font-weight "bold"
                :overflow-x "hidden"
                :text-overflow "ellipsis"
                :white-space "nowrap"}]
    [:.date {:grid-column "2"
             :grid-row "2"
             :align-self "center"
             :font-size "0.9em"
             :font-style "italic"}]]])

(def commit-list-styles
  [:.commit-list
   [:ul {:list-style "none"
         :padding 0
         :margin-top "0.4em"}]
   [:li {:margin "0.2em 0"
         :font-size "0.9em"}]])

(def commit-header-avatar-size "3.8em")
(def commit-header-styles
  [[:.commit-header {:display "grid"
                      :grid-template-columns (str commit-header-avatar-size " auto 1fr")
                      :grid-column-gap "0.5em"
                      :margin-bottom "1em"}
    [:.avatar {:grid-column "1"
               :grid-row "1/4"
               :width commit-header-avatar-size
               :height commit-header-avatar-size}]
    [:.summary-message {:grid-column "2/4"
                        :grid-row "1"
                        :align-self "center"
                        :font-weight "bold"}]
    [:.long-message {:grid-column "2/4"
                     :grid-row "3"
                     :align-self "center"}
     [:p {:margin "0.5em 0"}]]
    [:.date {:grid-column "3"
             :grid-row "2"
             :align-self "center"
             :font-size "0.9em"
             :font-style "italic"}]
    [:.author {:grid-column "2"
               :grid-row "2"
               :align-self "center"
               :justify-self "start"}]]])

(defstyles main
  [:* {:margin 0}]
  [:body {:background "#efefef"}]
  [:.container {:margin "2em"}]
  header-styles
  footer-styles
  main-styles
  avatar-styles
  review-view-styles
  commit-summary-styles
  commit-list-styles
  commit-header-styles)
