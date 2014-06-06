package main

import (
	"fmt"
	"math"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/topic/vanilla"

	"github.com/jlund3/ford/load"
)

type RandomRestart struct {
	bestState  [][]int
	bestMetric float64
}

func NewRandomRestart() *RandomRestart {
	return &RandomRestart{nil, math.Inf(-1)}
}

func Sugs(l *vanilla.LDA) {
	l.AbolateAll()
	vanilla.Gibbs(l)()
}

func SaveState(l *vanilla.LDA) [][]int {
	state := make([][]int, l.M)
	for d, zd := range l.Z {
		state[d] = make([]int, len(zd))
		copy(state[d], l.Z[d])
	}
	return state
}

func main() {
	corpus := load.Newsgroups.Import()
	lda := vanilla.NewLDA(corpus, 20, .1, .01)

	restart := NewRandomRestart()
	for i := 0; i < 25; i++ {
		Sugs(lda)
		post := lda.Posterior()
		if post > restart.bestMetric {
			restart.bestState = SaveState(lda)
			restart.bestMetric = post
		}
	}

	for d, zd := range restart.bestState {
		for n, zdn := range zd {
			lda.SetZ(d, n, zdn)
		}
	}

	for z := 0; z < lda.T; z++ {
		fmt.Printf("%d (%d) - %s\n", z, lda.Topics[z], lda.TopicSummary(z, 10))
	}

	labeled := eval.NewLabeledCorpusModel(lda)
	train, test := labeled.SplitRand(.9)
	naive := eval.NewNaiveBayes(train)
	fmt.Println("Accuracy:", naive.Validate(test))
}
