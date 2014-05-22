package main

import (
	"fmt"
	"math/rand"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/vanilla"

	"github.com/jlund3/ford/load"
)

func main() {
	rand.Seed(123456789)

	corpus := load.Newsgroups.Import()
	lda := vanilla.NewLDA(corpus, 20, .1, .01)
	inference := vanilla.Gibbs(lda)

	for iter := 0; iter < 100; iter++ {
		inference()
	}

	for z := 0; z < lda.T; z++ {
		fmt.Printf("%d: %s\n", z, lda.TopicSummary(z, 10))
	}

	labeled := eval.NewLabeledCorpusModel(lda)
	train, test := labeled.SplitRand(.8)
	naive := eval.NewNaiveBayes(train)
	fmt.Printf("Accuracy: %f\n", naive.Validate(test))
}
