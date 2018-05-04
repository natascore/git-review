(ns git-review.state-test
  (:require [cljs.core.async :refer [chan put!]]
            [git-review.state :refer [process-events
                                      handle-event]]
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
               (fn [state event]
                 (is (= {:a 1} state)))
               (fn [_] (done)))
             (put! c [])))))

(deftest process-events-calls-handler-with-event-test
  (testing "process-events calls event-handler with event"
    (async done
           (let [c (chan)
                 app-state (atom {:a 1})]
             (process-events
               c
               app-state
               (fn [state event]
                 (is (= :event event)))
               (fn [_] (done)))
             (put! c :event)))))

(deftest process-events-replaces-state-with-handler-return-value-test
  (testing "process-events replaces state with value returned by event handler"
    (async done
           (let [c (chan)
                 app-state (atom {})
                 handle-event (fn [state event] [:a])]
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
               (fn [state event]
                 (is (= :event-value)))
               (fn [_]
                 (done)))
             (put! c :event-value)))))

(deftest process-events-passes-new-state-to-post-processor-test
  (testing "process-events passes new state to handle-event-post"
    (async done
           (let [c (chan)
                 app-state (atom {})
                 event-handler (fn [state event] {:new-state 42})]
             (process-events
               c
               app-state
               event-handler
               (fn [state]
                 (is (= {{:new-state 42} state}))
                 (done)))
             (put! c [])))))

(deftest handle-event-initial-history-ready
  (testing "handle-event :initial-history-ready fills state with initial history"
    (let [state {}
          history [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                    :date "2018-05-02T21:50:15+02:00"
                    :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                    :message "fix: fix bug #1234"}]]
      (is (= {:history
              [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                :date "2018-05-02T21:50:15+02:00"
                :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                :message "fix: fix bug #1234"}]}
             (handle-event state [:initial-history-ready history]))))))

(deftest handle-event-initial-history-ready-summarizes-commit-message
  (testing "handle-event :initial-history-ready summarizes commit message"
    (let [state {}
          history [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                    :date "2018-05-02T21:50:15+02:00"
                    :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                    :message "fix: fix bug #1234\n\nMuch more details...\n"}]]
      (is (= {:history
              [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                :date "2018-05-02T21:50:15+02:00"
                :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                :message "fix: fix bug #1234"}]}
             (handle-event state [:initial-history-ready history]))))))

(deftest handle-event-initial-history-ready-leaves-other-keys-in-state-untouched
  (testing "handle-event :initial-history-ready leaves other keys in state untouched"
    (let [state {:other-key "other-value"}
          history [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                    :date "2018-05-02T21:50:15+02:00"
                    :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                    :message "fix: fix bug #1234\n\nMuch more details...\n"}]]
      (is (= "other-value"
             (:other-key (handle-event state [:initial-history-ready history])))))))


(deftest handle-event-more-history-ready-appends-to-history
  (testing "handle-event :more-history-ready appends to history"
    (let [state {:history [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                            :date "2018-05-03T21:50:15+02:00"
                            :hash "9de92ba351aba39de83db9c0501ef7fcad5e8b32"
                            :message "feat: add cool new feature"}]}
          more-history [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                         :date "2018-05-02T21:50:15+02:00"
                         :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                         :message "fix: fix bug #1234\n\nMuch more details...\n"}]]
      (is (= {:history
              [{:author {:name "Arthur Author", :email "arthur@acme.org"}
                :date "2018-05-03T21:50:15+02:00"
                :hash "9de92ba351aba39de83db9c0501ef7fcad5e8b32"
                :message "feat: add cool new feature"}
               {:author {:name "Arthur Author", :email "arthur@acme.org"}
                :date "2018-05-02T21:50:15+02:00"
                :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                :message "fix: fix bug #1234"}]}
             (handle-event state [:more-history-ready more-history]))))))

(deftest handle-event-commit-details-ready
  (testing "handle-event :commit-details-ready sets the current commit"
    (let [state {:commit nil}
          commit {:author {:name "Arthur Author", :email "arthur@acme.org"}
                  :date "2018-05-02T21:50:15+02:00"
                  :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                  :message "fix: fix bug #1234\n\nMuch more details...\n"}]
      (is (= {:commit {:author {:name "Arthur Author", :email "arthur@acme.org"}
                       :date "2018-05-02T21:50:15+02:00"
                       :hash "69885b60acda3c90fe3f3109012cf9f8f5953739"
                       :message "fix: fix bug #1234\n\nMuch more details...\n"}}
             (handle-event state [:commit-details-ready commit]))))))

