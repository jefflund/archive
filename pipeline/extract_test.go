package pipeline

import (
	"archive/tar"
	"bytes"
	"compress/gzip"
	"reflect"
	"strings"
	"testing"
)

func TestExtractWhole(t *testing.T) {
	name, data := "name", "lorem ipsum\ndolar set."
	r := WrapNameReader(name, strings.NewReader(data))
	expected := []Text{{name, data}}
	var actual []Text
	for text := range WholeExtractor().Extract(r) {
		actual = append(actual, text)
	}
	if !reflect.DeepEqual(actual, expected) {
		t.Error("WholeExtractor incorrect Text")
	}
}

func TestSkipExtractor(t *testing.T) {
	cases := []struct {
		name   string
		delim  string
		input  string
		output string
	}{
		{
			"name", "\n\n",
			"header\nheader\n\nlorem ipsum\ndolar set.",
			"lorem ipsum\ndolar set.",
		},
		{
			"name", "---",
			"header\nheader---", "",
		},
	}
	for _, c := range cases {
		r := WrapNameReader(c.name, strings.NewReader(c.input))
		expected := []Text{{c.name, c.output}}
		var actual []Text
		for text := range SkipExtractor(c.delim).Extract(r) {
			actual = append(actual, text)
		}
		if !reflect.DeepEqual(actual, expected) {
			t.Error("SkipExtractor incorrect Text")
		}
	}
}

func TestLineExtractor(t *testing.T) {
	cases := []struct {
		name     string
		delim    string
		input    string
		expected []Text
	}{
		{
			"lorem", " ",
			"lorem ipsum dolar set\nasdf qwer zxcv",
			[]Text{
				{"lorem", "ipsum dolar set"},
				{"asdf", "qwer zxcv"},
			},
		}, {
			"lorem", "\t",
			"lorem\tipsum dolar set\nasdf\t qwer zxcv\n",
			[]Text{
				{"lorem", "ipsum dolar set"},
				{"asdf", " qwer zxcv"},
			},
		},
	}
	for _, c := range cases {
		r := WrapNameReader(c.name, strings.NewReader(c.input))
		var actual []Text
		for text := range LineExtractor(c.delim).Extract(r) {
			actual = append(actual, text)
		}
		if !reflect.DeepEqual(actual, c.expected) {
			t.Error("LineExtractor incorrect Text")
		}
	}
}

func TestHTMLExtractor(t *testing.T) {
	name := "page.html"
	input := `<!doctype html>
<html>
	<head>
		<title>Test Page</title>
		<meta charset="utf-8" />
	</head>
	<body>
		<h1>Test Page</h1>
		<p>Lorem ipsum dolar set</p>
	</body>
</html>
`
	output := `Test Page
Test Page
Lorem ipsum dolar set`

	r := WrapNameReader(name, strings.NewReader(input))
	expected := []Text{{name, output}}
	var actual []Text
	for text := range HTMLExtractor().Extract(r) {
		actual = append(actual, text)
	}
	if !reflect.DeepEqual(actual, expected) {
		t.Error("HTMLExtractor incorrect text", actual, expected)
	}
}

func TestTarGzipExtractor(t *testing.T) {
	// Create some data to stick in our tar.gz file.
	expected := []Text{
		{"lorem.txt", "lorem ipsum dolar set."},
		{"asdf.txt", "asdf asdf asdf"},
		{"foobar.txt", "foobar foobaz spam eggs"},
	}

	// Write the tar archive. Nothing should fail here, as this is just stdlib.
	tbuf := new(bytes.Buffer)
	tw := tar.NewWriter(tbuf)
	for _, text := range expected {
		hdr := tar.Header{
			Name: text.Name,
			Mode: 0755,
			Size: int64(len(text.Data)),
		}
		if err := tw.WriteHeader(&hdr); err != nil {
			panic(err)
		}
		if _, err := tw.Write([]byte(text.Data)); err != nil {
			panic(err)
		}
	}
	if err := tw.Close(); err != nil {
		panic(err)
	}

	// Write the gzip file. Nothing should fail here, as this is just stdlib.
	zbuf := new(bytes.Buffer)
	zw := gzip.NewWriter(zbuf)
	if _, err := zw.Write(tbuf.Bytes()); err != nil {
		panic(err)
	}
	if err := zw.Close(); err != nil {
		panic(err)
	}

	// Finally, we can test the TarGzipExtractor.
	nr := WrapNameReader("asdf.tar.gz", zbuf)
	var actual []Text
	for text := range TarGzipExtractor(WholeExtractor()).Extract(nr) {
		actual = append(actual, text)
	}
	if !reflect.DeepEqual(actual, expected) {
		t.Error("TarGzipExtractor incorrect texts")
	}
}
