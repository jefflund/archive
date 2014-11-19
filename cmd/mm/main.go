package main

import (
	"fmt"
	"strings"
	"time"

	"github.com/jlund3/ford/load"
	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic"
	"github.com/jlund3/modelt/topic/cluster"
)

func main() {
	corpus := load.Newsgroups.Import()
	gold := load.Newsgroups.Label(corpus)

	for {
		mm := cluster.NewMM(corpus, 20, .1, .01)
		ccm := cluster.CMM(mm)
		checker := topic.NewDocConvergenceChecker(mm.Z)

		var dur time.Duration
		var iter int

		for {
			start := time.Now()
			ccm()
			end := time.Now()
			dur += end.Sub(start)
			iter += 1

			pred := eval.NewClusteringState(mm.Z)
			cont := eval.NewContingency(gold, pred)

			changes := checker.Check()

			line := []string{
				fmt.Sprintf("%d", iter),
				fmt.Sprintf("%f", dur.Seconds()),
				fmt.Sprintf("%d", changes),
				fmt.Sprintf("%f", cont.FMeasure())}
			fmt.Println(strings.Join(line, "\t"))

			if changes == 0 {
				break
			}
		}
		fmt.Println()
	}
}
