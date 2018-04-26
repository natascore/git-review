package resolver

import "gopkg.in/src-d/go-git.v4/plumbing/object"

// SignatureResolver Resolves a Signature
type SignatureResolver struct {
	signature object.Signature
}

// Name resolves to Name
func (r *SignatureResolver) Name() string {
	return r.signature.Name
}

// Email resolves to Email
func (r *SignatureResolver) Email() string {
	return r.signature.Email
}
