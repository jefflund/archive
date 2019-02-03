package gorl

// PopupTextdump is a struct that displays a potentially long amount of text
// such as a help file.
type PopupTextdump struct {
	Title  string
	Lines  []string
	Height int

	DoneKeys       map[Key]struct{}
	ScrollDownKeys map[Key]struct{}
	ScrollUpKeys   map[Key]struct{}
	ScrollAmount   int

	TitleColor Color
	LineColor  Color
}

// Display displays the text dump until a done key is pressed (default:
// KeyEsc). The user can optionally scroll through the text. The state of the
// terminal is saved and restored by the PopupTextdump.
func (p PopupTextdump) Display() {
	state := TermSave()
	defer func() {
		state.Restore()
		TermRefresh()
	}()

	maxindex := Max(0, len(p.Lines)-p.Height+1)

	var index int
	var key Key

	for !p.isDone(key) {
		TermClear()
		for x, ch := range p.Title {
			TermDraw(x, 0, ColoredChar(ch, p.TitleColor))
		}
		for y, line := range p.Lines[index:Min(index+p.Height-1, len(p.Lines))] {
			for x, ch := range line {
				TermDraw(x, y+1, ColoredChar(ch, p.LineColor))
			}
		}
		TermRefresh()
		key = TermGetKey()
		index = Clamp(0, index+p.scrollDelta(key), maxindex)
	}
}

// isDone returns true if the given Key should terminate a text dump display.
// If no done keys are given, then KeyEsc is used as the default.
func (p PopupTextdump) isDone(k Key) bool {
	if len(p.DoneKeys) == 0 {
		return k == KeyEsc
	}

	_, ok := p.DoneKeys[k]
	return ok
}

// scrollDelta returns the amount a given Key should scroll a text dump
// display. If no ScrollAmount is given, the default ScrollAmount is 1.
// If no scroll keys are given, no scroll is possible.
func (p PopupTextdump) scrollDelta(k Key) int {
	if _, ok := p.ScrollDownKeys[k]; ok {
		return Max(1, p.ScrollAmount)
	}
	if _, ok := p.ScrollUpKeys[k]; ok {
		return -Max(1, p.ScrollAmount)
	}
	return 0
}

// KeySet is a helper function for creating a set of Key.
func KeySet(ks ...Key) map[Key]struct{} {
	set := make(map[Key]struct{})
	for _, k := range ks {
		set[k] = struct{}{}
	}
	return set
}
