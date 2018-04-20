package main

import (
	"log"
	"net/http"

	"github.com/natascore/git-review/backend/handler"
)

func main() {
	mux := http.NewServeMux()

	mux.Handle("/graphql", handler.GraphQLHandler{})
	mux.Handle("/", handler.Playground{})

	log.Printf("About to listen on :8080: Go to http://127.0.0.1:8080/")
	err := http.ListenAndServe(":8080", mux)
	log.Fatal(err)
}
