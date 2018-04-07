package load

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

func (s ImportSpec) Run(c *pipeline.Corpus) *pipeline.Corpus {
	c, err := pipeline.FilterStopwordFiles(c, s.Stopwords...)
	if err != nil {
		panic(c)
	}

	if s.Stem {
		c = pipeline.ApplyStemming(c)
	}

	if s.Common > 0 {
		c = pipeline.FilterCommon(c, s.Common)
	}

	return c
}

type Importer struct {
	ImportSpec

	importer func(ImportSpec) *pipeline.Corpus
	labeler  func(*pipeline.Corpus) *eval.Clustering
}

func (i Importer) Import() *pipeline.Corpus {
	return i.importer(i.ImportSpec)
}

func (i Importer) Label(c *pipeline.Corpus) *eval.Clustering {
	return i.labeler(c)
}

func ImportDir(s ImportSpec) *pipeline.Corpus {
	c := pipeline.NewCorpus()
	err := c.UpdateWalk(s.Path, s.Tokenizer)
	if err != nil {
		panic(c)
	}
	return s.Run(c)
}

func ImportIndex(s ImportSpec) *pipeline.Corpus {
	c := pipeline.NewCorpus()
	err := c.UpdateIndex(s.Path, s.Tokenizer)
	if err != nil {
		panic(err)
	}
	return s.Run(c)
}

func LabelDir(c *pipeline.Corpus) *eval.Clustering {
	return eval.NewClusteringTitle(c)
}
