// Package gorl is a (somewhat) generic roguelike toolkit for Go. It is mostly
// intended for development of Sticks and Stones, but should be generally
// useful for various roguelikes. Some of the functionality includes:
// 	* Robust entity/component/event architecture
// 	* Behavior trees construction
// 	* DeltaClock scheduling
// 	* Heightmap generation
// 	* Log formatting language
// 	* High quality pseudo-random number generation
// 	* Basic Terminal IO
// 	* Various UI components
// 	* Fast field of view and line of sight
package gorl

// Event is a message sent to an Entity.
type Event interface{}

// Component processes Events for an Entity.
type Component interface {
	Process(Event)
}

// Entity is a single game object represented as a collection of Component.
type Entity interface {
	Handle(Event)
}

// ComponentFunc is function which implements Component. It is usually used to
// return an anonymous event handler as a Component.
type ComponentFunc func(Event)

// Process implements Component for ComponentFunc
func (c ComponentFunc) Process(v Event) {
	c(v)
}

// ComponentSlice is an Entity which is a slice of Components.
type ComponentSlice []Component

// Handle implements Entity for ComponentSlice by sending an Event to each
// Component for processing in order.
func (e ComponentSlice) Handle(v Event) {
	for _, c := range e {
		c.Process(v)
	}
}

// AddComponent appends a Component to the ComponentSlice.
func (e *ComponentSlice) AddComponent(c Component) {
	*e = append(*e, c)
}

// Tile is an Entity representing a single square in a map.
type Tile struct {
	Face     Glyph
	Pass     bool
	Lite     bool
	Offset   Offset
	Adjacent map[Offset]*Tile
	Occupant Entity

	ComponentSlice
}

// NewTile creates a new Tile with no neighbors or occupant.
func NewTile(o Offset) *Tile {
	return &Tile{Char('.'), true, true, o, make(map[Offset]*Tile), nil, nil}
}

// Handle implements Entity for Tile
func (e *Tile) Handle(v Event) {
	switch v := v.(type) {
	case *RenderEvent:
		v.Render = e.Face
		if e.Occupant != nil {
			e.Occupant.Handle(v)
		}
	case *MoveEvent:
		if bumped := v.Dest.Occupant; bumped != nil {
			e.Occupant.Handle(&BumpEvent{bumped})
		} else if v.Dest.Pass {
			e.Occupant.Handle(&PosEvent{v.Dest})
			v.Dest.Handle(&OccupyEvent{e.Occupant})
			e.Occupant = nil
		} else {
			e.Occupant.Handle(&CollideEvent{v.Dest})
		}
	case *OccupyEvent:
		e.Occupant = v.Occupant
	}

	e.ComponentSlice.Handle(v)
}

// PlaceOccupant is a helper to place an Entity occupant on the Tile. It works
// by sending an OccupyEvent to the Tile, and both a PosEvent and a
// ScheduleEvent to the occupant. It panics if the Tile already had an
// occupant.
func (e *Tile) PlaceOccupant(occupant Entity) {
	if e.Occupant != nil {
		panic("tile occupancy error")
	}

	e.Handle(&OccupyEvent{occupant})
	occupant.Handle(&PosEvent{e})
	occupant.Handle(&ScheduleEvent{})
}

// MoveOccupant is a helper to move an Entity occupant to a new Tile. It works
// by sending a MoveEvent to the Tile with the destination given by the delta
// Offset. It panics if the Tile has no occupant, or if the delta does not map
// to an adjacent Tile.
func (e *Tile) MoveOccupant(delta Offset) {
	if e.Occupant == nil {
		panic("tile occupancy error")
	}

	dst, ok := e.Adjacent[delta]
	if !ok {
		panic("tile adjacency error")
	}

	e.Handle(&MoveEvent{dst})
}

// RemoveOccupant is a helper to remove an Entity occupant from a Tile. It
// works by sending an UnscheduleEvent and a nil PosEvent to the occupant, and
// a nil OccupyEvent to the Tile. It panics if the Tile has no occupant.
func (e *Tile) RemoveOccupant() {
	if e.Occupant == nil {
		panic("tile occupancy error")
	}

	e.Occupant.Handle(&UnscheduleEvent{})
	e.Occupant.Handle(&PosEvent{nil})
	e.Handle(&OccupyEvent{nil})
}

// ActEvent is an Event triggering an Entity action.
type ActEvent struct{}

// ScheduleEvent is an Event triggering an Entity scheduling.
type ScheduleEvent struct {
	Delta float64
}

// UnscheduleEvent is an Event triggering an Entity unscheduling.
type UnscheduleEvent struct{}

// LogEvent is an Event requesting that a message be logged.
type LogEvent struct {
	Msg string
}

// RenderEvent is an Event querying an Entity for a Glyph to render.
// RenderEvent also includes a bool priority flag used to indicate whether or
// not the current render should be given priority. However, note that Tile
// neither reads nor sets this flag.
type RenderEvent struct {
	Render   Glyph
	Priority bool
}

// MoveEvent is an Event triggering an Entity occupant move to a new Tile.
type MoveEvent struct {
	Dest *Tile
}

// PosEvent is an Event informing an Entity of its new Tile position.
type PosEvent struct {
	Pos *Tile
}

// FoVEvent is an Event requesting the field of view for an Entity.
type FoVEvent struct {
	Origin *Tile
	FoV    map[Offset]*Tile
}

// BumpEvent is an Event in which one Entity bumps another.
type BumpEvent struct {
	Bumped Entity
}

// OccupyEvent is an Event informing about a new Entity occupant.
type OccupyEvent struct {
	Occupant Entity
}

// CollideEvent is an Event in which an Event collides with a Tile obstacle.
type CollideEvent struct {
	Obstacle *Tile
}

// NameEvent is an Event querying an Entity for its name.
type NameEvent struct {
	Name string
}

// UIEvent is an Event triggering a UI update.
type UIEvent struct{}
