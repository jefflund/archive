package pipeline

import (
	"testing"
)

func TestKeepFilterer(t *testing.T) {
	if !KeepFilterer().Filter(Document{}) {
		t.Error("KeepFilterer filtered")
	}
}
