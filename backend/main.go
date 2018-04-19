package main

import (
	"log"
	"net/http"
	"strings"
)

func sayHello(w http.ResponseWriter, r *http.Request) {
	message := r.URL.Path
	message = strings.TrimPrefix(message, "/")
	message = "Hello " + message
	w.Write([]byte(message))
}
func main() {
	mux := http.NewServeMux()

	mux.HandleFunc("/", sayHello)

	log.Printf("About to listen on :8080: Go to http://127.0.0.1:8080/")
	err := http.ListenAndServe(":8080", mux)
	log.Fatal(err)
}
