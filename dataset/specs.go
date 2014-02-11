package dataset

import (
	"fmt"

	"github.com/jlund3/modelt/pipeline"
)

var (
	Newsgroups = Importer{
		ImportSpec{
			"data/newsgroups",
			pipeline.NewsTokenizer,
			[]string{
				"data/stopwords/newsgroups.txt",
				"data/stopwords/english.txt"},
			false},
		ImportDir,
		ImportDirClustering}

	Enron = Importer{
		ImportSpec{
			"data/enron/annotation.tab",
			pipeline.NewsTokenizer,
			[]string{"data/stopwords/english.txt"},
			false},
		ImportIndex,
		ImportIndexClustering}

	Ambiant  = createAmbiantImporters()
	Moresque = createMoresqueImporters()
)

func createAmbiantImporters() []Importer {
	importers := make([]Importer, 44)
	for i := 0; i < 44; i++ {
		importers[i] = Importer{
			ImportSpec{
				fmt.Sprintf("data/ambiant/data/%d", i+1),
				pipeline.BasicTokenizer,
				[]string{"data/stopwords/english.txt"},
				true},
			ImportDir,
			ImportDirClustering}
	}
	return importers
}

func createMoresqueImporters() []Importer {
	importers := make([]Importer, 114)
	for i := 0; i < 114; i++ {
		importers[i] = Importer{
			ImportSpec{
				fmt.Sprintf("data/moresque/data/%d", i+45),
				pipeline.BasicTokenizer,
				[]string{"data/stopwords/english.txt"},
				true},
			ImportDir,
			ImportDirClustering}
	}
	return importers
}
