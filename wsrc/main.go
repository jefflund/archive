package main

import (
	"fmt"
	//"math/rand"
	//"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic"
	"github.com/jlund3/modelt/topic/crpcluster"

	"github.com/jlund3/ford/load"
)

const (
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
	ambMeans := runAll(load.Ambiant)
	morMeans := runAll(load.Moresque)

	fmt.Println("AMB", ambMeans.Means())
	fmt.Println("MOR", morMeans.Means())
	fmt.Println("ALL", eval.MeanCalcsCombine(ambMeans, morMeans).Means())
}

func runAll(data []load.Importer) *eval.MeanCalcs {
	means := eval.NewMeanCalcs()
	for _, importer := range data {
		for _, alpha := range grid {
			for _, beta := range grid {
				run(alpha, beta, importer, means)
			}
		}
	}
	return means
}

func run(alpha, beta float64, i load.Importer, m *eval.MeanCalcs) {
	corpus := i.Import()
	gold := i.Label(corpus)

	crpmm := crpcluster.NewCRPMM(corpus, 50, 1, alpha, beta)
	crpmm.AbolateAll()
	inference := crpcluster.CCM(crpmm)
	checker := topic.NewDocConvergenceChecker(crpmm.Z)
	converged := false
	for !converged {
		inference()
		converged = checker.Check() == 0 || true
	}

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
