package handler

import (
	"encoding/json"
	"io/ioutil"
	"net/http"

	graphql "github.com/graph-gophers/graphql-go"
	"github.com/natascore/git-review/backend/resolver"
)

func getSchema(path string) (string, error) {
	b, err := ioutil.ReadFile(path)
	if err != nil {
		return "", err
	}

	return string(b), nil
}

// The GraphQLHandler handler handles GraphQL API requests over HTTP.
type GraphQLHandler struct {
}

func (h GraphQLHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	var params struct {
		Query         string                 `json:"query"`
		OperationName string                 `json:"operationName"`
		Variables     map[string]interface{} `json:"variables"`
	}
	if err := json.NewDecoder(r.Body).Decode(&params); err != nil {
		http.Error(w, err.Error(), http.StatusBadRequest)
		return
	}

	s, err := getSchema("./schema/schema.graphql")
	if err != nil {
		panic(err)
	}

	schema := graphql.MustParseSchema(s, &resolver.Resolver{})

	var ctx = r.Context()

	response := schema.Exec(ctx, params.Query, params.OperationName, params.Variables)
	responseJSON, err := json.Marshal(response)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Write(responseJSON)
}
