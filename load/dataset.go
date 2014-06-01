package load

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
		LabelDir}

	Enron = Importer{
		ImportSpec{
			"data/enron/annotation.tab",
			pipeline.NewsTokenizer,
			[]string{"data/stopwords/english.txt"},
			false, 0},
		ImportIndex,
		nil}

	Ambiant  = createAmbiantImporters()
	Moresque = createMoresqueImporters()
)

func createQueryImport(filename string) Importer {
	return Importer{
		ImportSpec{
			filename,
			pipeline.BasicTokenizer,
			[]string{"data/stopwords/english.txt"},
			true, .8},
		ImportDir,
		LabelDir}
}

func createAmbiantImporters() []Importer {
	importers := make([]Importer, 44)
	for i := 0; i < 44; i++ {
		filename := fmt.Sprintf("data/ambiant/data/%d", i+1)
		importers[i] = createQueryImport(filename)
	}
	return importers
}

func createMoresqueImporters() []Importer {
	importers := make([]Importer, 114)
	for i := 0; i < 114; i++ {
		filename := fmt.Sprintf("data/moresque/data/%d", i+45)
		importers[i] = createQueryImport(filename)
	}
	return importers
}
