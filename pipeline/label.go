package pipeline

import (
	"path/filepath"
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
