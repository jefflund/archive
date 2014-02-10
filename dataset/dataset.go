package dataset

import (
	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
)

type ImportSpec struct {
	Root      string
	Tokenizer *pipeline.Tokenizer
	Stopwords []string
}

func ImportDir(s ImportSpec) *pipeline.Corpus {
	corpus := pipeline.NewCorpus()

	err := pipeline.UpdateCorpusWalk(s.Root, s.Tokenizer, corpus)
	if err != nil {
		panic(err)
	}

	corpus, err = pipeline.FilterStopwords(corpus, s.Stopwords...)
	if err != nil {
		panic(err)
	}

	return corpus
}
