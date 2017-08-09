package pipeline

import (
	"io"
)

// System independent min and max for int type.
const (
	MaxInt = int(^uint(0) >> 1)
	MinInt = ^MaxInt
)

// NameReader is a basic Reader with a string name. Note that File implements
// the NameReader interface. NameReader are typically generated using an
// Inputer.
type NameReader interface {
	Name() string
	io.Reader
}

// Inputer generates NameReader needed by Pipeline.
type Inputer interface {
	Input() chan NameReader
}

// Text pairs a string name with string data. Text is typically generated from
// a NameReader using an Extractor.
type Text struct {
	Name string
	Data string
}

// Extractor extracts Text from a NameReader.
type Extractor interface {
	Extract(NameReader) chan Text
}

// TokenLoc pairs a string token with an int location. TokenLoc is typically
// generated from Text data using a Tokenizer.
type TokenLoc struct {
	Token string
	Loc   int
}

// Tokenizer extracts TokenLoc from Text data.
type Tokenizer interface {
	Tokenize(string) []TokenLoc
}

// TypeLoc pairs a int token type with an int location. TypeLoc is typically
// generated by converting TokenLoc through a VocabBuilder.
type TypeLoc struct {
	Type int
	Loc  int
}

// VocabBuilder faciliates the conversion of string tokens to int token types
// by storing a bi-directional mapping between the two.
type VocabBuilder struct {
	tokens []string
	types  map[string]int
}

// NewVocabBuilder creates a new empty VocabBuilder.
func NewVocabBuilder() *VocabBuilder {
	return &VocabBuilder{nil, make(map[string]int)}
}

// Type gets the int id for a given string token.
func (v *VocabBuilder) Type(t string) int {
	id, ok := v.types[t]
	if !ok {
		id = len(v.tokens)
		v.types[t] = id
		v.tokens = append(v.tokens, t)
	}
	return id
}

// Convert turns a slice of TokenLoc into a slice of TypeLoc using Type.
func (v *VocabBuilder) Convert(tokens []TokenLoc) []TypeLoc {
	types := make([]TypeLoc, len(tokens))
	for i, t := range tokens {
		types[i] = TypeLoc{v.Type(t.Token), t.Loc}
	}
	return types
}

// Labeler produces the metadata labels as a dict mapping metadata attributes
// to metadata values for a document given by name.
type Labeler interface {
	Label(string) map[string]interface{}
}

// Filterer determines whether a Document should be included in a Corpus.
type Filterer interface {
	Filter(Document) bool
}

// Document represents a single text document in a Corpus. Document is
// typically generated by a Pipeline by converting Text to Document using the
// combined results of a Tokenizer, VocabBuilder and Labeler.
type Document struct {
	Text     string
	Tokens   []TypeLoc
	Metadata map[string]interface{}
}

// Corpus describes a collection of Document with an associated vocabulary.
type Corpus interface {
	Size() (M, V int)
	Documents() chan Document
	Vocabulary() []string
}

// Pipeline describes the process of constructing a new Corpus.
type Pipeline struct {
	Inputer
	Extractor
	Tokenizer
	Labeler
	Filterer
}

// SliceCorpus is a collection a Corpus backed by a slice of Document.
type SliceCorpus struct {
	Docs  []Document
	Vocab []string
}

// RunSlice constructs a SliceCorpus backed by a slice of Document.
func NewSliceCorpus(p Pipeline) Corpus {
	var documents []Document
	vocab := NewVocabBuilder()
	for reader := range p.Input() {
		for text := range p.Extract(reader) {
			tokens := p.Tokenize(text.Data)
			types := vocab.Convert(tokens)
			metadata := p.Label(text.Name)
			document := Document{text.Data, types, metadata}
			if p.Filter(document) {
				documents = append(documents, document)
			}
		}
	}
	return &SliceCorpus{documents, vocab.tokens}
}

// Size gets the number of documents and vocabulary size for the SliceCorpus.
func (s *SliceCorpus) Size() (M, V int) {
	return len(s.Docs), len(s.Vocab)
}

// Documents creates a channel and sequentially sends the Document in the
// backing slice to the channel.
func (s *SliceCorpus) Documents() chan Document {
	c := make(chan Document)
	go func() {
		for _, d := range s.Docs {
			c <- d
		}
		close(c)
	}()
	return c
}

// Vocabulary gets the vocabulary for the SliceCorpus.
func (s *SliceCorpus) Vocabulary() []string {
	return s.Vocab
}
