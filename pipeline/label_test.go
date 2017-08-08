package pipeline

import (
	"fmt"
	"path/filepath"
	"reflect"
	"strings"
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

func TestReadStringLabeler(t *testing.T) {
	name := "lorem"
	value := "foobar"
	attr := "class"
	delim := ":"
	r := strings.NewReader("asdf:foo\nlorem:foobar\nipsum:baz")
	expected := map[string]interface{}{attr: value}
	actual := ReadStringLabeler(attr, r, delim).Label(name)
	if !reflect.DeepEqual(actual, expected) {
		t.Error("ReadStringLabeler giave incorrect labels")
	}
}

func TestReadFloatLabeler(t *testing.T) {
	name := "lorem"
	value := 3.14
	attr := "rating"
	delim := ":"
	r := strings.NewReader("asdf:69\nlorem:3.14\nipsum:5.43")
	expected := map[string]interface{}{attr: value}
	actual := ReadFloatLabeler(attr, r, delim).Label(name)
	if !reflect.DeepEqual(actual, expected) {
		t.Error("ReadFloatLabeler giave incorrect labels")
	}
}

func TestReadSliceLabeler(t *testing.T) {
	cases := []struct {
		name             string
		attr, delim, sep string
		value            []string
		input            string
	}{
		{
			"lorem", "xref", ":", ",",
			[]string{"asdf", "qwer"},
			"lorem:asdf,qwer\nipsum:foo,baz",
		},
		{
			"lorem", "xref", ":", ",",
			nil,
			"lorem:\nipsum:foo,baz",
		},
	}
	for _, c := range cases {
		r := strings.NewReader(c.input)
		expected := map[string]interface{}{c.attr: c.value}
		actual := ReadSliceLabeler(c.attr, r, c.delim, c.sep).Label(c.name)
		if !reflect.DeepEqual(actual, expected) {
			t.Error("ReadSliceLabeler gave incorrect labels")
		}
	}
}
