package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
	"github.com/jlund3/modelt/topic"
	"github.com/jlund3/modelt/topic/interactive"

	"github.com/jlund3/ford/load"
)

func evaluate(itm *interactive.ITM) {
	for z := 0; z < itm.T; z++ {
		fmt.Printf("%d: %s\n", z, itm.TopicSummary(z, 10))
	}

	labeled := eval.NewLabeledCorpusModel(itm)
	train, test := labeled.SplitRand(.8)
	naive := eval.NewNaiveBayes(train)
	fmt.Printf("Accuracy: %f\n", naive.Validate(test))
}

func run(itm *interactive.ITM, inference topic.Inferencer, iters int) {
	for i := 0; i < iters; i++ {
		inference()
	}
	evaluate(itm)
}

var (
	firstStep = map[string]interactive.Abolator{
		"noop": interactive.AbolateNoOp,
		"term": interactive.AbolateTerm,
		"rand": interactive.AbolateTermRand}
	secondStep = map[string]interactive.Abolator{
		"noop":  interactive.AbolateNoOp,
		"ccm":   interactive.AbolateTermCCM,
		"gibbs": interactive.AbolateTermGibbs}
)

func experiment(c *pipeline.Corpus, u [][]string, first, second string, seed int64) {
	rand.Seed(seed)
	fmt.Printf("**** %s %s ****", first, second)

	itm := interactive.NewITM(c, 20, .1, .01, 100)
	itm.Abolate = interactive.ComposeAbolation(firstStep[first], secondStep[second])
	inference := interactive.Gibbs(itm)

	run(itm, inference, 100)
	evaluate(itm)

	for _, constraint := range u {
		itm.AddConstraintString(constraint)
	}

	run(itm, inference, 30)
	evaluate(itm)
}

func main() {
	seed := time.Now().UnixNano()

	corpus := load.Newsgroups.Import()
	constraints := load.GetConstraints("data/constraints/newsgroups.txt")

	for first := range firstStep {
		for second := range secondStep {
			experiment(corpus, constraints, first, second, seed)
		}
	}
}
