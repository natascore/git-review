package resolver

type commitResolver struct {
	commit Commit
}

// Commit from Git
type Commit struct {
	Hash string
}

// GetHistory QueryResolver for GetHistory()
func (r *Resolver) GetHistory() *[]*commitResolver {

	var testCommit = Commit{
		Hash: "First Commit",
	}

	var testCommit1 = Commit{
		Hash: "Second Commit",
	}

	var commitCollection = []Commit{
		testCommit, testCommit1,
	}

	var l []*commitResolver
	for _, commit := range commitCollection {
		l = append(l, &commitResolver{commit})
	}

	return &l

}

// commitResolver resolves Commits
func (r *commitResolver) Hash() string {
	return r.commit.Hash
}
