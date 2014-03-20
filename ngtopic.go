package main

import (
	"fmt"
	"time"

	"github.com/jlund3/modelt/topic/vanilla"

	"ford/load"
)

func main() {
	corpus := load.Newsgroups.Import()
	fmt.Println("Loaded")

	model := vanilla.NewLDA(corpus, 20, .1, .01)
	inferencer := vanilla.NewLDACCM(model)

	start := time.Now()
	for iter := 0; iter < 10; iter++ {
		inferencer.Inference()
		fmt.Println(iter, time.Now().Sub(start).Seconds())
	}

	for z := 0; z < model.T; z++ {
		fmt.Println(z, model.TopicSummaryString(z, 10))
	}
}
