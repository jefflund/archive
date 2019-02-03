package gorl

import (
	"fmt"
)

// Widget represents something which can draw itself on the screen.
type Widget interface {
	Update()
}

// Screen is a collection of Widget. Screen is also a Component which updates
// on UIEvent.
type Screen []Widget

// Update runs Update on each ScreenElement of the Screen.
func (s Screen) Update() {
	TermClear()
	for _, w := range s {
		w.Update()
	}
	TermRefresh()
}

// Process implements Component for Screen.
func (s Screen) Process(v Event) {
	switch v.(type) {
	case *UIEvent:
		s.Update()
	}
}

// Window serves as a base for various Widget which need relateive drawing.
type Window struct {
	X, Y, W, H int
}

// DrawRel performs a TermDraw relative to the location of the Widget.
func (w *Window) DrawRel(x, y int, g Glyph) {
	if 0 <= x && x <= w.W && 0 <= y && y < w.H {
		TermDraw(x+w.X, y+w.Y, g)
	}
}

// TextWidget is a Widget which displays dynamically bound text on screen.
type TextWidget struct {
	Window
	Text string
	Fg   Color
}

// NewTextWidget creates a new TextWidget with the given binding.
func NewTextWidget(x, y, w, h int) *TextWidget {
	return &TextWidget{Window{x, y, w, h}, "", ColorWhite}
}

// Update draws the bound text on screen.
func (w *TextWidget) Update() {
	x, y := 0, 0
	for _, ch := range w.Text {
		if ch == '\n' {
			x, y = 0, y+1
		} else {
			w.DrawRel(x, y, ColoredChar(ch, w.Fg))
			x++
		}
	}
}

// NewBinding creates a Component which updates the TextWidget text upon
// receiving a UIEvent. If both this Component and a Screen containing the
// TextWidget are added to an Entity, likely the TextWidget binding should
// revieve the UIEvent first, so the binding updates before the screen is
// redrawn. With ComponentSlice, this is done by adding the binding before
// adding the Screen.
func (w *TextWidget) NewBinding(fn func() string) Component {
	return ComponentFunc(func(v Event) {
		switch v.(type) {
		case *UIEvent:
			w.Text = fn()
		}
	})
}

// logmsg is a cached message in LogWidget.
type logmsg struct {
	Text  string
	Count int
	Seen  bool
}

// String implements fmt.Stringer for logmsg.
func (m *logmsg) String() string {
	if m.Count == 1 {
		return m.Text
	}
	return fmt.Sprintf("%s (x%d)", m.Text, m.Count)
}

// LogWidget is a Widget which stores and displays log messages.
type LogWidget struct {
	Window
	cache       []*logmsg
	SeenColor   Color
	UnseenColor Color
}

// NewLogWidget creates a new empty LogWidget.
func NewLogWidget(x, y, w, h int) *LogWidget {
	return &LogWidget{Window{x, y, w, h}, nil, ColorLightBlack, ColorWhite}
}

// Log places a message in the LogWidget cache.
func (w *LogWidget) Log(msg string) {
	if len(msg) == 0 {
		return
	}

	last := len(w.cache) - 1
	// If cache is empty, or last message text was different than this one.
	if last < 0 || w.cache[last].Text != msg {
		w.cache = append(w.cache, &logmsg{msg, 1, false})
		// Truncate cache if too long to show in the Widget Window.
		if len(w.cache) > w.H {
			w.cache = w.cache[len(w.cache)-w.H:]
		}
	} else {
		// Duplicate text, so just reuse last message.
		w.cache[last].Count++
		w.cache[last].Seen = false
	}
}

// Update draws the cached log messages on screen.
func (w *LogWidget) Update() {
	for y, msg := range w.cache {
		// Select color based on seen status.
		var fg Color
		if msg.Seen {
			fg = w.SeenColor
		} else {
			fg = w.UnseenColor
		}

		// Draw the message on screen. Assume no newlines, unlike TextWidget.
		for x, ch := range msg.String() {
			w.DrawRel(x, y, ColoredChar(ch, fg))
		}

		// Just displayed the message, so next time should be seen.
		msg.Seen = true
	}
}

// NewLogger creates a Component which responds to LogEvent by adding the log
// messages to the LogWidget.
func (w *LogWidget) NewLogger() Component {
	return ComponentFunc(func(v Event) {
		switch v := v.(type) {
		case *LogEvent:
			w.Log(v.Msg)
		}
	})
}

// NewConditionalLogger creates a Component which responds to LogEvent by
// adding the log message to the LogWidget whenever the condtion returns true.
func (w *LogWidget) NewConditionalLogger(condition func() bool) Component {
	return ComponentFunc(func(v Event) {
		switch v := v.(type) {
		case *LogEvent:
			if condition() {
				w.Log(v.Msg)
			}
		}
	})
}

// CameraWidget is a Widget which displays a field of view on screen.
type CameraWidget struct {
	Window
	FoV map[Offset]*Tile

	FoVFunc func(*Tile, int) map[Offset]*Tile
}

// NewCameraWidget creates a new CameraWidget with the given camera Entity,
// with FoVWallfix as the default FoVFunc.
func NewCameraWidget(x, y, w, h int) *CameraWidget {
	return &CameraWidget{Window{x, y, w, h}, nil, FoVWallfix}
}

// Update draws the camera field of view on screen.
func (w *CameraWidget) Update() {
	cx, cy := w.W/2, w.H/2
	for off, tile := range w.FoV {
		v := RenderEvent{}
		tile.Handle(&v)
		w.DrawRel(cx+off.X, cy+off.Y, v.Render)
	}
}

// NewCamera creates a Component which responds to PosEvent by sending a
// FoVEvent to the given camera Entity with the position as the field of view
// origin. This Component responds FovEvent by computing a field of view with
// the given radius.
func (w *CameraWidget) NewCamera(e Entity, radius int) Component {
	return ComponentFunc(func(v Event) {
		switch v := v.(type) {
		case *PosEvent:
			if v.Pos != nil {
				f := FoVEvent{Origin: v.Pos}
				e.Handle(&f)
				w.FoV = f.FoV
			}
		case *FoVEvent:
			v.FoV = w.FoVFunc(v.Origin, radius)
		}
	})
}
