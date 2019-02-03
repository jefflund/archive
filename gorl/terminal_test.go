package gorl

import (
	"fmt"
)

var colors = []Color{
	ColorRed,
	ColorBlue,
	ColorGreen,
}

func Example_terminal() {
	// Called at the beginning of pretty much every gorl application to
	// initialize and tear down the terminal for use with gorl.
	TermMustInit()
	defer TermDone()

	// Basic terminal drawing using TermDraw.
	TermClear()
	for x, ch := range "Hello World!" {
		TermDraw(x, 0, ColoredChar(ch, colors[Mod(x, len(colors))]))
	}
	TermRefresh()

	// TermGetKey blocks until a key press event is received.
	TermGetKey()

	// The terminal state can be saved and restored.
	state := TermSave()
	for x, ch := range "This text should disappear" {
		TermDraw(x, 1, ColoredChar(ch, colors[Mod(x, len(colors))]))
	}
	TermRefresh()
	TermGetKey()

	// Restore and display the saved state.
	state.Restore()
	TermRefresh()

	// Key presses can be used. See the Key type for more info.
	key := TermGetKey()
	TermClear()
	for x, ch := range fmt.Sprintf("You pressed: %c", key) {
		TermDraw(x, 0, Char(ch))
	}
	TermRefresh()
	TermGetKey()
}
