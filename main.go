package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/crpcluster"

	"ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	ambmean := &meancalc{}
	mormean := &meancalc{}
	allmean := &meancalc{}

	for {
		for topic := 0; topic < 44; topic++ {
			rand := run(load.Ambiant[topic])
			ambmean.observe(rand)
			allmean.observe(rand)
			fmt.Println(allmean.n, ambmean.mean(), mormean.mean(), allmean.mean())
		}
		for topic := 0; topic < 114; topic++ {
			rand := run(load.Moresque[topic])
			mormean.observe(rand)
			allmean.observe(rand)
			fmt.Println(allmean.n, ambmean.mean(), mormean.mean(), allmean.mean())
		}
	}
}

func run(i load.Importer) (rand float64) {
	corpus := i.Import()
	mm := crpcluster.NewCRPMM(corpus, 50, 20, 1, .01)
	inferencer := crpcluster.NewCRPMMCCM(mm)
	checker := crpcluster.NewCRPMMConvergenceChecker(mm)
	converged := false
	for !converged {
		inferencer.Inference()
		converged = checker.Check() == 0
	}
	gold := i.Label(corpus)
	pred := eval.NewClusteringCRPMM(mm)
	contingency := eval.NewContingency(gold, pred)
	return contingency.Rand()
}

type meancalc struct {
	sum float64
	n   int
}

func (m *meancalc) observe(x float64) {
	m.sum += x
	m.n += 1
}

func (m *meancalc) mean() float64 {
	return m.sum / float64(m.n)
}
