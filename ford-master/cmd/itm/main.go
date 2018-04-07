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

var inferencers = map[string]func(*topic.ITM){
	"gibbs":    func(i *topic.ITM) { i.Gibbs() },
	"ccm":      func(i *topic.ITM) { i.CCM() },
	"top3":     func(i *topic.ITM) { i.SampleTopN(3) },
	"top5":     func(i *topic.ITM) { i.SampleTopN(5) },
	"top10":    func(i *topic.ITM) { i.SampleTopN(10) },
	"thresh10": func(i *topic.ITM) { i.Threshold(.1) },
	"thresh20": func(i *topic.ITM) { i.Threshold(.2) },
	"thresh30": func(i *topic.ITM) { i.Threshold(.3) },
	"coin25":   func(i *topic.ITM) { i.Coinflip(.25) },
	"coin75":   func(i *topic.ITM) { i.Coinflip(.75) },
	"coin50":   func(i *topic.ITM) { i.Coinflip(.5) }}

func main() {
	corpus := load.Newsgroups.Import()
	_, constraints := load.GetConstraints("data/constraints/newsgroups.txt")

	util.SeedNow()
	seed := rand.Int63()

	rand.Seed(seed)
	trainPart, testPart := util.CreateSplit(corpus.M, .8)

	for name, inference := range inferencers {
		rand.Seed(seed)

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
		for i := 0; i < 30; i++ {
			start := time.Now()
			inference(itm)
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
}
