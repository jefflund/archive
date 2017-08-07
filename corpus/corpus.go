package corpus

import (
	"github.com/jefflund/modelt/pipeline"
)

// Bible gets a Corpus containing the King James version of the Bible.
func Bible() *pipeline.Corpus {
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
			// TODO xrefs
		),
		pipeline.KeepFilterer(),
	}
	p.Tokenizer = pipeline.FrequencyTokenizer(p, 2, -1)
	return p.Run()
}

func Newsgroups() *pipeline.Corpus {
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
	return p.Run()
}

// Amazon gets a Corpus consisting of roughly 40,000 Amazon product reviews
// with associated star ratings.
func Amazon() *pipeline.Corpus {
	p := pipeline.Pipeline{
		DownloadInputer("amazon/amazon.txt"),
		pipeline.LineExtractor("\t"),
		pipeline.StopwordTokenizer(
			pipeline.DefaultTokenizer(),
			pipeline.ReadWordlist(OpenDownload("stopwords/english.txt")),
		),
		pipeline.CompositeLabeler(
			pipeline.TitleLabeler("id"),
			pipeline.MapLabeler(
				"stars",
				pipeline.ReadFloatLabels(
					OpenDownload("amazon/amazon.stars"),
					"\t",
				),
			),
		),
		pipeline.EmptyFilterer(),
	}
	p.Tokenizer = pipeline.FrequencyTokenizer(p, 50, -1)
	return p.Run()
}
