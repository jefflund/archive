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
	"github.com/jlund3/modelt/topic/vanilla"
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
	fmt.Fprintln(out, "#iter secs post accur ccm-secs ccm-post ccm-accur")

	// Create model
	corpus := load.Newsgroups.Import()
	lda := vanilla.NewLDA(corpus, 20, .1, .01)

	ccm := vanilla.CCM(lda)
	gibbs := vanilla.Gibbs(lda)
	checker := topic.NewWordConvergenceChecker(lda.Z)
	//angibbs := []topic.Inferencer{
	//vanilla.AnnealedGibbs(lda, 3),
	//vanilla.AnnealedGibbs(lda, 2.5),
	//vanilla.AnnealedGibbs(lda, 2),
	//vanilla.AnnealedGibbs(lda, 1.5),
	//vanilla.AnnealedGibbs(lda, 1)}
	//sel := vanilla.GreedySelect(lda)
	//softccm := vanilla.SoftWordCCM(lda, 5)

	// Run inference
	iters := 1000
	var dur time.Duration
	for i := 1; i <= iters; i++ {
		start := time.Now()
		gibbs()
		end := time.Now()
		dur += end.Sub(start)

		checker.Check()

		labeled := eval.NewLabeledCorpusModel(lda)
		train, test := labeled.SplitRand(.8)
		naive := eval.NewNaiveBayes(train)

		stats := []string{
			fmt.Sprintf("%d", i),
			fmt.Sprintf("%f", dur.Seconds()),
			fmt.Sprintf("%f", lda.Posterior()),
			fmt.Sprintf("%f", naive.Validate(test))}

		checker.Check()
		start = time.Now()
		ccm()
		end = time.Now()

		labeled = eval.NewLabeledCorpusModel(lda)
		train, test = labeled.SplitRand(.8)
		naive = eval.NewNaiveBayes(train)

		stats = append(stats,
			fmt.Sprintf("%f", (dur+end.Sub(start)).Seconds()),
			fmt.Sprintf("%f", lda.Posterior()),
			fmt.Sprintf("%f", naive.Validate(test)))

		fmt.Fprintln(out, strings.Join(stats, " "))

		for d := 0; d < lda.M; d++ {
			for n := 0; n < lda.N[d]; n++ {
				lda.SetZ(d, n, checker.State[d][n])
			}
		}
	}
}
