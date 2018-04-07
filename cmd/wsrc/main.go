package main

import (
	"fmt"
	"strings"

	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/graphical"
	"github.com/jlund3/modelt/graphical/cluster"
	"github.com/jlund3/modelt/pipeline"

	"github.com/jlund3/ford/load"
)

/*
Algorithms: DPMOM-CCM DPMOM-Gibbs DPMOM-SUGS MOM-CCM MOM-Gibbs
Datasets: AMB MOR ALL
Measure: Rand SR@3 SR@5 SR@10 SR@10 SR@15 SR@20
*/

var Ks = [5]int{3, 5, 10, 15, 20}

type Stats struct {
	Rand   float64
	Recall [5]float64
}

type Averager struct {
	Sum float64
	N   float64
}

func (a *Averager) Observe(x float64) {
	a.Sum += x
	a.N += 1
}

func (a *Averager) Average() float64 {
	return a.Sum / a.N
}

type CumulativeStats struct {
	Rand   *Averager
	Recall [5]*Averager
}

func NewCumStats() CumulativeStats {
	c := CumulativeStats{}
	c.Rand = new(Averager)
	for i := range c.Recall {
		c.Recall[i] = new(Averager)
	}
	return c
}

func (c CumulativeStats) Observe(s Stats) {
	c.Rand.Observe(s.Rand)
	for i, recall := range s.Recall {
		c.Recall[i].Observe(recall)
	}
}

func (c CumulativeStats) String() string {
	stats := []float64{
		c.Rand.Average(),
		c.Recall[0].Average(),
		c.Recall[1].Average(),
		c.Recall[2].Average(),
		c.Recall[3].Average(),
		c.Recall[4].Average()}
	strStats := make([]string, len(stats))
	for i, stat := range stats {
		strStats[i] = fmt.Sprintf("%f", stat)
	}
	return strings.Join(strStats, " ")
}

type Runner func(*pipeline.Corpus) []int

func (r Runner) EvalQuery(importer load.Importer) Stats {
	corpus := importer.Import()
	gold := importer.Label(corpus)

	z := r(corpus)

	pred := eval.NewClusteringState(z)
	cont := eval.NewContingency(gold, pred)
	rerank := eval.Rerank(z)

	stats := Stats{}
	stats.Rand = cont.Rand()
	for i, k := range Ks {
		stats.Recall[i] = eval.SRecallK(k, rerank, gold)
	}
	return stats
}

func (r Runner) Eval() string {
	ambStats := NewCumStats()
	morStats := NewCumStats()
	allStats := NewCumStats()

	for _, importer := range load.Ambiant {
		stats := r.EvalQuery(importer)
		ambStats.Observe(stats)
		allStats.Observe(stats)
	}
	for _, importer := range load.Moresque {
		stats := r.EvalQuery(importer)
		morStats.Observe(stats)
		allStats.Observe(stats)
	}

	stats := []string{
		fmt.Sprintf("%f", ambStats.Rand.Average()),
		fmt.Sprintf("%f", morStats.Rand.Average()),
		fmt.Sprintf("%f", allStats.Rand.Average()),
		fmt.Sprintf("%f", allStats.Recall[0].Average()),
		fmt.Sprintf("%f", allStats.Recall[1].Average()),
		fmt.Sprintf("%f", allStats.Recall[2].Average()),
		fmt.Sprintf("%f", allStats.Recall[3].Average()),
		fmt.Sprintf("%f", allStats.Recall[4].Average())}
	return strings.Join(stats, " ")
}

func DPMOM(c *pipeline.Corpus) *cluster.DPMoM {
	return cluster.NewDPMoM(c, 1, 20, .2, .2)
}

func DPCCM(c *pipeline.Corpus) []int {
	dpmom := DPMOM(c)
	graphical.RunCCM(dpmom, cluster.NewConvergenceCheck(dpmom.Z), 100)
	return dpmom.Z
}

func DPGibbs(c *pipeline.Corpus) []int {
	dpmom := DPMOM(c)
	graphical.RunGibbs(dpmom, 30)
	return dpmom.Z
}

func DPSUGS(c *pipeline.Corpus) []int {
	dpmom := DPMOM(c)
	dpmom.SUGS()
	return dpmom.Z
}

func MOM(c *pipeline.Corpus) *cluster.MoM {
	return cluster.NewMoM(c, 20, .01, .01)
}

func MMCCM(c *pipeline.Corpus) []int {
	mom := MOM(c)
	graphical.RunCCM(mom, cluster.NewConvergenceCheck(mom.Z), 0)
	return mom.Z
}

func MMGibbs(c *pipeline.Corpus) []int {
	mom := MOM(c)
	graphical.RunGibbs(mom, 30)
	return mom.Z
}

func Experiment(prefix string, r func(*pipeline.Corpus) []int) {
	fmt.Println(prefix+"\t", Runner(r).Eval())
}

func main() {
	fmt.Println("# Algorithm      Amb-Rand Mor-Rand All-Rand SR@3     SR@5     SR@10    SR@15    SR@20")
	fmt.Println("MST              0.815    0.867    0.852    0.547    0.656    0.792    0.867    0.907")
	fmt.Println("KeySRC           0.665    0.558    0.588    0.443    0.558    0.720    0.791    0.832")

	Experiment("DPMoM-CCM", DPCCM)
	Experiment("DPMoM-Gibbs", DPGibbs)
	Experiment("DPMoM-SUGS", DPSUGS)

	Experiment("MoM-CCM  ", MMCCM)
	Experiment("MoM-Gibbs", MMGibbs)
}
