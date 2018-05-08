(defproject git-review "0.1.0-SNAPSHOT"
  :description "reviews with git made easy"
  :min-lein-version "2.7.1"

  :dependencies [[cljs-http "0.1.45"]
                 [cljsjs/showdown "1.4.2-0"]
                 [devcards "0.2.4"]
                 [garden "1.3.5"]
                 [lein-doo "0.1.10"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async  "0.4.474"]
                 [rum "0.11.2"]]

  :plugins [[lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]
            [lein-doo "0.1.10"]
            [lein-figwheel "0.5.15"]
            [lein-garden "0.3.0"]]

  :source-paths ["src"]

  :garden {:builds [{:id "main"
                     :source-paths ["src/styles"]
                     :stylesheet git-review.core/main
                     :compiler {:output-to "resources/public/css/main.css"
                                :pretty-print? false}}]}

  :cljsbuild {:test-commands {"test" ["lein" "doo" "phantom" "test" "once"]}
              :builds
              {:dev
               {:source-paths ["src"]
                :figwheel {:on-jsload "git-review.core/on-js-reload"
                           :open-urls ["http://localhost:3449/index.html"]}
                :compiler {:main git-review.core
                           :externs ["resources/lib/diff2html.min.js"]
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/git_review.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               :min
               {:source-paths ["src"]
                :compiler {:main git-review.core
                           :externs ["resources/lib/diff2html.min.js"]
                           :optimizations :advanced
                           :output-to "resources/public/js/compiled/git_review.min.js"
                           :pretty-print false}}
               :devcards-test
               {:source-paths ["src" "test"]
                :figwheel {:devcards true}
                :compiler {:main runners.browser
                           :optimizations :none
                           :asset-path "js/compiled/devcards/out"
                           :output-dir "resources/public/js/compiled/devcards/out"
                           :output-to "resources/public/js/compiled/devcards/tests.js"
                           :source-map-timestamp true}}
               :test
               {:source-paths ["src" "test"]
                :compiler {:main runners.doo
                           :optimizations :none
                           :output-dir "resources/private/js/out"
                           :output-to "resources/private/js/tests.js"}}}}
  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this

             ;; doesn't work for you just run your own server :) (see lein-ring)

             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you are using emacsclient you can just use
             ;; :open-file-command "emacsclient"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"

             ;; to pipe all the output to the repl
             ;; :server-logfile false
             }


  ;; Setting up nREPL for Figwheel and ClojureScript dev
  ;; Please see:
  ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl
  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.9"]
                                  [figwheel-sidecar "0.5.15"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   ;; need to add the compliled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
