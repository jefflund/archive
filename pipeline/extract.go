package pipeline

import (
	"archive/tar"
	"bufio"
	"compress/gzip"
	"io"
	"io/ioutil"
	"regexp"
	"strings"

	"golang.org/x/net/html"
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

// HTMLExtractor is an Extractor which extracts a single Text from the HTML
// contents of the NameReader.
func HTMLExtractor() Extractor {
	newline := regexp.MustCompile(`\n\n+`)
	return ExtractorFunc(func(r NameReader) chan Text {
		texts := make(chan Text)
		go func() {
			var buf []string
			z := html.NewTokenizer(r)

			for {
				tt := z.Next()
				if tt == html.ErrorToken {
					if err := z.Err(); err != io.EOF {
						panic(err)
					}
					break
				}
				if tt == html.TextToken {
					buf = append(buf, strings.TrimSpace(string(z.Text())))
				}
			}

			data := strings.Join(buf, "\n")
			data = newline.ReplaceAllLiteralString(data, "\n")
			data = strings.TrimSpace(data)
			texts <- Text{r.Name(), data}
			close(texts)
		}()
		return texts
	})
}

type wrappedReader struct {
	name string
	io.Reader
}

func (r *wrappedReader) Name() string { return r.name }

// WrapNameReader wraps a Reader to create a NameReader with the given name.
func WrapNameReader(n string, r io.Reader) NameReader {
	return &wrappedReader{n, r}
}

// TarExtractor is an Extractor which wraps another Extractor to read Text from
// a tar archive. Each file in the archive is given to the wrapped Extractor,
// and the results are aggregated.
func TarExtractor(e Extractor) Extractor {
	return ExtractorFunc(func(r NameReader) chan Text {
		texts := make(chan Text)
		go func() {
			tr := tar.NewReader(r)
			for {
				hdr, err := tr.Next()
				if err == io.EOF {
					break
				}
				if err != nil {
					panic(err)
				}
				if hdr.Typeflag == tar.TypeReg || hdr.Typeflag == tar.TypeRegA {
					for text := range e.Extract(WrapNameReader(hdr.Name, tr)) {
						texts <- text
					}
				}
			}
			close(texts)
		}()
		return texts
	})
}

// GzipExtractor is an Extractor which wraps another Extractor to read Text
// from a gzipped file.
func GzipExtractor(e Extractor) Extractor {
	return ExtractorFunc(func(r NameReader) chan Text {
		zr, err := gzip.NewReader(r)
		if err != nil {
			panic(err)
		}
		return e.Extract(WrapNameReader(r.Name(), zr))
	})
}

// TarGzipExtractor uses TarExtractor and GzipExtractor to wrap another
// Extractor to read from a gzipped tar archive.
func TarGzipExtractor(e Extractor) Extractor {
	return GzipExtractor(TarExtractor(e))
}
