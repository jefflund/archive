package anchor

import (
	"gonum.org/v1/gonum/mat"

	"github.com/jefflund/modelt/pipeline"
)

// BuildCooccurrence follows supplementary 4.1 of Arora et al. 2012 to
// generate a cooccurrence matrix from Corpus data with the proper
// expectations.
func BuildCooccurrence(c *pipeline.Corpus) *mat.Dense {
	D, V := len(c.Documents), len(c.Vocabulary) // Avoids bounds checks of Dense.Set
	Qdata := make([]float64, V*V)

	for _, doc := range c.Documents {
		n_d := len(doc.Tokens)
		if n_d <= 1 {
			continue
		}
		norm := 1 / float64(n_d*(n_d-1))

		for i, t_i := range doc.Tokens {
			for j, t_j := range doc.Tokens {
				if i == j {
					continue
				}
				Qdata[t_i.Type*V+t_j.Type] += norm // Write data in row-major order.
			}
		}
	}

	Q := mat.NewDense(V, V, Qdata)
	Q.Scale(1/float64(D), Q)
	return Q
}
