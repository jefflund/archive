package gorl

import (
	"math"

	"github.com/nsf/termbox-go"
)

// Color represents the color of a Glyph
type Color uint16

// Color constants for use with ColorChar.
const (
	ColorRed          = Color(termbox.ColorRed)
	ColorBlue         = Color(termbox.ColorBlue)
	ColorCyan         = Color(termbox.ColorCyan)
	ColorBlack        = Color(termbox.ColorBlack)
	ColorGreen        = Color(termbox.ColorGreen)
	ColorWhite        = Color(termbox.ColorWhite)
	ColorYellow       = Color(termbox.ColorYellow)
	ColorMagenta      = Color(termbox.ColorMagenta)
	ColorLightRed     = Color(termbox.ColorRed | termbox.AttrBold)
	ColorLightBlue    = Color(termbox.ColorBlue | termbox.AttrBold)
	ColorLightCyan    = Color(termbox.ColorCyan | termbox.AttrBold)
	ColorLightBlack   = Color(termbox.ColorBlack | termbox.AttrBold)
	ColorLightGreen   = Color(termbox.ColorGreen | termbox.AttrBold)
	ColorLightWhite   = Color(termbox.ColorWhite | termbox.AttrBold)
	ColorLightYellow  = Color(termbox.ColorYellow | termbox.AttrBold)
	ColorLightMagenta = Color(termbox.ColorMagenta | termbox.AttrBold)
)

// Glyph pairs a rune with a color.
type Glyph interface {
	Ch() rune
	Fg() Color
}

// char is a white Glyph
type char rune

// Char creates a white Glyph with the given rune.
func Char(ch rune) Glyph {
	return char(ch)
}

// Ch gets the rune value of the Char.
func (g char) Ch() rune {
	return rune(g)
}

// Fg returns ColorWhite.
func (g char) Fg() Color {
	return ColorWhite
}

// glyph is the basic implementation of Glyph
type glyph struct {
	ch rune
	fg Color
}

// ColoredChar creates a Glyph with the given rune and Color.
func ColoredChar(ch rune, fg Color) Glyph {
	return glyph{ch, fg}
}

// Ch gets the rune value of the ColoredChar.
func (g glyph) Ch() rune {
	return g.ch
}

// Fg gets the Color value of the ColoredChar.
func (g glyph) Fg() Color {
	return g.fg
}

// Key represents a single keypress.
type Key rune

// Key constants which normally require escapes.
const (
	KeyEsc   Key = Key(termbox.KeyEsc)
	KeyEnter Key = Key(termbox.KeyEnter)
	KeyCtrlC Key = Key(termbox.KeyCtrlC)
	KeyPgup  Key = Key(termbox.KeyPgup)
	KeyPgdn  Key = Key(termbox.KeyPgdn)
	KeyRight Key = Key(termbox.KeyArrowRight)
	KeyUp    Key = Key(termbox.KeyArrowUp)
	KeyDown  Key = Key(termbox.KeyArrowDown)
	KeyLeft  Key = Key(termbox.KeyArrowLeft)
)

// KeyMap stores default directional Key values. This dictionary can be edited
// to affect any core functions which require knowledge of directional keys.
var KeyMap = map[Key]Offset{
	'h': {-1, 0}, '4': {-1, 0},
	'l': {1, 0}, '6': {1, 0},
	'k': {0, -1}, '8': {0, -1},
	'j': {0, 1}, '2': {0, 1},
	'u': {1, -1}, '9': {1, -1},
	'y': {-1, -1}, '7': {-1, -1},
	'n': {1, 1}, '3': {1, 1},
	'b': {-1, 1}, '1': {-1, 1},
	'.': {0, 0}, '5': {0, 0},
}

// Offset stores a 2-dimensional int vector.
type Offset struct {
	X, Y int
}

// Sub returns the result of subtracting another Offset from this one.
func (o Offset) Sub(a Offset) Offset {
	return Offset{o.X - a.X, o.Y - a.Y}
}

// Add returns the result of adding another Offset to this one.
func (o Offset) Add(a Offset) Offset {
	return Offset{o.X + a.X, o.Y + a.Y}
}

// Neg returns the result of negating the Offset.
func (o Offset) Neg() Offset {
	return Offset{-o.X, -o.Y}
}

// Scale returns the result of multiplying the Offset by an integer scalar.
func (o Offset) Scale(s int) Offset {
	return Offset{o.X * s, o.Y * s}
}

// Manhattan returns the L_1 distance off the Offset.
func (o Offset) Manhattan() int {
	return Abs(o.X) + Abs(o.Y)
}

// Euclidean returns the L_2 distance off the Offset.
func (o Offset) Euclidean() float64 {
	return math.Hypot(float64(o.X), float64(o.Y))
}

// Chebyshev returns the L_inf distance off the Offset.
func (o Offset) Chebyshev() int {
	return Max(Abs(o.X), Abs(o.Y))
}
