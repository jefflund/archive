package pipeline

// FiltererFunc is an adaptor to allow a function with the appropriate
// signature to serve as a Filterer.
type FiltererFunc func(Document) bool

// Filter implements Filterer by calling f(d).
func (f FiltererFunc) Filter(d Document) bool { return f(d) }

// KeepFilterer is a Filterer which returns true for every Document.
func KeepFilterer() Filterer {
	return FiltererFunc(func(Document) bool { return true })
}

// LengthFilterer is a Filterer which removes any Document whose length (as
// measured by number of tokens) is strictly below a min or strictly above a
// max threshold. Negative values for those thresholds indicates that the bound
// is ignored.
func LengthFilterer(min, max int) Filterer {
	// Adjust boundaries if any are turned off using negative bounds.
	if min < 0 && max < 0 {
		return KeepFilterer() // No filtering done for this case.
	} else if max < 0 {
		max = MaxInt // No max length since no Document is this long.
	} else if min < 0 {
		min = MinInt // No min length since no Document is this short.
	}
	return FiltererFunc(func(d Document) bool {
		return len(d.Tokens) >= min && len(d.Tokens) <= max
	})
}

// EmptyFilterer removes any Document with no tokens (i.e. a LengthFilterer
// with a min threshold of 1).
func EmptyFilterer() Filterer { return LengthFilterer(1, -1) }
