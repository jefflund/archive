package load

import (
	"github.com/jlund3/modelt/pipeline"
)

type ImportSpec struct {
	Path      string
	Tokenizer pipeline.Tokenizer
	Stopwords []string
}

func (s ImportSpec) Run(c *pipeline.Corpus) *pipeline.Corpus {
	c, err := pipeline.FilterStopwordFiles(c, s.Stopwords...)
	if err != nil {
		panic(c)
	}
	return c
}

type Importer struct {
	ImportSpec

	importer func(ImportSpec) *pipeline.Corpus
}

func (i Importer) Import() *pipeline.Corpus {
	return i.importer(i.ImportSpec)
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
