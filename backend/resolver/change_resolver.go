package resolver

import (
	helper "github.com/natascore/git-review/backend/helper"
	git "gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing"
	"gopkg.in/src-d/go-git.v4/plumbing/object"
)

type fileChangeResolver struct {
	change *object.Change
}

func (r *fileChangeResolver) Diff() string {
	patch, err := r.change.Patch()
	helper.CheckIfError(err)

	return patch.String()
}

func (r *fileChangeResolver) Action() string {
	return r.change.String()

}

func (r *fileChangeResolver) From() *string {
	from, _, err := r.change.Files()
	helper.CheckIfError(err)

	if from == nil {
		return nil
	}

	fromContent, err := from.Contents()
	helper.CheckIfError(err)

	return &fromContent
}

func (r *fileChangeResolver) To() *string {
	_, to, err := r.change.Files()
	helper.CheckIfError(err)

	if to == nil {
		return nil
	}

	toContent, err := to.Contents()
	helper.CheckIfError(err)

	return &toContent
}

// GetChanges QueryResolver for GetChanges()
func (r *Resolver) GetChanges(args struct{ Hash string }) *[]*fileChangeResolver {

	repo, err := git.PlainOpen("../")
	helper.CheckIfError(err)

	hash := plumbing.NewHash(args.Hash)

	to, err := repo.CommitObject(hash)
	helper.CheckIfError(err)

	from, err := to.Parent(0)
	helper.CheckIfError(err)

	fromTree, err := from.Tree()
	helper.CheckIfError(err)
	toTree, err := to.Tree()
	helper.CheckIfError(err)

	changes, err := fromTree.Diff(toTree)
	helper.CheckIfError(err)

	var l []*fileChangeResolver
	for _, v := range changes {
		l = append(l, &fileChangeResolver{v})
	}

	return &l

}
