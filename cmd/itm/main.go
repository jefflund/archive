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
	offset = flag.Int("offset", 0, "seed offset")
)

func RunWithSeed(run func(), seed int64) {
	reset := rand.Int63()
	rand.Seed(seed)
	run()
	rand.Seed(reset)
}

func main() {
	// Setup
	flag.Parse()
	if nodename := os.Getenv("PSSH_NODENUM"); nodename != "" {
		var nodenum int
		fmt.Sscanf(nodename, "%d", &nodenum)
		for i := 0; i < nodenum+*offset; i++ {
			rand.Int63()
		}
		rand.Seed(rand.Int63())
	} else {
		rand.Seed(time.Now().UnixNano())
	}

	// Create model
	corpus := load.Newsgroups.Import()
	itm := interactive.NewITM(corpus, 20, .1, .01, 100)
	itm.Ablate = interactive.AblateDoc
	gibbs := interactive.Gibbs(itm)
	for i := 0; i < 100; i++ {
		gibbs()
	}

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
	fmt.Fprintln(out, "#iter secs post accur changes")

	// Create eval
	evaluateSeed := rand.Int63()
	evaluate := func(iter int, duration time.Duration, changes int) {
		run := func() {
			labeled := eval.NewWordLabelCorpus(itm)
			train, test := labeled.SplitRand(.8)

			stats := []string{
				fmt.Sprintf("%d", iter),
				fmt.Sprintf("%f", duration.Seconds()),
				fmt.Sprintf("%f", itm.Posterior()),
				//fmt.Sprintf("%f", eval.NewNaiveBayes(train).Validate(test))}
				fmt.Sprintf("%f", eval.Wabbit(train, test)),
				fmt.Sprintf("%d", changes)}
			fmt.Fprintln(out, strings.Join(stats, " "))
		}
		RunWithSeed(run, evaluateSeed)
	}

	// Add constraints
	evaluate(0, 0, 0)
	_, constraints := load.GetConstraints("data/constraints/newsgroups.txt")
	for _, constraint := range constraints {
		itm.AddConstraintString(constraint)
	}

	// Setup inference
	inference := interactive.CCM(itm)
	checker := topic.NewWordConvergenceChecker(itm.Z)
	var duration time.Duration

	// Run inference
	for iter := 1; iter <= 100; iter++ {
		start := time.Now()
		inference()
		end := time.Now()
		duration += end.Sub(start)

		evaluate(iter, duration, checker.Check())
	}
}
