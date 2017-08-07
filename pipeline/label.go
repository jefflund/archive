package pipeline

import (
	"bufio"
	"io"
	"path/filepath"
	"strconv"
	"strings"
)

// LabelerFunc is an adaptor to allow a function with the approriate signature
// to serve as a Labeler.
type LabelerFunc func(string) map[string]interface{}

// Label implements Labeler by calling f(n).
func (f LabelerFunc) Label(n string) map[string]interface{} { return f(n) }

// NoopLabeler is a Labeler which returns no labels.
func NoopLabeler() Labeler {
	return LabelerFunc(func(string) map[string]interface{} { return nil })
}

// TitleLabeler is a Labeler which using the name to create a title.
func TitleLabeler(attr string) Labeler {
	return LabelerFunc(func(n string) map[string]interface{} {
		return map[string]interface{}{attr: n}
	})
}

// DirLabeler is a Labeler which creates a label by using filepath.Dir on the
// given name.
func DirLabeler(attr string) Labeler {
	return LabelerFunc(func(n string) map[string]interface{} {
		return map[string]interface{}{attr: filepath.Dir(n)}
	})
}

// CompositeLabeler is a Labeler which combines the results of other Labeler.
func CompositeLabeler(ls ...Labeler) Labeler {
	return LabelerFunc(func(n string) map[string]interface{} {
		labels := make(map[string]interface{})
		for _, l := range ls {
			for k, v := range l.Label(n) {
				labels[k] = v
			}
		}
		return labels
	})
}

// MapLabeler is a Labeler which associates the metadata attribute for each
// each name with a value using the given label mapping.
func MapLabeler(attr string, labels map[string]interface{}) Labeler {
	return LabelerFunc(func(n string) map[string]interface{} {
		value, ok := labels[n]
		if !ok {
			panic("MapLabeler missing label")
		}
		return map[string]interface{}{attr: value}
	})
}

// ReadStringLabels is a helper function for reading key/value pairs from a
// file for use with MapLabeler. Each line should contain a string name and
// string value separated by a delimiter.
func ReadStringLabels(r io.Reader, delim string) map[string]interface{} {
	mapping := make(map[string]interface{})
	scanner := bufio.NewScanner(r)
	for scanner.Scan() {
		line := scanner.Text()
		index := strings.Index(line, delim)
		if index == -1 {
			panic("ReadStringLabels missing delim")
		}
		mapping[line[:index]] = line[index+len(delim):]
	}
	return mapping
}

// ReadFloatLabels is a helper function for reading key/value pairs from a
// file for use with MapLabeler. Each line should contain a string name and
// float64 value separated by a delimiter.
func ReadFloatLabels(r io.Reader, delim string) map[string]interface{} {
	mapping := make(map[string]interface{})
	scanner := bufio.NewScanner(r)
	for scanner.Scan() {
		line := scanner.Text()
		index := strings.Index(line, delim)
		if index == -1 {
			panic("ReadStringLabels missing delim")
		}
		value, err := strconv.ParseFloat(line[index+len(delim):], 64)
		if err != nil {
			panic(err)
		}
		mapping[line[:index]] = value
	}
	return mapping
}
