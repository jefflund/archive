package anchor

import (
	//"math"
	//"math/rand"

	"gonum.org/v1/gonum/mat"

	"github.com/jefflund/modelt/pipeline"
)

// BuildCooccurrence follows supplementary 4.1 of Arora et al. 2012 to
// generate a cooccurrence matrix from Corpus data with the proper
// expectations.
func BuildCooccurrence(c pipeline.Corpus) *mat.Dense {
	D, V := c.Size()

	// Q is a cooccurrence matrix with Q_{i,j} = p(w_j|w_i).
	Q := mat.NewDense(V, V, nil)
	Qdata := Q.RawMatrix().Data // Avoids bound checks of Q.Set.

	// For each cooccurrence in the corpus, add some weight to Q.
	for doc := range c.Documents() {
		// Short documents have no cooccurrences.
		nd := len(doc.Tokens)
		if nd <= 1 {
			continue
		}

		// Since a word doesn't cooccur with itself, there are nd * (nd - 1)
		// cooccurrences in a document. By scaling the cooccurrences of each
		// document by this amount, we give equal weight to each document
		// similar to Anandkumar et al., 2012.
		norm := 1 / float64(nd*(nd-1))
		for i, ti := range doc.Tokens {
			for j, tj := range doc.Tokens {
				if i == j {
					continue
				}
				Qdata[ti.Type*V+tj.Type] += norm // Data is in row-major order.
			}
		}
	}

	// Since we add a total of 1 for each document, dividing by D normalize Q.
	Q.Scale(1/float64(D), Q)
	return Q
}
