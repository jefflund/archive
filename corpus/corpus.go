package corpus

import (
	"encoding/gob"
	"os"

	"github.com/jefflund/modelt/pipeline"
)

// getCorpus reads a gob encoded Corpus from disk, or constructs a new
// SliceCorpus using the Pipeline and writes the gob encoded result to disk.
func getCorpus(p pipeline.Pipeline, name string) pipeline.Corpus {
	gob.Register(&pipeline.SliceCorpus{})
	path := getPath(name)
	var c pipeline.Corpus

	// Try to simply read the Corpus from disk.
	file, err := os.Open(path)
	if err == nil {
		if err := gob.NewDecoder(file).Decode(&c); err != nil {
			panic(err)
		}
		return c
	}

	// Fail if gob file exists, but we still errored.
	if !os.IsNotExist(err) {
		panic(err)
	}

	// File does not exist, so we must run Pipeline and save Corpus.
	c = p.RunSlice()
	file, err = os.Create(path)
	if err != nil {
		panic(err)
	}
	if err := gob.NewEncoder(file).Encode(&c); err != nil {
		panic(err)
	}
	return c
}

// Bible gets a Corpus containing the King James version of the Bible. Each
// Document is labeled with a verse and xrefs.
func Bible() pipeline.Corpus {
	p := pipeline.Pipeline{
		DownloadInputer("bible/bible.txt"),
		pipeline.LineExtractor(" "),
		pipeline.StopwordTokenizer(
			pipeline.DefaultTokenizer(),
			pipeline.ReadWordlist(
				OpenDownload("stopwords/english.txt"),
				OpenDownload("stopwords/jacobean.txt"),
			),
		),
		pipeline.CompositeLabeler(
			pipeline.TitleLabeler("verse"),
			pipeline.ReadSliceLabeler(
				"xrefs",
				OpenDownload("bible/xref.txt"),
				"\t", ",",
			),
		),
		pipeline.KeepFilterer(),
	}
	p.Tokenizer = pipeline.FrequencyTokenizer(p, 2, -1)
	return getCorpus(p, "bible.gob")
}

// Newsgroups gets a Corpus consisting of roughly 20,000 usenet postings from
// 20 different newgroups in the early 1990s. Each Document is labeled with a
// filename and newsgroup.
func Newsgroups() pipeline.Corpus {
	p := pipeline.Pipeline{
		DownloadInputer("newsgroups/newsgroups.tar.gz"),
		pipeline.TarGzipExtractor(pipeline.SkipExtractor("\n\n")),
		pipeline.RegexpRemoveTokenizer(
			pipeline.StopwordTokenizer(
				pipeline.DefaultTokenizer(),
				pipeline.ReadWordlist(OpenDownload("stopwords/english.txt")),
			),
			`^(.{0,2}|.{15,})$`, // Remove any token t for which 2<len(t)<=15.
		),
		pipeline.CompositeLabeler(
			pipeline.TitleLabeler("filename"),
			pipeline.DirLabeler("newsgroup"),
		),
		pipeline.EmptyFilterer(),
	}
	p.Tokenizer = pipeline.FrequencyTokenizer(p, 50, 2000)
	return getCorpus(p, "newsgroups.gob")
}

// Amazon gets a Corpus consisting of roughly 40,000 Amazon product reviews
// with associated star ratings. Each Document is labeled with an id and stars.
func Amazon() pipeline.Corpus {
	p := pipeline.Pipeline{
		DownloadInputer("amazon/amazon.txt"),
		pipeline.LineExtractor("\t"),
		pipeline.StopwordTokenizer(
			pipeline.DefaultTokenizer(),
			pipeline.ReadWordlist(OpenDownload("stopwords/english.txt")),
		),
		pipeline.CompositeLabeler(
			pipeline.TitleLabeler("id"),
			pipeline.ReadFloatLabeler(
				"stars",
				OpenDownload("amazon/amazon.stars"),
				"\t",
			),
		),
		pipeline.EmptyFilterer(),
	}
	p.Tokenizer = pipeline.FrequencyTokenizer(p, 50, -1)
	return getCorpus(p, "amazon.gob")
}
