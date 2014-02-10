package dataset

import (
	"github.com/jlund3/modelt/eval"
	"github.com/jlund3/modelt/pipeline"
)

type ImportSpec struct {
	Path      string
	Tokenizer *pipeline.Tokenizer
	Stopwords []string
}

func ImportDir(s ImportSpec) *pipeline.Corpus {
	corpus := pipeline.NewCorpus()

	err := pipeline.UpdateCorpusWalk(s.Path, s.Tokenizer, corpus)
	if err != nil {
		panic(err)
	}

	corpus, err = pipeline.FilterStopwords(corpus, s.Stopwords...)
	if err != nil {
		panic(err)
	}

	return corpus
}

func ImportDirClustering(c *pipeline.Corpus, _ ImportSpec) *eval.Clustering {
	return eval.NewClusteringTitle(c)
}

func ImportIndex(s ImportSpec) {
	corpus := pipeline.NewCorpus()

	err := pipeline.UpdateCorpusIndex(s.Path, s.Tokenizer, corpus)
	if err != nil {
		panic(err)
	}

	corpus, err = pipeline.FilterStopwords(corpus, s.Stopwords...)
	if err != nil {
		panic(err)
	}

	return corpus
}

func ImportIndexClustering(c *pipeline.Corpus, s ImportSpec) *eval.Clustering {
	return eval.NewClusteringIndex(s.Path, c)
}
