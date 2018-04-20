M = $(shell printf "\033[34;1m▶\033[0m")

build: dep ; $(info $(M) Building project...)
		go build

dep: setup ; $(info $(M) Ensuring vendored dependencies are up-to-date...)
		dep ensure

setup: ; $(info $(M) Fetching github.com/golang/dep...)
		go get github.com/golang/dep/cmd/dep

run: ; $(info $(M) Starting development server...)
		go run main.go

.PHONY: build dep setup server
