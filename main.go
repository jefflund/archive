package main

import (
	"fmt"
	"math"
	"math/rand"
	"time"

	"path/filepath"
	"sort"
	"strconv"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/crpcluster"

	"ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	ambmean := run(load.Ambiant)
	fmt.Println("Ambiant", ambmean.means())

	mormean := run(load.Moresque)
	fmt.Println("Moresque", mormean.means())

	allmean := Combine(ambmean, mormean)
	fmt.Println("All", allmean.means())
}

func run(importers []load.Importer) *meancalcs {
	result := NewMeanCalcs(8)

	for _, importer := range importers {
		corpus := importer.Import()
		gold := importer.Label(corpus)

		crpmm := crpcluster.NewCRPMM(corpus, 50, 1, .1, .4)
		inferencer := crpcluster.NewCRPMMCCM(crpmm)
		for i := 0; i < 2; i++ {
			inferencer.Inference()
		}

		pred := eval.NewClusteringCRPMM(crpmm)
		contingency := eval.NewContingency(gold, pred)
		rand := contingency.Rand()
		terr := float64(crpmm.T - len(gold.Labels))

		reranked := rerank(crpmm)

		result.observe(rand, terr, math.Abs(terr),
			srecallat(3, reranked, gold),
			srecallat(5, reranked, gold),
			srecallat(10, reranked, gold),
			srecallat(15, reranked, gold),
			srecallat(20, reranked, gold))
	}

	return result
}

func srecallat(k int, ranking []int, g *eval.Clustering) float64 {
	if k >= len(ranking) {
		return 1
	}

	found := make(map[interface{}]bool)
	for _, d := range ranking[:k] {
		found[g.Data[d]] = true
	}
	return float64(len(found)) / float64(len(g.Labels))
}

func rerank(crpmm *crpcluster.CRPMM) []int {
	topics := make([]int, crpmm.T)
	i := 0
	for z, used := range crpmm.Used {
		if used {
			topics[i] = z
			i++
		}
	}
	sorter := &topicsort{crpmm, topics}
	sort.Sort(sorter)

	clusters := make([][]int, crpmm.T)
	for i, z := range topics {
		clusters[i] = make([]int, crpmm.Topics[z])
		j := 0
		for d := 0; d < crpmm.M; d++ {
			if crpmm.Z[d] == z {
				clusters[i][j] = d
				j++
			}
		}
		sorter := &clustersort{crpmm, clusters[i]}
		sort.Sort(sorter)
	}

	reranked := make([]int, crpmm.M)
	r := 0
	k := 0
	for r < crpmm.M {
		for _, cluster := range clusters {
			if k < len(cluster) {
				reranked[r] = cluster[k]
				r++
			}
		}
		k++
	}

	return reranked
}

type clustersort struct {
	crpmm   *crpcluster.CRPMM
	cluster []int
}

func (c *clustersort) Len() int {
	return len(c.cluster)
}

func (c *clustersort) Less(i, j int) bool {
	ri := rank(c.crpmm.Titles[c.cluster[i]])
	rj := rank(c.crpmm.Titles[c.cluster[j]])
	return ri < rj
}

func rank(title string) int {
	base := filepath.Base(title)
	i, err := strconv.Atoi(base)
	if err != nil {
		panic(err)
	}
	return i
}

func (c *clustersort) Swap(i, j int) {
	c.cluster[i], c.cluster[j] = c.cluster[j], c.cluster[i]
}

type topicsort struct {
	crpmm  *crpcluster.CRPMM
	topics []int
}

func (t *topicsort) Len() int {
	return len(t.topics)
}

func (t *topicsort) Less(i, j int) bool {
	ci := t.crpmm.Topics[t.topics[i]]
	cj := t.crpmm.Topics[t.topics[j]]
	return ci > cj
}

func (t *topicsort) Swap(i, j int) {
	t.topics[i], t.topics[j] = t.topics[j], t.topics[i]
}

type meancalcs struct {
	sums []float64
	n    int
}

func NewMeanCalcs(k int) *meancalcs {
	return &meancalcs{make([]float64, k), 0}
}

func Combine(ms ...*meancalcs) *meancalcs {
	combined := NewMeanCalcs(len(ms[0].sums))
	for _, m := range ms {
		for i, x := range m.sums {
			combined.sums[i] += x
		}
		combined.n += m.n
	}
	return combined
}

func (m *meancalcs) observe(xs ...float64) {
	for i, x := range xs {
		m.sums[i] += x
	}
	m.n += 1
}

func (m *meancalcs) means() []float64 {
	ms := make([]float64, len(m.sums))
	for i, sum := range m.sums {
		ms[i] = sum / float64(m.n)
	}
	return ms
}
