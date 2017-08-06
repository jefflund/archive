package corpus

import (
	"io"
	"net/http"
	"os"
	"path/filepath"

	"github.com/jefflund/modelt/pipeline"
)

// DataDir is the directory in which corpus data will be saved. Override this
// before calling any corpus functions to customize this location.
var DataDir = filepath.Join(os.Getenv("HOME"), ".modelt")

func getPath(name string) string {
	return filepath.Join(DataDir, name)
}

// BaseURL is the base URL from which data should be downloaded. Override this
// before calling any corpus functions to customize this location.
var BaseURL = "https://github.com/jefflund/data/raw/data2"

func getURL(name string) string {
	return filepath.Join(BaseURL, name)
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
func OpenDownload(name string) pipeline.NameReader {
	url := getURL(name)
	path := getPath(name)
	ensureDownload(url, path)

	file, err := os.Open(path)
	if err != nil {
		panic(err)
	}
	return file
}

// DownloadInputer is an Inputer which gets NameReader for each of the given
// names, downloading the data from BaseURL to DataDir if needed. Using the
// default BaseURL the available names are:
//	* amazon/amazon.stars
//	* amazon/amazon.txt
//	* bible/bible.txt
//	* bible/xref.txt
//	* newsgroups/newsgroups.tar.gz
//	* stopwords/english.txt
//	* stopwords/jacobean.txt
func DownloadInputer(names ...string) pipeline.Inputer {
	return pipeline.InputerFunc(func() chan pipeline.NameReader {
		input := make(chan pipeline.NameReader)
		go func() {
			for _, name := range names {
				input <- OpenDownload(name)
			}
			close(input)
		}()
		return input
	})
}
