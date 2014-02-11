package main

import (
	"fmt"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/cluster"

	"ford/dataset"
)

func main() {
	corpus := dataset.Ambiant[0].Import()
	mm := cluster.NewMM(corpus, 5, .1, .01)
	inferencer := cluster.NewMMCCM(mm)
	checker := cluster.NewMMConvergenceChecker(mm)
	converged := false
	for !converged {
		inferencer.Inference()
		converged = checker.Check() == 0
	}

	gold := dataset.Ambiant[0].Label(corpus)
	pred := eval.NewClusteringMM(mm)
	contingency := eval.NewContingency(gold, pred)

	fmt.Println("FM", contingency.FMeasure())
	fmt.Println("ARI", contingency.ARI())
	fmt.Println("VI", contingency.VI())
}
