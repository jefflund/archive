package main

import (
	"fmt"
	"math/rand"
	"time"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
	"github.com/jlund3/modelt/topic/cluster"
	"github.com/jlund3/modelt/topic/crpcluster"

	"ford/load"
)

func main() {
	rand.Seed(time.Now().UnixNano())

	mmmean := &meancalc{}
	crpmean := &meancalc{}
	tmean := &meancalc{}

	commonmean := &meancalc{}
	commontmean := &meancalc{}

	for {
		for topic := 0; topic < 44; topic++ {
			importer := load.Ambiant[topic]
			corpus := importer.Import()
			gold := importer.Label(corpus)

			mmmean.observe(runMM(corpus, gold))
			r, t := runCRP(corpus, gold)
			crpmean.observe(r)
			tmean.observe(float64(t))

			common := filterCommon(corpus, gold)
			commonGold := importer.Label(common)
			r, t = runCRP(common, commonGold)
			commonmean.observe(r)
			commontmean.observe(float64(t))

			fmt.Printf("MM:%.3f CRP:%s (%.3f) Common:%s (%.3f)\n",
				mmmean.mean(), color(crpmean.mean()), tmean.mean(),
				color(commonmean.mean()), commontmean.mean())
		}
		fmt.Println("***")
	}
}

func color(x float64) string {
	return fmt.Sprintf("\x1b[38;5;192m%.3f\x1b[0m", x)
}

func runMM(c *pipeline.Corpus, g *eval.Clustering) (rand float64) {
	mm := cluster.NewMM(c, 20, 1, .01)
	inferencer := cluster.NewMMCCM(mm)
	for i := 0; i < 2; i++ {
		inferencer.Inference()
	}
	pred := eval.NewClusteringMM(mm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand()
}

func runCRP(c *pipeline.Corpus, g *eval.Clustering) (rand float64, T int) {
	crpmm := crpcluster.NewCRPMM(c, 50, 1, .001, .1)
	//inferencer := crpcluster.NewCRPMMGibbs(crpmm)
	inferencer := crpcluster.NewCRPMMCCM(crpmm)
	for i := 0; i < 2; i++ {
		inferencer.Inference()
	}
	pred := eval.NewClusteringCRPMM(crpmm)
	contingency := eval.NewContingency(g, pred)
	return contingency.Rand(), crpmm.T - len(g.Labels)
}

func mostCommon(g *eval.Clustering) interface{} {
	maxCount := -1
	maxLabel := g.Data[0]
	counts := make(map[interface{}]int)
	for _, label := range g.Data {
		counts[label]++
		if counts[label] > maxCount {
			maxCount = counts[label]
			maxLabel = label
		}
	}
	return maxLabel
}

func filterCommon(c *pipeline.Corpus, g *eval.Clustering) *pipeline.Corpus {
	common := mostCommon(g)
	filtered := pipeline.NewCorpus()
	for d, label := range g.Data {
		if label == common {
			tokens := make([]string, c.N[d])
			for n, v := range c.W[d] {
				tokens[n] = c.Vocab.Tokens[v]
			}
			filtered.AddDocument(c.Titles[d], tokens)
		}
	}
	return filtered
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
