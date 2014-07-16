package main

import (
	"fmt"
	"math"
	//"math/rand"
	//"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic"
	"github.com/jlund3/modelt/topic/crpcluster"

	"github.com/jlund3/ford/load"
)

const (
	terrKey     = "terr"
	absterrKey  = "absterr"
	itersKey    = "iters"
	randKey     = "rand"
	recall3Key  = "recall3"
	recall5Key  = "recall5"
	recall10Key = "recall10"
	recall15Key = "recall15"
	recall20Key = "recall20"
)

var (
	grid = []float64{.1, .2, .3, .4, .5, .6, .7, .8, .9, 1}
)

func main() {
	runRunner("Gibbs", func(m *crpcluster.CRPMM) {
		inference := crpcluster.Gibbs(m)
		for i := 0; i < 10; i++ {
			inference()
		}
	})
	runRunner("SUGS", func(m *crpcluster.CRPMM) {
		m.AbolateAll()
		inference := crpcluster.CCM(m)
		inference()
	})
	runRunner("SUGS+CCM", func(m *crpcluster.CRPMM) {
		m.AbolateAll()
		inference := crpcluster.CCM(m)
		checker := topic.NewDocConvergenceChecker(m.Z)
		converged := false
		for !converged {
			inference()
			converged = checker.Check() == 0
		}
	})
	runRunner("CCM-2", func(m *crpcluster.CRPMM) {
		inference := crpcluster.CCM(m)
		inference()
		inference()
	})
	runRunner("CCM", func(m *crpcluster.CRPMM) {
		inference := crpcluster.CCM(m)
		checker := topic.NewDocConvergenceChecker(m.Z)
		converged := false
		for !converged {
			inference()
			converged = checker.Check() == 0
		}
	})
}

func runRunner(name string, r Runner) {
	ambMeans := r.RunAll(load.Ambiant)
	morMeans := r.RunAll(load.Moresque)

	fmt.Println(name)
	fmt.Println("AMB", ambMeans.Means())
	fmt.Println("MOR", morMeans.Means())
	fmt.Println("ALL", eval.MeanCalcsCombine(ambMeans, morMeans).Means())
}

type Runner func(*crpcluster.CRPMM)

func (r Runner) RunAll(data []load.Importer) *eval.MeanCalcs {
	means := eval.NewMeanCalcs()
	for _, importer := range data {
		for _, alpha := range grid {
			for _, beta := range grid {
				r.Run(alpha, beta, importer, means)
			}
		}
	}
	return means
}

func (r Runner) Run(alpha, beta float64, i load.Importer, m *eval.MeanCalcs) {
	corpus := i.Import()
	gold := i.Label(corpus)

	crpmm := crpcluster.NewCRPMM(corpus, 50, 1, alpha, beta)
	r(crpmm)

	terr := float64(crpmm.T - len(gold.Labels))
	m.Observe(terrKey, terr)
	m.Observe(absterrKey, math.Abs(terr))

	pred := eval.NewClusteringState(crpmm.Z)
	cont := eval.NewContingency(gold, pred)
	m.Observe(randKey, cont.Rand())

	ranking := eval.Rerank(crpmm)
	m.Observe(recall3Key, eval.SRecallK(3, ranking, gold))
	m.Observe(recall5Key, eval.SRecallK(5, ranking, gold))
	m.Observe(recall10Key, eval.SRecallK(10, ranking, gold))
	m.Observe(recall15Key, eval.SRecallK(15, ranking, gold))
	m.Observe(recall20Key, eval.SRecallK(20, ranking, gold))
}
