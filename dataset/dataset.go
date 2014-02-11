package dataset

import (
	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
)

type ImportSpec struct {
	Path      string
	Tokenizer pipeline.Tokenizer
	Stopwords []string
	Stem      bool
	Common    float64
}

type Importer struct {
	spec     ImportSpec
	importer func(ImportSpec) *pipeline.Corpus
	labeler  func(*pipeline.Corpus, ImportSpec) *eval.Clustering
}

func (i Importer) Import() *pipeline.Corpus {
	return i.importer(i.spec)
}

func (i Importer) Label(c *pipeline.Corpus) *eval.Clustering {
	return i.labeler(c, i.spec)
}

func ImportDir(s ImportSpec) *pipeline.Corpus {
	corpus := pipeline.NewCorpus()
	err := pipeline.UpdateCorpusWalk(s.Path, s.Tokenizer, corpus)
	if err != nil {
		panic(err)
	}
	return runImportSpec(corpus, s)
}

func ImportIndex(s ImportSpec) *pipeline.Corpus {
	corpus := pipeline.NewCorpus()
	err := pipeline.UpdateCorpusIndex(s.Path, s.Tokenizer, corpus)
	if err != nil {
		panic(err)
	}
	return runImportSpec(corpus, s)
}

func runImportSpec(corpus *pipeline.Corpus, s ImportSpec) *pipeline.Corpus {
	corpus, err := pipeline.FilterStopwords(corpus, s.Stopwords...)
	if err != nil {
		panic(err)
	}

	if s.Stem {
		corpus, err = pipeline.ApplyStemming(corpus)
		if err != nil {
			panic(err)
		}
	}

	if s.Common > 0 {
		corpus, err = pipeline.RemoveCommon(corpus, s.Common)
		if err != nil {
			panic(err)
		}
	}

	return corpus
}

func ImportDirClustering(c *pipeline.Corpus, _ ImportSpec) *eval.Clustering {
	return eval.NewClusteringTitle(c)
}

func ImportIndexClustering(c *pipeline.Corpus, s ImportSpec) *eval.Clustering {
	return eval.NewClusteringIndexRef(s.Path, c)
}
