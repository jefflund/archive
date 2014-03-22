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
	ccmInferencer := vanilla.NewLDACCM(model)
	pbccmInferencer := vanilla.NewLDAPBlockCCM(model)

	runner(model, ccmInferencer)
	runner(model, pbccmInferencer)
	runner(model, ccmInferencer)
}

func runner(model *vanilla.LDA, inferencer vanilla.LDAInfrencer) {
	for iter := 0; iter < 10; iter++ {
		start := time.Now()
		inferencer.Inference()
		end := time.Now()
		fmt.Println(iter, end.Sub(start).Seconds())
	}
	for z := 0; z < model.T; z++ {
		fmt.Println(z, model.TopicSummaryString(z, 10))
	}
}
