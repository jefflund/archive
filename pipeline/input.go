package pipeline

import (
	"os"
	"path/filepath"
)

// InputerFunc is an adaptor to allow a function with the appropriate signature
// to serve as an Inputer.
type InputerFunc func() chan NameReader

// Input implements Inputer by calling f().
func (f InputerFunc) Input() chan NameReader { return f() }

// FileInputer is an Inputer which returns a set of files.
func FileInputer(filenames ...string) Inputer {
	return InputerFunc(func() chan NameReader {
		input := make(chan NameReader)
		go func() {
			for _, filename := range filenames {
				file, err := os.Open(filename)
				if err != nil {
					panic(err)
				}
				// No file.Close() as caller needs it open. GC will handle.
				input <- file
			}
			close(input)
		}()
		return input
	})
}

// GlobInputer is an Inputer which returns a set of files from a glob pattern.
func GlobInputer(pattern string) Inputer {
	matches, err := filepath.Glob(pattern)
	if err != nil {
		panic(err)
	}
	return FileInputer(matches...)
}

// DownloadInputer is an Inputer which gets NameReader for each of the given
// names, downloading the data from BaseURL to DataDir using OpenDownload.
// With the default BaseURL the available names are:
//	* amazon/amazon.stars
//	* amazon/amazon.txt
//	* bible/bible.txt
//	* bible/xref.txt
//	* newsgroups/newsgroups.tar.gz
//	* stateunion/stateunion.tar.gz
//	* stopwords/english.txt
//	* stopwords/jacobean.txt
func DownloadInputer(names ...string) Inputer {
	return InputerFunc(func() chan NameReader {
		input := make(chan NameReader)
		go func() {
			for _, name := range names {
				input <- OpenDownload(name)
			}
			close(input)
		}()
		return input
	})
}
