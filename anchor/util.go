package anchor

import (
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
