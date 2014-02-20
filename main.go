package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
	"github.com/jlund3/modelt/topic/crpcluster"

	"ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	alpha := .1
	beta := .4

	var rand, terr, absterr float64
	var randtotal, terrtotal, absterrtotal float64

	fmt.Printf("Alpha: %f\tBeta: %f\n", alpha, beta)
	fmt.Printf("Algorithm Rand  Terr  AbsTerr\n")

	rand, terr, absterr = runImporters(load.Ambiant, alpha, beta)
	fmt.Printf("Ambiant   %.3f %.3f %.3f\n", rand, terr, absterr)
	randtotal += rand
	terrtotal += terr
	absterrtotal += absterr

	rand, terr, absterr = runImporters(load.Moresque, alpha, beta)
	fmt.Printf("Moresque  %.3f %.3f %.3f\n", rand, terr, absterr)
	randtotal += rand
	terrtotal += terr
	absterrtotal += absterr

	fmt.Printf("All       %.3f %.3f %.3f\n\n", randtotal/2, terrtotal/2, absterrtotal/2)
}

func runImporters(importers []load.Importer, alpha, beta float64) (rand, terr, absterr float64) {
	randmean := &meancalc{}
	terrmean := &meancalc{}
	absterrmean := &meancalc{}

	for _, importer := range importers {
		corpus := importer.Import()
		gold := importer.Label(corpus)

		rand, terr := runCRP(corpus, gold, alpha, beta)
		randmean.observe(rand)
		terrmean.observe(float64(terr))
		absterrmean.observe(float64(abs(terr)))
	}

	return randmean.mean(), terrmean.mean(), absterrmean.mean()
}

func runCRP(c *pipeline.Corpus, g *eval.Clustering, alpha, beta float64) (rand float64, T int) {
	crpmm := crpcluster.NewCRPMM(c, 50, 1, alpha, beta)
	inferencer := crpcluster.NewCRPMMCCM(crpmm)
	for i := 0; i < 2; i++ {
		inferencer.Inference()
	}
	pred := eval.NewClusteringCRPMM(crpmm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand(), crpmm.T - len(g.Labels)
}

func abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
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

type bestparam struct {
	alpha, beta, rand float64
}

func (p *bestparam) update(a, b, r float64) {
	if r > p.rand {
		p.alpha = a
		p.beta = b
		p.rand = r
	}
}
