package pipeline

import (
	"bufio"
	"io/ioutil"
	"strings"
)

// ExtractorFunc is an adaptor to allow a function with the appropriate
// signature to serve as an Extractor.
type ExtractorFunc func(NameReader) chan Text

// Extract implements Extractor by calling f(r).
func (f ExtractorFunc) Extract(r NameReader) chan Text { return f(r) }

// WholeExtractor is an Extractor which treats the name and entire contents of
// a NameReader as a single Text.
func WholeExtractor() Extractor {
	return ExtractorFunc(func(r NameReader) chan Text {
		texts := make(chan Text)
		go func() {
			data, err := ioutil.ReadAll(r)
			if err != nil {
				panic(err)
			}
			texts <- Text{r.Name(), string(data)}
			close(texts)
		}()
		return texts
	})
}

// SkipExtractor is an Extractor which treats the name and entire contents of a
// NameReader after skipping a header as a single Text.
func SkipExtractor(delim string) Extractor {
	return ExtractorFunc(func(r NameReader) chan Text {
		texts := make(chan Text)
		go func() {
			buf, err := ioutil.ReadAll(r)
			if err != nil {
				panic(err)
			}
			data := string(buf)
			index := strings.Index(data, delim)
			if index == -1 {
				panic("SkipExtractor missing delim")
			}
			texts <- Text{r.Name(), data[index+len(delim):]}
			close(texts)
		}()
		return texts
	})
}

// LineExtractor is an Extractor which treats each line of a NameReader as a
// Text, with everything before the delimiter as the Text name, and everything
// after as the Text data.
func LineExtractor(delim string) Extractor {
	return ExtractorFunc(func(r NameReader) chan Text {
		texts := make(chan Text)
		go func() {
			scanner := bufio.NewScanner(r)
			for scanner.Scan() {
				line := scanner.Text()
				index := strings.Index(line, delim)
				if index == -1 {
					panic("LineExtractor missing delim")
				}
				texts <- Text{line[:index], line[index+len(delim):]}
			}
			if err := scanner.Err(); err != nil {
				panic(err)
			}
			close(texts)
		}()
		return texts
	})
}
