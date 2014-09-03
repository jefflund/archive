package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/vanilla"

	"github.com/jlund3/ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	corpus := load.Newsgroups.Import()
	lda := vanilla.NewLDA(corpus, 20, .1, .01)
	//inference := vanilla.Gibbs(lda)
	inference := vanilla.VEM(lda)

	for iter := 0; iter < 100; iter++ {
		inference()

		for z := 0; z < lda.T; z++ {
			fmt.Printf("%d (%d): %s\n", z, lda.Topics[z], lda.TopicSummary(z, 10))
		}
		labeled := eval.NewLabeledCorpusModel(lda)
		train, test := labeled.SplitRand(.8)
		naive := eval.NewNaiveBayes(train)
		fmt.Printf("Accuracy: %f\n", naive.Validate(test))
		fmt.Printf("Posterior: %f\n", lda.Posterior())
	}

}
