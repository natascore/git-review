package resolver

import (
	"time"

	helper "github.com/natascore/git-review/backend/helper"
	git "gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing/object"
)

type commitResolver struct {
	commit *object.Commit
}

// GetHistory QueryResolver for GetHistory()
func (r *Resolver) GetHistory() *[]*commitResolver {

	repo, err := git.PlainOpen("../")
	helper.CheckIfError(err)

	// ... retrieving the HEAD reference
	ref, err := repo.Head()
	helper.CheckIfError(err)

	// ... retrieves the commit history
	cIter, err := repo.Log(&git.LogOptions{From: ref.Hash()})
	helper.CheckIfError(err)

	var l []*commitResolver

	err = cIter.ForEach(func(c *object.Commit) error {
		l = append(l, &commitResolver{c})
		return nil
	})
	helper.CheckIfError(err)
	return &l

}

// commitResolver resolves Commits
func (r *commitResolver) Hash() string {
	return r.commit.Hash.String()
}

func (r *commitResolver) Message() string {
	return r.commit.Message
}

func (r *commitResolver) Date() string {
	return r.commit.Author.When.Format(time.RFC3339)
}
