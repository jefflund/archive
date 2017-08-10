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

// Label stores a single labeling mapping a name (not the metadata attribute,
// but the name of the thing being labeled) to a metadata value.
type Label struct {
	Name  string
	Value interface{}
}

// ChanLabeler is a Labeler which is backed by a channel which produces labels.
// Assuming that the channel generates labels in the same order they are
// requested, ChanLabeler will use no extra memory. If this assumption is
// violated, ChanLabeler will cache generated labels as needed.
type ChanLabeler struct {
	attr   string
	labels chan Label
	cache  map[string]interface{}
}

// NewChanLabeler creates a new Labeler using the given channel and metadata
// attribute name.
func NewChanLabeler(attr string, c chan Label) *ChanLabeler {
	return &ChanLabeler{attr, c, make(map[string]interface{})}
}

// Label generates Label from the backing channel until the requested name and
// associated value is found.
func (c *ChanLabeler) Label(n string) map[string]interface{} {
	if value, ok := c.cache[n]; ok {
		delete(c.cache, n)
		return map[string]interface{}{c.attr: value}
	}

	for l := range c.labels {
		if l.Name == n {
			return map[string]interface{}{c.attr: l.Value}
		}

		c.cache[l.Name] = l.Value
	}

	panic("ChanLabeler missing label")
}

// ReadStringLabeler creates a new Labeler which generates labels by reading
// lines from a Reader. Each line should contain one name and value, separated
// by a delimiter. The the labels are requested in the order they are read, no
// extra memory is required by the Labeler. Otherwise, labels are cached as
// needed.
func ReadStringLabeler(attr string, r io.Reader, delim string) Labeler {
	c := make(chan Label)
	go func() {
		scanner := bufio.NewScanner(r)
		for scanner.Scan() {
			line := scanner.Text()
			index := strings.Index(line, delim)
			if index == -1 {
				panic("ReadStringLabeler missing delim")
			}
			c <- Label{line[:index], line[index+len(delim):]}
		}
		if err := scanner.Err(); err != nil {
			panic(err)
		}
		close(c)
	}()
	return NewChanLabeler(attr, c)
}

// ReadFloatLabeler creates a new Labeler which generates labels by reading
// lines from a Reader. Each line should contain one name and value, separated
// by a delimiter. The value should be a string representation of a flaot64.
// The the labels are requested in the order they are read, no extra memory is
// required by the Labeler. Otherwise, labels are cached as needed.
func ReadFloatLabeler(attr string, r io.Reader, delim string) Labeler {
	c := make(chan Label)
	go func() {
		scanner := bufio.NewScanner(r)
		for scanner.Scan() {
			line := scanner.Text()
			index := strings.Index(line, delim)
			if index == -1 {
				panic("ReadFloatLabeler missing delim")
			}
			value, err := strconv.ParseFloat(line[index+len(delim):], 64)
			if err != nil {
				panic(err)
			}
			c <- Label{line[:index], value}
		}
		if err := scanner.Err(); err != nil {
			panic(err)
		}
	}()
	return NewChanLabeler(attr, c)
}

// ReadSliceLabeler creates a new Labeler which generates labels by reading
// lines from a Reader. Each line should contain one name and value, separated
// by a delimiter. The value should be a slice of string, represented by the
// output of strings.Join(slice, sep). The the labels are requested in the
// order they are read, no extra memory is required by the Labeler. Otherwise,
// labels are cached as needed.
func ReadSliceLabeler(attr string, r io.Reader, delim, sep string) Labeler {
	c := make(chan Label)
	go func() {
		scanner := bufio.NewScanner(r)
		for scanner.Scan() {
			line := scanner.Text()
			index := strings.Index(line, delim)
			if index == -1 {
				panic("ReadStringLabeler missing delim")
			}
			var value []string
			if s := line[index+len(delim):]; len(s) > 0 {
				value = strings.Split(s, sep)
			}
			c <- Label{line[:index], value}
		}
		if err := scanner.Err(); err != nil {
			panic(err)
		}
		close(c)
	}()
	return NewChanLabeler(attr, c)
}
