package main

import (
	"fmt"
	"io/ioutil"
	"math/rand"
	"strings"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/graphical/topic"
	"github.com/jlund3/modelt/util"

	"github.com/jlund3/ford/load"
)

func main() {
	corpus := load.Newsgroups.Import()
	_, constraints := load.GetConstraints("data/constraints/newsgroups.txt")

	util.SeedNow()
	seed := rand.Int63()

	rand.Seed(seed)
	trainPart, testPart := util.CreateSplit(corpus.M, .8)

	rand.Seed(seed)

	name := "annealing"
	dirpath := "scratch/output/" + name + "/"
	util.EnsureDir(dirpath)
	out, err := ioutil.TempFile(dirpath, "")
	if err != nil {
		panic(err)
	}
	defer out.Close()

	fmt.Fprintln(out, "# seed ", seed)
	fmt.Fprintln(out, "# iter duration changes wz-naive wz-wabbit w-naive w-wabbit post")

	itm := topic.NewITM(corpus, 20, .1, .01, 100)
	for i := 0; i < 100; i++ {
		itm.Gibbs()
	}

	for _, constraint := range constraints {
		itm.AddConstraintString(constraint)
	}

	checker := topic.NewConvergenceCheck(itm.Z)
	var duration time.Duration

	epoc := func(iters int, temp float64) {
		fmt.Fprintln(out, "# temp %f", temp)
		for i := 0; i < iters; i++ {
			start := time.Now()
			itm.AnnealedGibbs(temp)
			end := time.Now()
			duration += end.Sub(start)

			wzLabeled := eval.NewLabelCorpusWordFeature(corpus, itm.Z)
			wzTrain, wzTest := wzLabeled.Split(trainPart, testPart)

			zLabeled := eval.NewLabeledCorpusFeature(corpus, itm.Z)
			zTrain, zTest := zLabeled.Split(trainPart, testPart)

			stats := []string{
				fmt.Sprintf("%d", i),
				fmt.Sprintf("%f", duration.Seconds()),
				fmt.Sprintf("%d", checker.Check()),

				fmt.Sprintf("%f", eval.Naive(wzTrain, wzTest)),
				fmt.Sprintf("%f", eval.Wabbit(wzTrain, wzTest)),

				fmt.Sprintf("%f", eval.Naive(zTrain, zTest)),
				fmt.Sprintf("%f", eval.Wabbit(zTrain, zTest)),

				fmt.Sprintf("%f", itm.Posterior())}

			fmt.Fprintln(out, strings.Join(stats, " "))
		}
	}

	epoc(20, 10)
	epoc(20, 5)
	epoc(20, 2.5)
	epoc(20, 2)
	epoc(20, 1.75)
	epoc(20, 1.5)
	epoc(20, 1.25)
	epoc(20, 1.1)
	epoc(20, 1)
	epoc(20, .75)
	epoc(20, .5)
	epoc(20, .25)
	epoc(20, .1)
	epoc(20, .01)
}
