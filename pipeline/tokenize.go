package pipeline

import (
	"strings"
	"unicode"
)

// TokenizerFunc is an adapator to allow a function with the appropriate
// signature to serve as a Tokenizer.
type TokenizerFunc func(string) []TokenLoc

// Tokenize implements Tokenizer by calling f(d).
func (f TokenizerFunc) Tokenize(d string) []TokenLoc { return f(d) }

// FieldsTokenizer is a Tokenizer which splits the data around each instance of
// one or more consecutive white space characters.
func FieldsTokenizer() Tokenizer {
	return FieldsFuncTokenizer(unicode.IsSpace)
}

// FieldsFuncTokenizer is a Tokenizer which splits the data around one or more
// code points satisfying the given delimiter function.
func FieldsFuncTokenizer(f func(rune) bool) Tokenizer {
	return TokenizerFunc(func(d string) []TokenLoc {
		var tokens []TokenLoc
		begin := -1 // Set to -1 when looking for start of token.
		for i, r := range d {
			if f(r) {
				if begin >= 0 {
					tokens = append(tokens, TokenLoc{d[begin:i], begin})
					begin = -1
				}
			} else if begin == -1 {
				begin = i
			}
		}
		if begin >= 0 { // Last token might end at EOF.
			tokens = append(tokens, TokenLoc{d[begin:], begin})
		}
		return tokens
	})
}

// MapTokenizer is a Tokenizer which transforms the output of a base Tokenizer
// using strings.Map with the given mapping function. Any empty tokens after
// the transform are excluded.
func MapTokenizer(base Tokenizer, mapping func(rune) rune) Tokenizer {
	return TokenizerFunc(func(d string) []TokenLoc {
		tokens := base.Tokenize(d)
		mapped := make([]TokenLoc, 0, len(tokens))
		for _, tl := range tokens {
			token := strings.Map(mapping, tl.Token)
			if token != "" {
				mapped = append(mapped, TokenLoc{token, tl.Loc})
			}
		}
		return mapped
	})
}

// ToLowerTokenizer is a Tokenizer which transforms the output of a base
// Tokenizer by lower casing the letters of each token using unicode.ToLower.
func ToLowerTokenizer(base Tokenizer) Tokenizer {
	return MapTokenizer(base, unicode.ToLower)
}

// RemovePunctTokenizer is a Tokenizer which transforms the output of a base
// Tokenizer by removing punctation characters from each token, as defined by
// unicode.IsPunct. Any empty tokens after the transform are removed.
func RemovePunctTokenizer(base Tokenizer) Tokenizer {
	mapping := func(r rune) rune {
		if unicode.IsPunct(r) {
			return -1
		}
		return r
	}
	return MapTokenizer(base, mapping)
}

// DefaultTokenizer is a Tokenizer which splits the data around each instance
// of one or more white space characters (as defined by unicode.IsSpace), lower
// cases the output (using unicode.ToLower), and removes punctuation (as
// defined by unicode.IsPunct). Any empty tokens after the transform are
// removed.
func DefaultTokenizer() Tokenizer {
	return RemovePunctTokenizer(ToLowerTokenizer(FieldsTokenizer()))
}
