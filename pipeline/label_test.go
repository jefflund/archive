package pipeline

import (
	"fmt"
	"path/filepath"
	"reflect"
	"testing"
)

func TestNoopLabeler(t *testing.T) {
	var expected map[string]interface{}
	actual := NoopLabeler().Label("asdf")
	if !reflect.DeepEqual(actual, expected) {
		t.Error("NoopLabeler returned labels")
	}
}

func TestTitleLabeler(t *testing.T) {
	input := "asdf"
	attr := "title"
	expected := map[string]interface{}{attr: input}
	actual := TitleLabeler(attr).Label(input)
	if !reflect.DeepEqual(actual, expected) {
		t.Error("TitleLabeler incorrect label")
	}
}

func TestDirLabeler(t *testing.T) {
	input := filepath.Join("a", "b", "c")
	attr := "dir"
	expected := map[string]interface{}{attr: filepath.Dir(input)}
	actual := DirLabeler(attr).Label(input)
	if !reflect.DeepEqual(actual, expected) {
		t.Error("DirLabeler incorrect label")
	}
}

func TestCompositeLabeler(t *testing.T) {
	called := make([]bool, 3)
	labelers := make([]Labeler, 3)
	for i := 0; i < 3; i++ {
		i := i // Cannot use iteration variable in closure!
		called[i] = false
		labelers[i] = LabelerFunc(func(n string) map[string]interface{} {
			called[i] = true
			return DirLabeler(fmt.Sprintf("dir-%d", i)).Label(n)
		})
	}

	input := filepath.Join("lorem", "ipsum")
	expected := map[string]interface{}{
		"dir-0": "lorem",
		"dir-1": "lorem",
		"dir-2": "lorem",
	}
	actual := CompositeLabeler(labelers...).Label(input)

	if !reflect.DeepEqual(actual, expected) {
		t.Error("CompositeLabeler incorrect labels")
	}

	for i := 0; i < 3; i++ {
		if !called[i] {
			t.Error("CompositeLabeler failed to call sub-Labeler")
			break
		}
	}
}
