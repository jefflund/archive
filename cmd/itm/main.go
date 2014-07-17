package main

import (
	"flag"
	"fmt"
	"io/ioutil"
	"math/rand"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/jlund3/ford/load"
	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic"
	"github.com/jlund3/modelt/topic/interactive"
)

var (
	outdir = flag.String("outdir", "", "output directory")
)

func main() {
	// Setup
	flag.Parse()
	rand.Seed(time.Now().UnixNano())

	// Create out file
	dirpath := filepath.Join("scratch", "output", *outdir)
	if err := os.MkdirAll(dirpath, os.ModeDir|os.ModePerm); err != nil {
		panic(err)
	}
	out, err := ioutil.TempFile(dirpath, "")
	if err != nil {
		panic(err)
	}
	defer out.Close()
	fmt.Fprintln(out, "#time acc")

	// Create model
	corpus := load.Newsgroups.Import()
	rounds, constraints := load.GetConstraints("data/constraints/newsgroups.txt")
	itm := interactive.NewITM(corpus, 20, .1, .01, 100)
	inference := interactive.Gibbs(itm)
	for i := 0; i < 100; i++ {
		inference()
	}

	inference = interactive.CMM(itm)

	var duration time.Duration
	for round := 1; round < rounds; round++ {
		for _, constraint := range constraints {
			itm.AddConstraintString(constraint[:round])
		}

		checker := topic.NewWordConvergenceChecker(itm.Z)
		start := time.Now()
		for iter := 0; iter < 30 && checker.Check() > 1000; iter++ {
			inference()
		}
		end := time.Now()
		duration += end.Sub(start)

		labeled := eval.NewLabeledCorpusModel(itm)
		train, test := labeled.SplitRand(.8)
		naive := eval.NewNaiveBayes(train)

		stats := []string{
			fmt.Sprintf("%f", duration.Seconds()),
			fmt.Sprintf("%f", naive.Validate(test))}
		fmt.Fprintln(out, strings.Join(stats, " "))
	}
}
