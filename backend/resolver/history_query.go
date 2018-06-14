package resolver

import (
	helper "github.com/natascore/git-review/backend/helper"
	git "gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing"
	"regexp"
)

// History QueryResolver for git log
func (r *Resolver) History(args struct {
	First int32
	After *string
	Grep *string
}) *[]*CommitResolver {

	repo, err := git.PlainOpen("../")
	helper.CheckIfError(err)

	var hash plumbing.Hash
	var l []*CommitResolver

	if args.After != nil {
		curentHash := plumbing.NewHash(string(*args.After))
		commit, err := repo.CommitObject(curentHash)
		if err != nil {
			panic("commit not found")
		}
		parent, err := commit.Parent(0)
		// if it is the initial commit we return an empty list
		if err != nil {
			return &l
		}
		hash = parent.Hash
	} else {
		// ... retrieving the HEAD reference
		ref, err := repo.Head()
		helper.CheckIfError(err)
		hash = ref.Hash()
	}

	// ... retrieves the commit history
	cIter, err := repo.Log(&git.LogOptions{From: hash})
	helper.CheckIfError(err)

	for i := 0; i < int(args.First); i++ {
		c, err := cIter.Next()
		if err != nil {
			break
		}
		if args.Grep != nil {
			message := c.Message
			r, _ := regexp.Compile(*args.Grep)
			if !r.MatchString(message) {
				i--
				continue
			}
		}
		l = append(l, &CommitResolver{c, repo})
	}
	return &l
}
