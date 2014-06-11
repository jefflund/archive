package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic"
	"github.com/jlund3/modelt/topic/interactive"

	"github.com/jlund3/ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	corpus := load.Newsgroups.Import()
	rounds, constraints := load.GetConstraints("data/constraints/newsgroups.txt")

	model := interactive.NewITM(corpus, 20, .1, .01, 100)

	evaluate := func(name string, round int) {
		labeled := eval.NewLabeledCorpusModel(model)
		train, test := labeled.SplitRand(.9)
		naive := eval.NewNaiveBayes(train)
		fmt.Println(name, round, ":", naive.Validate(test))
	}

	inference := interactive.Gibbs(model)
	for i := 0; i < 100; i++ {
		inference()
	}
	base := model.CopyState()
	evaluate("Base", 0)

	newModel := func() *interactive.ITM {
		i := interactive.NewITM(corpus, 20, .1, .01, 100)
		i.SetState(base)
		return i
	}

	model = newModel()
	inference = interactive.Gibbs(model)
	for round := 1; round < rounds; round++ {
		for _, constraint := range constraints {
			model.AddConstraintString(constraint[:round])
		}
		for i := 0; i < 10; i++ {
			inference()
		}
		evaluate("Gibbs", round)
	}

	model = newModel()
	inference = interactive.CMM(model)
	for round := 1; round < rounds; round++ {
		for _, constraint := range constraints {
			model.AddConstraintString(constraint[:round])
		}
		converged := false
		checker := topic.NewWordConvergenceChecker(model.Z)
		for !converged {
			inference()
			converged = checker.Check() == 0
		}
		evaluate("CCM", round)
	}

	model = newModel()
	inference = interactive.CMM(model)
	for round := 1; round < rounds; round++ {
		for _, constraint := range constraints {
			model.AddConstraintString(constraint[:round])
		}
		interactive.AbolateAll(model, nil)
		inference()
		evaluate("SUGS", round)
	}

	model = newModel()
	inference = interactive.CMM(model)
	for round := 1; round < rounds; round++ {
		for _, constraint := range constraints {
			model.AddConstraintString(constraint[:round])
		}
		interactive.AbolateAll(model, nil)
		converged := false
		checker := topic.NewWordConvergenceChecker(model.Z)
		for !converged {
			inference()
			converged = checker.Check() == 0
		}
		evaluate("SUGS-CCM", round)
	}
}
