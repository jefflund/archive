package anchor

import (
	"math"

	"gonum.org/v1/gonum/mat"
)

func _ExponentiatedGradient(Y *mat.VecDense, X, XX *mat.Dense, epsilon float64) *mat.VecDense {
	const (
		C1 = 1e-4
		C2 = .75
	)
	K, _ := X.Dims()

	XY := mat.NewVecDense(K, nil)
	XY.MulVec(X, Y)
	YY := mat.Dot(Y, Y)

	A := _NewUniformVec(K, 1/float64(K))
	logA := _NewUniformVec(K, math.Log(1/float64(K)))
	Adiff := mat.NewVecDense(K, nil)
	oldA := mat.NewVecDense(K, nil)
	oldLogA := mat.NewVecDense(K, nil)

	AXX := _MulVecMat(A, XX)
	AXY := mat.Dot(A, XY)
	AXXA := mat.Dot(AXX, A)

	grad := mat.NewVecDense(K, nil)
	grad.SubVec(AXX, XY)
	grad.ScaleVec(2, grad)

	oldGrad := mat.NewVecDense(K, nil)
	oldGrad.CopyVec(grad)

	obj := AXXA - 2*AXY + YY

	stepsize := 1.0
	decreased := false
	convergence := math.Inf(1)

	for convergence >= epsilon && obj != 0 && stepsize != 0 {
		// Copy A and logA so we can revert if stepsize needs to change.
		oldA.CopyVec(A)
		oldLogA.CopyVec(logA)
		// Add the gradient and renormalize in logspace, then exponentiate.
		logA.AddScaledVec(logA, -stepsize, grad)
		_VecLogNormalize(logA)
		_VecExpVec(A, logA)
		// Precompute change in A.
		Adiff.SubVec(A, oldA)

		// Precomute the quantities needed for adaptive stepsize
		AXX := _MulVecMat(A, XX)
		AXY = mat.Dot(A, XY)
		AXXA = mat.Dot(AXX, A)

		// See if stepsize should decrease
		oldObj, obj := obj, AXXA-2*AXY+YY
		if obj >= oldObj+C1*stepsize*mat.Dot(grad, Adiff) {
			stepsize /= 2
			A.CopyVec(oldA)
			logA.CopyVec(oldLogA)
			obj = oldObj
			decreased = true
			continue
		}

		// Compute new gradient
		oldGrad.CopyVec(grad)
		grad.SubVec(AXX, XY)
		grad.ScaleVec(2, grad)

		// Set if stepsize should increase
		if !decreased && mat.Dot(grad, Adiff) < C2*mat.Dot(oldGrad, Adiff) {
			stepsize *= 2
			A.CopyVec(oldA)
			logA.CopyVec(oldLogA)
			grad.CopyVec(oldGrad)
			obj = oldObj
			continue
		}

		// Update book keeping
		decreased = false
		convergence = mat.Dot(A, _SubVecFloat(grad, _MinVec(grad)))
	}

	return A
}

func RecoverTopics(Q, anchors *mat.Dense, epsilon float64) *mat.Dense {
	K, V := anchors.Dims()

	// X is row-normalized anchors (so we don't modify anchors).
	X := mat.NewDense(K, V, nil)
	X.Copy(anchors)
	_MatRowNormalize(X)

	// Pre-compute X X.T for _ExponentiatedGradient.
	XX := mat.NewDense(K, K, nil)
	XX.Mul(X, X.T())

	// Compute word marginals for use with Bayes' rule later.
	Pw := mat.NewDiagonal(V, _RowSumsMat(Q))
	for v := 0; v < V; v++ {
		if math.IsNaN(Pw.At(v, v)) {
			Pw.SetSymBand(v, v, 1e-16)
		}
	}

	// Compute row normalized Q (without modifying original Q).
	Q.Copy(Q)
	_MatRowNormalize(Q)

	// Reconstruct each word as a linear combination of the anchors.
	A := mat.NewDense(V, K, nil)
	for v := 0; v < V; v++ {
		alpha := _ExponentiatedGradient(Q.RowView(v), X, XX, epsilon)
		if _VecHasNan(alpha) {
			_VecFill(alpha, 1/float64(K))
		}
		A.RowView(v).CopyVec(alpha)
	}

	// Use Bayes' Rule to recover the topic-word distributions as columns of A.
	A.Mul(Pw, A)
	_MatColumnNormalize(A)
	return A
}
