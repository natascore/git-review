package resolver

import (
	helper "github.com/natascore/git-review/backend/helper"
	git "gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing/object"
)

// History QueryResolver for git log
func (r *Resolver) History() *[]*CommitResolver {

	repo, err := git.PlainOpen("../")
	helper.CheckIfError(err)

	// ... retrieving the HEAD reference
	ref, err := repo.Head()
	helper.CheckIfError(err)

	// ... retrieves the commit history
	cIter, err := repo.Log(&git.LogOptions{From: ref.Hash()})
	helper.CheckIfError(err)

	var l []*CommitResolver

	err = cIter.ForEach(func(c *object.Commit) error {
		l = append(l, &CommitResolver{c, repo})
		return nil
	})
	helper.CheckIfError(err)
	return &l

}
