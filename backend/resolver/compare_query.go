package resolver

import (
	helper "github.com/natascore/git-review/backend/helper"
	git "gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing"
)

// Compare QueryResolver for GetChanges()
func (r *Resolver) Compare(args struct {
	From string
	To   string
}) *[]*FileChangeResolver {

	repo, err := git.PlainOpen("../")
	helper.CheckIfError(err)

	hash := plumbing.NewHash(args.To)

	to, err := repo.CommitObject(hash)
	helper.CheckIfError(err)

	return GetChangesForCommit(repo, to, &args.From)
}
