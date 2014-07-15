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
	fmt.Fprintln(out, "#iter secs post accur")

	// Create model
	corpus := load.Newsgroups.Import()
	lda := vanilla.NewLDA(corpus, 20, .1, .01)
	inference := vanilla.Gibbs(lda)

	for i := 1; i <= 100; i++ {
		inference()
	}

	inference = vanilla.CCM(lda)

	// Run inference
	start := time.Now()
	for i := 1; i <= 100; i++ {
		inference()

		if i%10 == 0 {
			labeled := eval.NewLabeledCorpusModel(lda)
			train, test := labeled.SplitRand(.8)
			naive := eval.NewNaiveBayes(train)

			stats := []string{
				fmt.Sprintf("%d", i),
				fmt.Sprintf("%f", time.Now().Sub(start).Seconds()),
				fmt.Sprintf("%f", lda.Posterior()),
				fmt.Sprintf("%f", naive.Validate(test))}
			fmt.Fprintln(out, strings.Join(stats, " "))
		}
	}
}
