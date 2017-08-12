package anchor

import (
	"math"

	"gonum.org/v1/gonum/mat"
)

func _ExponentiatedGradient(Y *mat.VecDense, X, XX *mat.Dense, epsilon float64) []float64 {
	return nil
}

func RecoverTopics(Q, anchors *mat.Dense, epsilon float64) *mat.Dense {
	K, V := anchors.Dims()

	// A encodes topic-word distributions as stochastic column vectors.
	A := mat.NewDense(V, K, nil)

	// X is row-normalized anchors (so we don't modify anchors).
	X := mat.NewDense(V, K, nil)
	X.Copy(anchors)
	_RowNormalize(X)

	// Pre-compute X X.T for _ExponentiatedGradient.
	XX := mat.NewDense(V, V, nil)
	XX.Mul(X, X.T())

	// Compute word marginals for use with Bayes' rule later.
	Pw := mat.NewDiagonal(V, _RowSums(Q))
	for v := 0; v < V; v++ {
		if math.IsNaN(Pw.At(v, v)) {
			Pw.SetSymBand(v, v, 1e-16)
		}
	}

	// Compute row normalized Q (without modifying original Q).
	Q.Copy(Q)
	_RowNormalize(Q)

	for v := 0; v < V; v++ {
		alpha := _ExponentiatedGradient(Q.RowView(v), X, XX, epsilon)
		// if alpha contains NaN, set all alpha to 1/K
		A.SetRow(v, alpha)
	}

	A.Mul(Pw, A)
	// Column Normalize A
	return A
}
