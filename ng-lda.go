package main

import (
	"fmt"

	"github.com/jlund3/modelt/topic"
	"github.com/jlund3/modelt/topic/vanilla"

	"ford/load"
)

func main() {
	corpus := load.Newsgroups.Import()
	lda := vanilla.NewLDA(corpus, 20, .1, .01)
	inference := vanilla.CCM(lda)
	checker := topic.NewWordConvergenceChecker(lda.Z)

	for changes := -1; changes != 0; changes = checker.Check() {
		inference()
		fmt.Println(changes)
	}

	for z := 0; z < lda.T; z++ {
		fmt.Printf("%d: %s\n", z, lda.TopicSummaryString(z, 10))
	}
}
