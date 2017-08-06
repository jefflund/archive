package pipeline

import (
	"bufio"
	"io"
	"regexp"
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

// RegexpReplaceTokenizer is a Tokenizer which transforms the output of a base
// Tokenizer by replacing all tokens which match a regular expression. Note
// that the entire token is replaced if any part of it matches the regular
// expression pattern, so it may be desirable to use ^ and $ anchors to match
// the entire token.
func RegexpReplaceTokenizer(base Tokenizer, pattern string, replace string) Tokenizer {
	r := regexp.MustCompile(pattern)
	return TokenizerFunc(func(d string) []TokenLoc {
		tokens := base.Tokenize(d)
		for i, tl := range tokens {
			if r.MatchString(tl.Token) {
				tokens[i].Token = replace
			}
		}
		return tokens
	})
}

// RegexpRemoveTokenizer is a Tokenzer which transforms the output of a base
// Tokenizer by removing all tokens which match a regular expression. Note that
// the entire token is replace if any part of it matches the regular expression
// pattern, so it may be desireable to use ^ and $ anchors to match the entire
// pattern.
func RegexpRemoveTokenizer(base Tokenizer, pattern string) Tokenizer {
	r := regexp.MustCompile(pattern)
	return TokenizerFunc(func(d string) []TokenLoc {
		tokens := base.Tokenize(d)
		filtered := tokens[:0] // Reuse backing array of tokens.
		for _, tl := range tokens {
			if !r.MatchString(tl.Token) {
				filtered = append(filtered, tl)
			}
		}
		return filtered
	})
}

// ReadWordlist is a helper function for reading a list of words from one more
// more Reader. Each line should contain a single string to be appended to the
// list. ReadWordlist is primarily intended to be used in conjunction with
// StopwordTokenizer and CombineTokenizer.
func ReadWordlist(rs ...io.Reader) []string {
	var list []string
	for _, r := range rs {
		scanner := bufio.NewScanner(r)
		for scanner.Scan() {
			list = append(list, scanner.Text())
		}
		if err := scanner.Err(); err != nil {
			panic(err)
		}
	}
	return list
}

func containCheck(list []string) func(string) bool {
	set := make(map[string]struct{})
	for _, s := range list {
		set[s] = struct{}{}
	}
	return func(s string) bool {
		_, ok := set[s]
		return ok
	}
}

// StopwordTokenizer is a Tokenizer which transforms the output of a base
// Tokenizer by removing tokens which appear in a stopword list. The stopword
// list is usually read from a File using ReadWordlist.
func StopwordTokenizer(base Tokenizer, stopwords []string) Tokenizer {
	isStopword := containCheck(stopwords)
	return TokenizerFunc(func(d string) []TokenLoc {
		tokens := base.Tokenize(d)
		filtered := tokens[:0] // Reuse backing array of tokens.
		for _, tl := range tokens {
			if !isStopword(tl.Token) {
				filtered = append(filtered, tl)
			}
		}
		return filtered
	})
}

// CombineTokenizer is a Tokenizer which transforms the output of a base
// Tokenizer by replacing all tokens which appear in a combine list. The
// combine list is usually read from a File using ReadWordlist.
func CombineTokenizer(base Tokenizer, combine []string, replace string) Tokenizer {
	isCombine := containCheck(combine)
	return TokenizerFunc(func(d string) []TokenLoc {
		tokens := base.Tokenize(d)
		for i, tl := range tokens {
			if isCombine(tl.Token) {
				tokens[i].Token = replace
			}
		}
		return tokens
	})
}
