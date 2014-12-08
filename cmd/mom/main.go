package main

import (
	"fmt"
	"math/rand"
	"strings"
	"time"

	"github.com/jlund3/ford/load"
	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/cluster"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	corpus := load.Newsgroups.Import()
	gold := load.Newsgroups.Label(corpus)

	mom := cluster.NewMoM(corpus, 20, .1, .01)
	checker := cluster.NewConvergenceCheck(mom.Z)
	converged := false

	var dur time.Duration
	var iter int

	fmt.Println("iter\ttime\t\tchanges\tf-measure")

	for !converged {
		start := time.Now()
		mom.CCM()
		end := time.Now()

		dur += end.Sub(start)
		iter += 1

		pred := eval.NewClusteringState(mom.Z)
		cont := eval.NewContingency(gold, pred)
		changes := checker.Check()

		line := []string{
			fmt.Sprintf("%d", iter),
			fmt.Sprintf("%f", dur.Seconds()),
			fmt.Sprintf("%d", changes),
			fmt.Sprintf("%f", cont.FMeasure())}
		fmt.Println(strings.Join(line, "\t"))

		converged = changes == 0
	}

	fmt.Println()
	for z := 0; z < mom.T; z++ {
		fmt.Printf("%d: %s\n", z, mom.TopicSummary(z, 10))
	}
}
