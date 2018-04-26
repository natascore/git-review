package resolver

import (
	helper "github.com/natascore/git-review/backend/helper"
	git "gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing"
)

// Commit QueryResolver for a single Commit()
func (r *Resolver) Commit(args struct {
	Hash string
}) *CommitResolver {

	repo, err := git.PlainOpen("../")
	helper.CheckIfError(err)

	hash := plumbing.NewHash(args.Hash)

	commit, err := repo.CommitObject(hash)
	helper.CheckIfError(err)

	return &CommitResolver{commit, repo}

}
