package main

import (
	"fmt"
	"math"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
	"github.com/jlund3/modelt/topic/crpcluster"

	"ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	var rand, terr, absterr float64
	var randtotal, terrtotal, absterrtotal float64

	fmt.Printf("Algorithm Rand  Terr  AbsTerr\n")

	rand, terr, absterr = runImporters(load.Ambiant)
	fmt.Printf("Ambiant   %.3f %.3f %.3f\n", rand, terr, absterr)
	randtotal += rand
	terrtotal += terr
	absterrtotal += absterr

	rand, terr, absterr = runImporters(load.Moresque)
	fmt.Printf("Moresque  %.3f %.3f %.3f\n", rand, terr, absterr)
	randtotal += rand
	terrtotal += terr
	absterrtotal += absterr

	fmt.Printf("All       %.3f %.3f %.3f\n\n",
		randtotal/2, terrtotal/2, absterrtotal/2)
}

func runImporters(importers []load.Importer) (rand, terr, absterr float64) {
	randmean := new(meancalc)
	terrmean := new(meancalc)
	absterrmean := new(meancalc)

	for _, importer := range importers {
		corpus := importer.Import()
		gold := importer.Label(corpus)
		rand, terr := runCRP(corpus, gold)

		randmean.observe(rand)
		terrmean.observe(terr)
		absterrmean.observe(math.Abs(terr))
	}

	return randmean.mean(), terrmean.mean(), absterrmean.mean()
}

func runCRP(c *pipeline.Corpus, g *eval.Clustering) (rand, terr float64) {
	crpmm := crpcluster.NewCRPMM(c, 50, 1, .1, .4)
	inferencer := crpcluster.NewCRPMMCCM(crpmm)
	for i := 0; i < 2; i++ {
		inferencer.Inference()
	}
	pred := eval.NewClusteringCRPMM(crpmm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand(), float64(crpmm.T - len(g.Labels))
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
