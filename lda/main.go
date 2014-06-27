package main

import (
	"runtime"

	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/vanilla"

	"github.com/jlund3/ford/load"
)

func main() {
	runtime.GOMAXPROCS(runtime.NumCPU())
	rand.Seed(time.Now().UnixNano())

	corpus := load.Newsgroups.Import()
	model := vanilla.NewLDA(corpus, 20, .1, .01)

	inference := vanilla.NBTrain(model, .975)

	for i := 0; i < 100; i++ {
		start := time.Now()
		inference()
		end := time.Now()

		fmt.Println("Iteration:", i, "Time:", end.Sub(start))

		start = time.Now()
		labeled := eval.NewLabeledCorpusModel(model)
		train, test := labeled.SplitRand(.9)
		naive := eval.NewNaiveBayes(train)
		fmt.Println("Accuracy:", naive.Validate(test))
		end = time.Now()
		fmt.Println("NB Time:", end.Sub(start))
		fmt.Println("Posterior:", model.Posterior())

		fmt.Println("Topics:")
		for z := 0; z < model.T; z++ {
			fmt.Println(z, model.Topics[z], model.TopicSummary(z, 10))
		}
	}
}
