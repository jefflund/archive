package gorl

import (
	"github.com/nsf/termbox-go"
)

// TermInit initializes the termbox library. TermInit should be called before
// any other Term functions are used. After a successful call to TermInit, a
// call to TermDone should be deferred.
func TermInit() error {
	return termbox.Init()
}

// TermMustInit is like TermInit, except that any errors result in a panic.
func TermMustInit() {
	if err := TermInit(); err != nil {
		panic(err)
	}
}

// TermDone deinitializes the termbox library. Typically a call to TermDone is
// deferred immediately after a successful call to TermInit.
func TermDone() {
	termbox.Close()
}

// TermDraw places a Glyph into the internal termbox buffer at the given
// location. No changes are made on screen until TermRefresh is called.
func TermDraw(x, y int, g Glyph) {
	termbox.SetCell(x, y, g.Ch(), termbox.Attribute(g.Fg()), termbox.ColorBlack)
}

// TermClear erases everything in the internal termbox buffer. No changes are
// made on screen until TermRefresh is called.
func TermClear() {
	termbox.Clear(termbox.ColorWhite, termbox.ColorBlack)
}

// TermRefresh ensures that the screen reflects the internal buffer state.
func TermRefresh() {
	termbox.Flush()
}

// TermState captures the state of the internal termbox buffer so it can be
// restored later on.
type TermState interface {
	Restore()
}

type termstate [][]termbox.Cell

// TermSave captures the current state of the internal buffer so it can be
// restored later on.
func TermSave() TermState {
	cols, rows := termbox.Size()
	cells := termbox.CellBuffer()

	state := make(termstate, rows)
	for y := 0; y < rows; y++ {
		state[y] = make([]termbox.Cell, cols)
		for x := 0; x < cols; x++ {
			state[y][x] = cells[y*cols+x]
		}
	}

	return state
}

// Restore reverse the termbox buffer to the saved state.
func (s termstate) Restore() {
	for y, row := range s {
		for x, cell := range row {
			termbox.SetCell(x, y, cell.Ch, cell.Fg, cell.Bg)
		}
	}
}

// TermGetKey returns the next keypress. It blocks until there is one.
func TermGetKey() Key {
	for {
		event := termbox.PollEvent()
		if event.Type == termbox.EventKey {
			return Key(event.Ch) | Key(event.Key)
		}
	}
}
