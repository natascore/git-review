(ns git-review.state-test
  (:require [cljs.core.async :refer [chan put!]]
            [git-review.state :refer [process-events]]
            [cljs.test :refer-macros [is testing async]]
            [devcards.core :refer-macros [deftest]]))

(deftest process-events-calls-handler-with-state-test
  (testing "process-events calls event-handler with current state"
    (async done
           (let [c (chan)
                 app-state (atom {:a 1})]
             (process-events
               c
               app-state
               (fn [state]
                 (is (= {:a 1} state)))
               (fn [_] (done)))
             (put! c [])))))

(deftest process-events-replaces-state-with-handler-return-value-test
  (testing "process-events replaces state with value returned by event handler"
    (async done
           (let [c (chan)
                 app-state (atom {})
                 handle-event (fn [_] [:a])]
             (process-events
               c
               app-state
               handle-event
               (fn [_]
                 (is (= [:a] @app-state))
                 (done)))
             (put! c [])))))

(deftest process-events-passes-event-to-event-handler-test
  (testing "process-events passes event from channel to event handler"
    (async done
           (let [c (chan)
                 app-state (atom {})]
             (process-events
               c
               app-state
               (fn [event]
                 (is (= :event-value)))
               (fn [_]
                 (done)))
             (put! c :event-value)))))

(deftest process-events-passes-new-state-to-post-processor-test
  (testing "process-events passes new state to handle-event-post"
    (async done
           (let [c (chan)
                 app-state (atom {})
                 event-handler (fn [_] {:new-state 42})]
             (process-events
               c
               app-state
               event-handler
               (fn [state]
                 (is (= {{:new-state 42} state}))
                 (done)))
             (put! c [])))))

