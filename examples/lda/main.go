package main

import (
	"fmt"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/graphical/topic"
	"github.com/jlund3/modelt/util"

	"github.com/jlund3/ford/load"
)

func main() {
	util.SeedNow()

	corpus := load.Newsgroups.Import()
	lda := topic.NewLDA(corpus, 20, .1, .01)
	for i := 0; i < 100; i++ {
		lda.Gibbs()
	}

	labels := eval.NewLabelCorpusWordFeature(corpus, lda.Z)
	fmt.Println("Accuracty:", eval.Naive(labels.SplitRand(.8)))
	for z := 0; z < lda.T; z++ {
		fmt.Printf("%d: %s\n", z, lda.TopicSummary(z, 10))
	}
}
