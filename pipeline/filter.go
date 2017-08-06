package pipeline

// FiltererFunc is an adaptor to allow a function with the appropriate
// signature to serve as a Filterer.
type FiltererFunc func(Document) bool

// Filter implements Filterer by calling f(d).
func (f FiltererFunc) Filter(d Document) bool { return f(d) }

func KeepFilterer() Filterer {
	return FiltererFunc(func(Document) bool { return true })
}
