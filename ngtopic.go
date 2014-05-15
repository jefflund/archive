package main

import (
	"fmt"

	"github.com/jlund3/modelt/topic/vanilla"

	"ford/load"
)

func main() {
	corpus := load.Newsgroups.Import()
	lda := vanilla.NewLDA(corpus, 20, .1, .01)
	inference := vanilla.Gibbs(lda)

	for iter := 0; iter < 20; iter++ {
		inference()
	}

	for z := 0; z < lda.T; z++ {
		fmt.Printf("%d: %s\n", z, lda.TopicSummaryString(z, 10))
	}
}
