package pipeline

import (
	"reflect"
	"testing"
)

func TestDefaultTokenizer(t *testing.T) {
	// Since the DefaultTokenizer depends on RemovePunctTokenizer,
	// ToLowerTokenizer, and FieldsTokenizer, which in turn depend on
	// MapTokenizer and FieldsFuncTokenizer, this test is actually covers quite
	// a few different Tokenizer types despite its humble appearance!
	cases := []struct {
		input    string
		expected []TokenLoc
	}{
		{ // 012345678901234567890
			"Lorem Ipsum Dolar Set",
			[]TokenLoc{{"lorem", 0}, {"ipsum", 6}, {"dolar", 12}, {"set", 18}},
		},
		{ // 012345678901 2345678901
			"Lorem Ipsum\nDolar Set\n",
			[]TokenLoc{{"lorem", 0}, {"ipsum", 6}, {"dolar", 12}, {"set", 18}},
		},
		{ // 012345678901 234567890123
			"Lorem Ipsum\nAin't DON'T",
			[]TokenLoc{{"lorem", 0}, {"ipsum", 6}, {"aint", 12}, {"dont", 18}},
		},
		{ // 012345678901234567890123
			"Lorem Ipsum $100 <token>",
			[]TokenLoc{{"lorem", 0}, {"ipsum", 6}, {"$100", 12}, {"<token>", 17}},
		},
		{
			"\n",
			make([]TokenLoc, 0),
		},
		{
			"\n!@#!#",
			make([]TokenLoc, 0),
		},
	}
	for _, c := range cases {
		actual := DefaultTokenizer().Tokenize(c.input)
		if !reflect.DeepEqual(actual, c.expected) {
			t.Error("DefaultTokenzer incorrect tokens")
		}
	}
}

func TestRegexpReplaceTokenizer(t *testing.T) {
	cases := []struct {
		input    string
		pattern  string
		replace  string
		expected []TokenLoc
	}{
		{ // 0123456789012345678901234567890
			"Lorem Ipsum $100 token weird$69",
			`^\$\d+$`,
			"<money>",
			[]TokenLoc{{"Lorem", 0}, {"Ipsum", 6}, {"<money>", 12}, {"token", 17}, {"weird$69", 23}},
		},
		{ // 0123456789012345678901234567890
			"Lorem Ipsum $100 token weird$69",
			`\$\d+`,
			"<money>",
			[]TokenLoc{{"Lorem", 0}, {"Ipsum", 6}, {"<money>", 12}, {"token", 17}, {"<money>", 23}},
		},
	}
	for _, c := range cases {
		actual := RegexpReplaceTokenizer(FieldsTokenizer(), c.pattern, c.replace).Tokenize(c.input)
		if !reflect.DeepEqual(actual, c.expected) {
			t.Error("RegexpReplaceTokenizer incorrect tokens")
		}
	}
}

func TestRegexpRemoveTokenizer(t *testing.T) {
	cases := []struct {
		input    string
		pattern  string
		expected []TokenLoc
	}{
		{ // 0123456789012345678901234567890
			"Lorem Ipsum $100 token weird$69",
			`^\$\d+$`,
			[]TokenLoc{{"Lorem", 0}, {"Ipsum", 6}, {"token", 17}, {"weird$69", 23}},
		},
		{ // 0123456789012345678901234567890
			"Lorem Ipsum $100 token weird$69",
			`\$\d+`,
			[]TokenLoc{{"Lorem", 0}, {"Ipsum", 6}, {"token", 17}},
		},
	}
	for _, c := range cases {
		actual := RegexpRemoveTokenizer(FieldsTokenizer(), c.pattern).Tokenize(c.input)
		if !reflect.DeepEqual(actual, c.expected) {
			t.Error("RegexpRemoveTokenizer incorrect tokens")
		}
	}
}
