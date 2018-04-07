package main

import (
	"fmt"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/graphical"
	"github.com/jlund3/modelt/graphical/cluster"

	"github.com/jlund3/ford/load"
)

func main() {
	sumR := 0.0
	sum3 := 0.0
	sum5 := 0.0
	sum10 := 0.0

	for _, importer := range load.Ambiant {
		corpus := importer.Import()

		dpmom := cluster.NewDPMoM(corpus, 1, 20, .2, .2)
		graphical.RunCCM(dpmom, cluster.NewConvergenceCheck(dpmom.Z), 0)

		gold := importer.Label(corpus)
		pred := eval.NewClusteringState(dpmom.Z)
		cont := eval.NewContingency(gold, pred)
		rerank := eval.Rerank(dpmom.Z)

		sumR += cont.Rand()
		sum3 += eval.SRecallK(3, rerank, gold)
		sum5 += eval.SRecallK(5, rerank, gold)
		sum10 += eval.SRecallK(10, rerank, gold)
	}

	n := float64(len(load.Ambiant))
	fmt.Println("Rand :", sumR/n)
	fmt.Println("SR@3 :", sum3/n)
	fmt.Println("SR@5 :", sum5/n)
	fmt.Println("SR@10:", sum10/n)
}
