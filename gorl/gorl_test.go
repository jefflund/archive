package gorl

import (
	"fmt"
)

func Example() {
	// This example gives an overview of how the various components of gorl fit
	// together. Most of the functionality here is stubbed, but the basic
	// skeleton is a good starting point for gorl based roguelikes.

	// Without this, cannot use gorl terminal functionality.
	TermMustInit()
	defer TermDone()

	// Generate the world map using GenHeightmap. See ExampleGenTerrain for a
	// more complete example of how heightmap generation works.
	world := GenTerrain(120, 40, func(o Offset, h float64) *Tile {
		t := NewTile(o)
		switch {
		case h <= .5:
			t.Face = ColoredChar('~', ColorBlue)
			t.Pass = false
			t.Lite = true
		case h <= 1:
			t.Face = ColoredChar('.', ColorGreen)
			t.Pass = true
			t.Lite = true
		}
		return t
	})
	GenFence(world, func(t *Tile) {
		t.Face = Char('#')
		t.Pass = false
		t.Lite = false
	})

	// Setup the UI for our demo.
	clock := NewDeltaClock()
	viewport := NewCameraWidget(0, 0, 21, 21)
	journal := NewLogWidget(21, 0, 80-21, 24)
	status := NewTextWidget(0, 21, 80, 1)
	screen := Screen{viewport, journal, status}

	// Create a simple Entity type for our creatures.
	type Creature struct {
		Pos   *Tile
		Alive bool
		ComponentSlice
	}
	// Initialize the hero
	hero := &Creature{Alive: true}
	// Add scheduler with 0 offset to the hero
	hero.AddComponent(clock.NewScheduler(hero, 0))
	// Add UI components to hero, taking care to add screen last so UIEvent
	// updates UI components and then redraws the screen.
	hero.AddComponent(viewport.NewCamera(hero, 11))
	hero.AddComponent(journal.NewLogger())
	hero.AddComponent(status.NewBinding(func() string {
		return fmt.Sprintf("Hero (%d, %d)", hero.Pos.Offset.X, hero.Pos.Offset.Y)
	}))
	hero.AddComponent(screen)
	// Add basic event handling to hero. While the logic here is purely
	// stubbed, these are the bare minimum Event types that a character Entity
	// should probably know how to respond to.
	hero.AddComponent(ComponentFunc(func(v Event) {
		switch v := v.(type) {
		case *RenderEvent:
			v.Render = Char('@')
			v.Priority = true // Gives the entity rendor priority over other components on the Tile.
		case *PosEvent:
			hero.Pos = v.Pos
		case *BumpEvent:
			hero.Handle(Log("%s <bump> %o", hero, v.Bumped))
		case *CollideEvent:
			hero.Handle(Log("%s <cannot pass> %o", hero, v.Obstacle))
		case *NameEvent:
			v.Name = "you" // Makes Log use second person with the hero.
		}
	}))
	// Add controller to hero
	hero.AddComponent(BehaviorTree(ActBehavior(func() BehaviorState {
		// Updates UI components (e.g. status) and redraws the screen.
		hero.Handle(&UIEvent{})

		switch key := TermGetKey(); key {
		case KeyEsc:
			hero.Alive = false
		default:
			if delta, ok := KeyMap[key]; ok {
				hero.Pos.MoveOccupant(delta)
			}
			// Without this ScheduleEvent, the hero will only move once since
			// it never got rescheduled.
			hero.Handle(&ScheduleEvent{1})
		}
		return SuccessState
	})))

	// Place the hero on the world. This helper notifies the tile of its new
	// occupant, notifies the hero of its new position, and schedules the hero
	// to act.
	RandOpenTile(world).PlaceOccupant(hero)

	// Main game loop
	for hero.Alive {
		clock.Tick()
	}
}
