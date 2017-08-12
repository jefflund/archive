package anchor

import (
	"math"

	"gonum.org/v1/gonum/mat"

	"github.com/jefflund/modelt/pipeline"
)

func _FindCommonWords(c pipeline.Corpus, threshold int) []int {
	V := len(c.Vocabulary())
	if threshold < 0 {
		candidates := make([]int, V)
		for v := 0; v < V; v++ {
			candidates[v] = v
		}
		return candidates
	}

	counts := make(map[int]int)
	for doc := range c.Documents() {
		docwords := make(map[int]struct{})
		for _, t := range doc.Tokens {
			docwords[t.Type] = struct{}{}
		}
		for v := range docwords {
			counts[v]++
		}
	}

	candidates := make([]int, 0, V)
	for i := 0; i < V; i++ {
		if counts[i] > threshold {
			candidates = append(candidates, i)
		}
	}
	return candidates
}

func GramSchmidtAnchors(c pipeline.Corpus, Q *mat.Dense, k, projectDim, anchorThreshold int) *mat.Dense {
	// Using rare words leads to extremely eccentric anchors.
	candidates := _FindCommonWords(c, anchorThreshold)

	// Row normalize and project Q, but preserve original Q for anchors
	Qorig, Q := Q, mat.DenseCopyOf(Q)
	_RowNormalize(Q)
	if projectDim > 0 {
		Q = _RandomProjection(Q, projectDim)
	}

	// Setup book keeping.
	_, V := Q.Dims() // Use Q.Dims() not len(c.Vocabulary()) due to projection.
	indices := make([]int, k)
	basis := mat.NewDense(k-1, V, nil)

	// Find farthest point from the origin.
	var maxdist float64
	for _, i := range candidates {
		row := Q.RowView(i)
		if dist := mat.Dot(row, row); dist > maxdist {
			maxdist = dist
			indices[0] = i
		}
	}

	// Translate all points to our new coordinate system.
	origin := mat.NewVecDense(V, nil)
	origin.CopyVec(Q.RowView(indices[0]))
	for _, i := range candidates {
		row := Q.RowView(i)
		row.SubVec(row, origin)
	}

	// Find farthest point from the new origin and form first basis vector.
	maxdist = 0
	for _, i := range candidates {
		row := Q.RowView(i)
		if dist := mat.Dot(row, row); dist > maxdist {
			maxdist = dist
			indices[1] = i
		}
	}
	basis.RowView(0).ScaleVec(1/math.Sqrt(maxdist), Q.RowView(indices[1]))

	// Stabilized Gram-Schmidt to expand our subspace.
	for j := 1; j < k-1; j++ {
		maxdist = 0
		basisrow := basis.RowView(j - 1)
		for _, i := range candidates {
			row := Q.RowView(i)
			tmp := mat.NewVecDense(V, nil)
			tmp.ScaleVec(mat.Dot(row, basisrow), basisrow)
			row.SubVec(row, tmp)
			if dist := mat.Dot(row, row); dist > maxdist {
				maxdist = dist
				indices[j+1] = i
			}
		}
		basis.RowView(j).ScaleVec(1/math.Sqrt(maxdist), Q.RowView(indices[j+1]))
	}

	// Get anchors from original Q and anchor indices.
	_, V = Q.Dims()
	anchors := mat.NewDense(k, V, nil)
	for i := 0; i < k; i++ {
		anchors.RowView(i).CopyVec(Qorig.RowView(indices[i]))
	}
	return anchors
}
