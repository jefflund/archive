package pipeline

import (
	"io"
	"reflect"
	"strings"
	"testing"
)

type WrappedNameReader struct {
	name string
	io.Reader
}

func (r WrappedNameReader) Name() string {
	return r.name
}

func TestExtractWhole(t *testing.T) {
	name, data := "name", "lorem ipsum\ndolar set."
	r := WrappedNameReader{name, strings.NewReader(data)}
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
		r := WrappedNameReader{c.name, strings.NewReader(c.input)}
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
		r := WrappedNameReader{c.name, strings.NewReader(c.input)}
		var actual []Text
		for text := range LineExtractor(c.delim).Extract(r) {
			actual = append(actual, text)
		}
		if !reflect.DeepEqual(actual, c.expected) {
			t.Error("LineExtractor incorrect Text")
		}
	}
}
