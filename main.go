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

	randtotal := 0.0
	terrtotal := 0.0

	fmt.Println("Moresque")
	rand, terr := run(load.Moresque)
	randtotal += rand
	terrtotal += terr

	fmt.Println("Ambiant")
	rand, terr = run(load.Ambiant)
	randtotal += rand
	terrtotal += terr

	fmt.Println("All")
	fmt.Printf("%.3f %.3f", randtotal/2, terrtotal/2)
}

func run(importers []load.Importer) (rand, terr float64) {
	randmean := &meancalc{}
	terrmean := &meancalc{}

	for _, importer := range importers {
		corpus := importer.Import()
		gold := importer.Label(corpus)

		rand, terr := runCRP(corpus, gold)
		randmean.observe(rand)
		terrmean.observe(float64(terr))

		fmt.Printf("%.3f %.3f\n", randmean.mean(), terrmean.mean())
	}

	return randmean.mean(), terrmean.mean()
}

func runMM(c *pipeline.Corpus, g *eval.Clustering) (rand float64) {
	mm := cluster.NewMM(c, 8, 1, .01)
	inferencer := cluster.NewMMCCM(mm)
	for i := 0; i < 2; i++ {
		inferencer.Inference()
	}
	pred := eval.NewClusteringMM(mm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand()
}

func runCRP(c *pipeline.Corpus, g *eval.Clustering) (rand float64, T int) {
	crpmm := crpcluster.NewCRPMM(c, 50, 1, .001, .1)
	inferencer := crpcluster.NewCRPMMCCM(crpmm)
	for i := 0; i < 2; i++ {
		inferencer.Inference()
	}
	pred := eval.NewClusteringCRPMM(crpmm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand(), crpmm.T - len(g.Labels)
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
