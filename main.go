package main

import (
	"fmt"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/cluster"

	"ford/dataset"
)

func main() {
	total := 0.0
	samples := 0

	for i := 0; i < 100; i++ {
		for topic := 0; topic < 44; i++ {
			corpus := dataset.Ambiant[topic].Import()
			mm := cluster.NewMM(corpus, 5, .1, .01)
			inferencer := cluster.NewMMCCM(mm)
			checker := cluster.NewMMConvergenceChecker(mm)
			converged := false
			for !converged {
				inferencer.Inference()
				converged = checker.Check() == 0
			}

			gold := dataset.Ambiant[topic].Label(corpus)
			pred := eval.NewClusteringMM(mm)
			contingency := eval.NewContingency(gold, pred)

			rand := contingency.Rand()
			total += rand
			samples += 1
			fmt.Println(total / float64(samples))
		}
	}
}
