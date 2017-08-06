package corpus

import (
	"github.com/jefflund/modelt/pipeline"
)

// Bible gets a Corpus containing the King James version of the Bible.
func Bible() *pipeline.Corpus {
	p := pipeline.Pipeline{
		DownloadInputer("bible/bible.txt"),
		pipeline.LineExtractor(" "),
		pipeline.DefaultTokenizer(),
		pipeline.TitleLabeler("verse"),
		pipeline.KeepFilterer(),
	}
	return p.Run()
}

func Newsgroups() *pipeline.Corpus {
	p := pipeline.Pipeline{
		DownloadInputer("newsgroups/newsgroups.tar.gz"),
		pipeline.SkipExtractor("\n\n"),
		pipeline.DefaultTokenizer(),
		pipeline.CompositeLabeler(
			pipeline.TitleLabeler("title"),
			pipeline.DirLabeler("newsgroup"),
		),
		pipeline.KeepFilterer(),
	}
	return p.Run()
}

func Amazon() *pipeline.Corpus {
	p := pipeline.Pipeline{
		DownloadInputer("amazon/amazon.txt"),
		pipeline.LineExtractor("\t"),
		pipeline.DefaultTokenizer(),
		pipeline.CompositeLabeler(
			pipeline.TitleLabeler("id"),
		),
		pipeline.KeepFilterer(),
	}
	return p.Run()
}
