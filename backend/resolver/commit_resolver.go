package resolver

import (
	"time"

	git "gopkg.in/src-d/go-git.v4"
	"gopkg.in/src-d/go-git.v4/plumbing/object"
)

// CommitResolver can Resolv details for a Commit
type CommitResolver struct {
	commit *object.Commit
	repo   *git.Repository
}

// Hash resolves to Hash
func (r *CommitResolver) Hash() string {
	return r.commit.Hash.String()
}

// Message resolves to Message
func (r *CommitResolver) Message() string {
	return r.commit.Message
}

// Date resolves to Date
func (r *CommitResolver) Date() string {
	return r.commit.Author.When.Format(time.RFC3339)
}

// Author resolves to Author
func (r *CommitResolver) Author() *SignatureResolver {
	return &SignatureResolver{r.commit.Author}
}

// Committer resolves to Committer
func (r *CommitResolver) Committer() *SignatureResolver {
	return &SignatureResolver{r.commit.Committer}
}

// Changes resolves to FileChanges
func (r *CommitResolver) Changes() *[]*FileChangeResolver {
	return GetChangesForCommit(r.repo, r.commit, nil)
}
