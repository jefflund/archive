package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/cluster"
	"github.com/jlund3/modelt/topic/crpcluster"

	"ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	mmrandmean := &meancalc{}
	crprandmean := &meancalc{}
	tmean := &meancalc{}

	for {
		for topic := 0; topic < 44; topic++ {
			mmrand, crprand, nclusters := run(load.Ambiant[topic])
			mmrandmean.observe(mmrand)
			crprandmean.observe(crprand)
			tmean.observe(float64(nclusters))
			fmt.Printf("MM:%.3f CRP:%.3f (%.3f)\n",
				mmrandmean.mean(), crprandmean.mean(), tmean.mean())
		}
	}
}

func run(i load.Importer) (mmrand, crprand float64, nclusters int) {
	corpus := i.Import()
	gold := i.Label(corpus)

	mm := cluster.NewMM(corpus, 20, 1, .01)
	mminferencer := cluster.NewMMCCM(mm)
	mminferencer.Inference()
	mminferencer.Inference()
	mmpred := eval.NewClusteringMM(mm)
	mmrand = eval.NewContingency(gold, mmpred).Rand()

	crpmm := crpcluster.NewCRPMM(corpus, 50, 5, .01, .1)
	crpinferencer := crpcluster.NewCRPMMCCM(crpmm)
	crpinferencer.Inference()
	crpinferencer.Inference()
	crppred := eval.NewClusteringCRPMM(crpmm)
	crprand = eval.NewContingency(gold, crppred).Rand()
	nclusters = crpmm.T

	return
}

type meancalc struct {
	sum float64
	n   int
}

func (m *meancalc) observe(x float64) {
	m.sum += x
	m.n += 1
}

func (m *meancalc) mean() float64 {
	return m.sum / float64(m.n)
}
