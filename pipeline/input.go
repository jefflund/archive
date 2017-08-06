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
