package pipeline

import (
	"encoding/gob"
	"io"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
)

// DataDir is the directory in which corpus data will be saved. The default
// value is "$HOME/.modelt". Override this before calling any corpus functions
// to customize this location.
var DataDir = filepath.Join(os.Getenv("HOME"), ".modelt")

// getPath computes the path of a named data item using DataDir.
func getPath(name string) string {
	return filepath.Join(DataDir, name)
}

// BaseURL is the base URL from which data should be downloaded. Override this
// before calling any corpus functions to customize this location. However,
// beware that if the URL points to a destination which does not have the
// expected directory structure, then the various Download* functions may
// panic. See DownloadInputer for more information on the expected directory
// contents.
var BaseURL = "https://github.com/jefflund/data/raw/data2/"

// getURL computes the url of a named data itme using BaseURL.
func getURL(name string) string {
	u, err := url.Parse(BaseURL)
	if err != nil {
		panic(err)
	}
	u, err = u.Parse(name)
	if err != nil {
		panic(err)
	}
	return u.String()
}

func ensureDownload(url, path string) {
	// If the file exists, we are done.
	_, err := os.Stat(path)
	if err == nil {
		return
	}
	if !os.IsNotExist(err) {
		panic(err)
	}

	// Ensure the path directory exists.
	if err := os.MkdirAll(filepath.Dir(path), 0755); err != nil && !os.IsExist(err) {
		panic(err)
	}

	// Create the file, and save url to that file.
	file, err := os.Create(path)
	if err != nil {
		panic(err)
	}
	defer file.Close()
	resp, err := http.Get(url)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	_, err = io.Copy(file, resp.Body)
	if err != nil {
		panic(err)
	}
}

// OpenDowload gets a NameReader for the given name, downloading the data from
// BaseURL to DataDir if needed.
func OpenDownload(name string) NameReader {
	url := getURL(name)
	path := getPath(name)
	ensureDownload(url, path)

	file, err := os.Open(path)
	if err != nil {
		panic(err)
	}
	return file
}

// getCorpus reads a gob encoded SliceCorpus from disk, or constructs a new
// SliceCorpus using the Pipeline and writes the gob encoded result to disk.
func getCorpus(p Pipeline, name string) Corpus {
	gob.Register(&SliceCorpus{})
	path := getPath(name)
	var c Corpus

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
	c = NewSliceCorpus(p)
	file, err = os.Create(path)
	if err != nil {
		panic(err)
	}
	if err := gob.NewEncoder(file).Encode(&c); err != nil {
		panic(err)
	}
	return c
}

// DownloadBible gets a Corpus containing the King James version of the Bible.
// Each Document is labeled with a verse and xrefs. The data and Corpus are
// saved to DataDir for reuse.
func DownloadBible() Corpus {
	p := Pipeline{
		DownloadInputer("bible/bible.txt"),
		LineExtractor(" "),
		StopwordTokenizer(
			DefaultTokenizer(),
			ReadWordlist(
				OpenDownload("stopwords/english.txt"),
				OpenDownload("stopwords/jacobean.txt"),
			),
		),
		CompositeLabeler(
			TitleLabeler("verse"),
			ReadSliceLabeler(
				"xrefs",
				OpenDownload("bible/xref.txt"),
				"\t", ",",
			),
		),
		KeepFilterer(),
	}
	p.Tokenizer = FrequencyTokenizer(p, 2, -1)
	return getCorpus(p, "bible.gob")
}

// DownloadNewsgroups gets a Corpus consisting of roughly 20,000 usenet
// postings from 20 different newgroups in the early 1990s. Each Document is
// labeled with a filename and newsgroup. The data and Corpus are saved to
// DataDir for reuse.
func DownloadNewsgroups() Corpus {
	p := Pipeline{
		DownloadInputer("newsgroups/newsgroups.tar.gz"),
		TarGzipExtractor(SkipExtractor("\n\n")),
		RegexpRemoveTokenizer(
			StopwordTokenizer(
				DefaultTokenizer(),
				ReadWordlist(OpenDownload("stopwords/english.txt")),
			),
			`^(.{0,2}|.{15,})$`, // Remove any token t for which 2<len(t)<=15.
		),
		CompositeLabeler(
			TitleLabeler("filename"),
			DirLabeler("newsgroup"),
		),
		EmptyFilterer(),
	}
	p.Tokenizer = FrequencyTokenizer(p, 50, 2000)
	return getCorpus(p, "newsgroups.gob")
}

// DownloadAmazon gets a Corpus consisting of roughly 40,000 Amazon product
// reviews with associated star ratings. Each Document is labeled with an id
// and stars.  The data and Corpus are saved to DataDir for reuse.
func DownloadAmazon() Corpus {
	p := Pipeline{
		DownloadInputer("amazon/amazon.txt"),
		LineExtractor("\t"),
		StopwordTokenizer(
			DefaultTokenizer(),
			ReadWordlist(OpenDownload("stopwords/english.txt")),
		),
		CompositeLabeler(
			TitleLabeler("id"),
			ReadFloatLabeler(
				"stars",
				OpenDownload("amazon/amazon.stars"),
				"\t",
			),
		),
		EmptyFilterer(),
	}
	p.Tokenizer = FrequencyTokenizer(p, 30, -1)
	return getCorpus(p, "amazon.gob")
}

// DownloadStateUnion gets a Corpus contain state of the union address up till
// 2006. The data and Corpus are saved to DataDir for reuse.
func DownloadStateUnion() Corpus {
	p := Pipeline{
		DownloadInputer("stateunion/stateunion.tar.gz"),
		TarGzipExtractor(WholeExtractor()),
		StopwordTokenizer(
			DefaultTokenizer(),
			ReadWordlist(OpenDownload("stopwords/english.txt")),
		),
		NoopLabeler(),
		KeepFilterer(),
	}
	p.Tokenizer = FrequencyTokenizer(p, 2, -1)
	return getCorpus(p, "stateunion.gob")
}
