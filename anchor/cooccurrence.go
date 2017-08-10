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
	Qdata := make([]float64, V*V) // Avoids bound checks of Dense.Set.

	for doc := range c.Documents() {
		nd := len(doc.Tokens)
		if nd <= 1 {
			continue
		}
		norm := 1 / float64(nd*(nd-1))

		for i, ti := range doc.Tokens {
			for j, tj := range doc.Tokens {
				if i == j {
					continue
				}
				Qdata[ti.Type*V+tj.Type] += norm // Write data in row-major order.
			}
		}
	}

	Q := mat.NewDense(V, V, Qdata)
	Q.Scale(1/float64(D), Q)
	return Q
}
