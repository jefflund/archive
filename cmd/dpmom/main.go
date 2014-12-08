package main

import (
	"fmt"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/cluster"

	"github.com/jlund3/ford/load"
)

func main() {
	sum := 0.0

	for _, importer := range load.Ambiant {
		corpus := importer.Import()

		dpmom := cluster.NewDPMoM(corpus, 1, 20, .2, .2)
		for i := 0; i < 10; i++ {
			dpmom.CCM()
		}

		gold := importer.Label(corpus)
		pred := eval.NewClusteringState(dpmom.Z)
		cont := eval.NewContingency(gold, pred)

		sum += cont.Rand()
	}

	fmt.Println("Rand:", sum/float64(len(load.Ambiant)))
}
