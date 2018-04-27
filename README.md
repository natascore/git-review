# git-review

## Develop the frontend

The frontend is written in [ClojureScript](https://clojurescript.org) and uses [Leiningen](https://leiningen.org) as a build tool. You should run all `lein` commands inside the frontend directory.

    cd frontend

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `frontend/resources/public/index.html`. You will not
get live reloading, nor a REPL. 
