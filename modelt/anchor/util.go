package anchor

import (
	"math"
	"math/rand"

	"gonum.org/v1/gonum/mat"
)

const (
	_Sqrt3 = 1.73205080756887729352744634150587236694280525381038062805580697 // http://oeis.org/A002194
)

// _RandomProjection projects the points (rows) of A into k-dimensions,
// following the method given by Achlioptas 2001 which preserves pairwise
// distances within a certain threshold.
func _RandomProjection(A *mat.Dense, k int) *mat.Dense {
	r, c := A.Dims()

	R := mat.NewDense(c, k, nil)
	for i := 0; i < c; i++ {
		for j := 0; j < k; j++ {
			if p := rand.Float64(); p < 1/6 {
				R.Set(i, j, _Sqrt3)
			} else if p < 1/3 {
				R.Set(i, j, -_Sqrt3)
			}
		}
	}

	proj := mat.NewDense(r, k, nil)
	proj.Mul(A, R)
	return proj
}

// These functions are named in such a way as to give a hint about their use.
// The general scheme is as follows: _<reciever><operation><operands>.
// The reciever type indicates the type of the first argument that will store
// the result of the operation. If there is no reciever, then the function
// returns a new matrix or vector with the result.

// _MatRowNormalize scales each row of the given matrix so that it sums to 1.
// The normalization is done in place.
func _MatRowNormalize(A *mat.Dense) {
	r, c := A.Dims()

	for i := 0; i < r; i++ {
		row := A.RawRowView(i)
		var sum float64
		for j := 0; j < c; j++ {
			sum += row[j]
		}
		for j := 0; j < c; j++ {
			row[j] /= sum
		}
	}
}

// _MatColumnNormalize scales each column of the given matrix so that it sums
// to 1. This column normalization is done in place.
func _MatColumnNormalize(A *mat.Dense) {
	r, c := A.Dims()

	for j := 0; j < c; j++ {
		col := A.ColView(j).RawVector()
		var sum float64
		for i := 0; i < r; i++ {
			sum += col.Data[i]
		}
		for i := 0; i < r; i++ {
			col.Data[i] /= sum
		}
	}
}

// _RowSumsMat returns a slice containing the sums of each row of A.
func _RowSumsMat(A *mat.Dense) []float64 {
	r, c := A.Dims()

	sums := make([]float64, r)
	for i := 0; i < r; i++ {
		row := A.RawRowView(i)
		for j := 0; j < c; j++ {
			sums[i] += row[j]
		}
	}
	return sums
}

// _MulVecMat returns a.T * b as a vector.
func _MulVecMat(a *mat.VecDense, b *mat.Dense) *mat.VecDense {
	_, m := b.Dims()
	c := mat.NewDense(1, m, nil)
	c.Mul(a.T(), b)
	return c.RowView(0)
}

// _LogSumExpVec computes the log of the sum of exponentials of a in a
// numerically stable way. This is useful for computing sums in log space.
func _LogSumExpVec(a *mat.VecDense) float64 {
	raw := a.RawVector().Data // Avoid bounds checks of a.At.

	max := math.Inf(-1)
	for _, v := range raw {
		max = math.Max(v, max)
	}

	var sum float64
	for _, v := range raw {
		sum += math.Exp(v - max)
	}

	return max + math.Log(sum)
}

// _VecSubFloat subtracts a float from each element of a vector.
func _VecSubFloat(a *mat.VecDense, b float64) {
	raw := a.RawVector().Data // Avoid bounds checks of a.At.
	for i := range raw {
		raw[i] -= b
	}
}

// _VecLogNormalize normalizes a log space vector by subtracting the log sum of
// the exponentials of a.
func _VecLogNormalize(a *mat.VecDense) {
	_VecSubFloat(a, _LogSumExpVec(a))
}

// _VecExpVec exponentiates the elements of a and places the result in r.
func _VecExpVec(r, a *mat.VecDense) {
	r.CopyVec(a)
	raw := r.RawVector().Data
	for i, v := range raw {
		raw[i] = math.Exp(v)
	}
}

// _NewUniformVec creates a new vector and fills it with a value.
func _NewUniformVec(n int, v float64) *mat.VecDense {
	data := make([]float64, n)
	for i := 0; i < n; i++ {
		data[i] = v
	}
	return mat.NewVecDense(n, data)
}

// _VecFill sets each element of a vector to be v.
func _VecFill(a *mat.VecDense, v float64) {
	raw := a.RawVector().Data
	for i := 0; i < len(raw); i++ {
		raw[i] = v
	}
}

// SubVecFloat subtracts b from each element of a and returns the result.
func _SubVecFloat(a *mat.VecDense, b float64) *mat.VecDense {
	raw := a.RawVector().Data
	res := make([]float64, len(raw))
	for i, v := range raw {
		res[i] = v - b
	}
	return mat.NewVecDense(len(res), res)
}

// _MinVec returns the minium element of a vector.
func _MinVec(a *mat.VecDense) float64 {
	min := math.Inf(1)
	for _, v := range a.RawVector().Data {
		min = math.Min(min, v)
	}
	return min
}

// _VecHasNan returns true if any element of the vector is NaN.
func _VecHasNan(a *mat.VecDense) bool {
	for _, v := range a.RawVector().Data {
		if math.IsNaN(v) {
			return true
		}
	}
	return false
}
