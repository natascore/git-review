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

func getParentTree(commit *object.Commit, repo *git.Repository) (*object.Tree, error) {
	from, err := commit.Parent(0)
	if err != nil {
		emptyTreeHash := plumbing.NewHash("4b825dc642cb6eb9a060e54bf8d69288fbee4904")
		from, _ := repo.TreeObject(emptyTreeHash)
		return from, nil
	}
	parentTree, err := from.Tree()

	return parentTree, err

}

// GetChanges QueryResolver for GetChanges()
func (r *Resolver) GetChanges(args struct{ Hash string }) *[]*fileChangeResolver {

	repo, err := git.PlainOpen("../")
	helper.CheckIfError(err)

	hash := plumbing.NewHash(args.Hash)

	to, err := repo.CommitObject(hash)
	helper.CheckIfError(err)

	toTree, err := to.Tree()
	helper.CheckIfError(err)

	fromTree, err := getParentTree(to, repo)
	helper.CheckIfError(err)

	changes, err := fromTree.Diff(toTree)
	helper.CheckIfError(err)

	var l []*fileChangeResolver
	for _, v := range changes {
		l = append(l, &fileChangeResolver{v})
	}

	return &l

}
