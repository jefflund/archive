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
			false, 0},
		ImportDir,
		ImportDirClustering}

	Enron = Importer{
		ImportSpec{
			"data/enron/annotation.tab",
			pipeline.NewsTokenizer,
			[]string{"data/stopwords/english.txt"},
			false, 0},
		ImportIndex,
		ImportIndexClustering}

	Ambiant  = createAmbiantImporters()
	Moresque = createMoresqueImporters()
)

func createQueryImporter(filename string) Importer {
	return Importer{
		ImportSpec{
			filename,
			pipeline.BasicTokenizer,
			[]string{"data/stopwords/english.txt"},
			true, .8},
		ImportDir,
		ImportDirClustering}
}

func createAmbiantImporters() []Importer {
	importers := make([]Importer, 44)
	for i := 0; i < 44; i++ {
		filename := fmt.Sprintf("data/ambiant/data/%d", i+1)
		importers[i] = createQueryImporter(filename)
	}
	return importers
}

func createMoresqueImporters() []Importer {
	importers := make([]Importer, 114)
	for i := 0; i < 114; i++ {
		filename := fmt.Sprintf("data/moresque/data/%d", i+45)
		importers[i] = createQueryImporter(filename)
	}
	return importers
}
