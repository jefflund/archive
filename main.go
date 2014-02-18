package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
	"github.com/jlund3/modelt/topic/cluster"
	"github.com/jlund3/modelt/topic/crpcluster"

	"ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	mmmean := &meancalc{}
	crpmean := &meancalc{}
	tmean := &meancalc{}

	for {
		for topic := 0; topic < 44; topic++ {
			importer := load.Ambiant[topic]
			corpus := importer.Import()
			gold := importer.Label(corpus)

			mmmean.observe(runMM(corpus, gold))
			r, t := runCRP(corpus, gold)
			crpmean.observe(r)
			tmean.observe(float64(t))

			fmt.Printf("MM:%.3f CRP:%.3f (%.3f)\n",
				mmmean.mean(), crpmean.mean(), tmean.mean())
		}
	}
}

func runMM(c *pipeline.Corpus, g *eval.Clustering) (rand float64) {
	mm := cluster.NewMM(c, 20, 1, .01)
	inferencer := cluster.NewMMCCM(mm)
	inferencer.Inference()
	inferencer.Inference()
	pred := eval.NewClusteringMM(mm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand()
}

func runCRP(c *pipeline.Corpus, g *eval.Clustering) (rand float64, T int) {
	crpmm := crpcluster.NewCRPMM(c, 50, 5, .01, .1)
	inferencer := crpcluster.NewCRPMMCCM(crpmm)
	inferencer.Inference()
	inferencer.Inference()
	pred := eval.NewClusteringCRPMM(crpmm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand(), crpmm.T
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
