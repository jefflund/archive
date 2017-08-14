package anchor

import (
	"math"

	"gonum.org/v1/gonum/mat"

	"github.com/jefflund/modelt/pipeline"
)

func _FindCommonWords(c pipeline.Corpus, threshold int) []int {
	V := len(c.Vocabulary())

	// If threshold negative, no filtering done, so return all words.
	if threshold < 0 {
		candidates := make([]int, V)
		for v := 0; v < V; v++ {
			candidates[v] = v
		}
		return candidates
	}

	// Count how many times each word appears in a document.
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

	// Find the words that appear in enough documents to be considered common.
	candidates := make([]int, 0, V)
	for i := 0; i < V; i++ {
		if counts[i] > threshold {
			candidates = append(candidates, i)
		}
	}
	return candidates
}

func GramSchmidtAnchors(c pipeline.Corpus, Q *mat.Dense, K, projectDim, anchorThreshold int) *mat.Dense {
	// Using rare words leads to extremely eccentric anchors.
	candidates := _FindCommonWords(c, anchorThreshold)

	// Row normalize and project Q, preserving original Q.
	Qorig, Q := Q, mat.DenseCopyOf(Q)
	_MatRowNormalize(Q)
	if projectDim > 0 {
		Q = _RandomProjection(Q, projectDim)
	}

	// Setup book keeping.
	_, V := Q.Dims() // Use Q.Dims() not len(c.Vocabulary()) due to projection.
	indices := make([]int, K)
	basis := mat.NewDense(K-1, V, nil)

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
	origin.CopyVec(Q.RowView(indices[0])) // Need copy for i > indices[0].
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
	for j := 1; j < K-1; j++ {
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
	anchors := mat.NewDense(K, V, nil)
	for i := 0; i < K; i++ {
		anchors.RowView(i).CopyVec(Qorig.RowView(indices[i]))
	}
	return anchors
}
