package main

import (
	"fmt"

	"github.com/jlund3/modelt/topic/interactive"

	"ford/load"
)

func main() {
	corpus := load.Newsgroups.Import()
	itm := interactive.NewITM(corpus, 20, .1, .01, 100)
	inference := interactive.Gibbs(itm)

	for iter := 0; iter < 100; iter++ {
		inference()
	}

	for z := 0; z < itm.T; z++ {
		fmt.Printf("%d: %s\n", z, itm.TopicSummaryString(z, 10))
	}
}
