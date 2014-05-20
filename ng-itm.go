package main

import (
	"fmt"
	"math/rand"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/interactive"

	"github.com/jlund3/ford/load"
)

func main() {
	rand.Seed(123456789)

	corpus := load.Newsgroups.Import()
	itm := interactive.NewITM(corpus, 20, .1, .01, 100)
	inference := interactive.Gibbs(itm)

	for iter := 0; iter < 100; iter++ {
		inference()
	}

	for z := 0; z < itm.T; z++ {
		fmt.Printf("%d: %s\n", z, itm.TopicSummary(z, 10))
	}

	labeled := eval.NewLabeledCorpusModel(itm)
	train, test := labeled.SplitRand(.8)
	naive := eval.NewNaiveBayes(train)
	fmt.Printf("Accuracy: %f\n", naive.Validate(test))
}
