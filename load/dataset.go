package load

import (
	"github.com/jlund3/modelt/pipeline"
)

var (
	Newsgroups = Importer{
		ImportSpec{
			"data/newsgroups",
			pipeline.NewsTokenizer,
			[]string{
				"data/stopwords/newsgroups.txt",
				"data/stopwords/english.txt"}},
		ImportDir}

	Enron = Importer{
		ImportSpec{
			"data/enron/annotation.tab",
			pipeline.NewsTokenizer,
			[]string{"data/stopwords/english.txt"}},
		ImportIndex}
)
