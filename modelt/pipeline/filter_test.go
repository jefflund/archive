package pipeline

import (
	"testing"
)

func TestKeepFilterer(t *testing.T) {
	if !KeepFilterer().Filter(Document{}) {
		t.Error("KeepFilterer filtered")
	}
}

func TestEmptyFilterer(t *testing.T) {
	cases := []struct {
		doc  Document
		keep bool
	}{
		{Document{"a b c", []TypeLoc{{0, 0}, {1, 2}, {2, 4}}, nil}, true},
		{Document{"a", []TypeLoc{{0, 0}}, nil}, true},
		{Document{"filtered", []TypeLoc{}, nil}, false},
	}
	for _, c := range cases {
		actual := EmptyFilterer().Filter(c.doc)
		if actual != c.keep {
			t.Error("EmptyFilterer incorrect filter")
		}
	}
}
