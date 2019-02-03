package gorl

import (
	"testing"
)

func ExampleGenTerrain() {
	// Create various biomes to be used in our world.
	sea := func(t *Tile) {
		t.Pass = false
		t.Lite = true
		t.Face = ColoredChar('~', ColorBlue)
	}
	grassland := func(t *Tile) {
		grasses := []Glyph{
			ColoredChar('.', ColorGreen),
			ColoredChar('.', ColorLightGreen),
		}
		t.Pass = true
		t.Lite = true
		t.Face = grasses[RandIntn(len(grasses))]
	}
	forest := func(t *Tile) {
		trees := []Glyph{
			ColoredChar('%', ColorGreen),
			ColoredChar('%', ColorLightGreen),
		}
		if RandChance(.9) {
			grassland(t)
		} else {
			t.Pass = false
			t.Lite = false
			t.Face = trees[RandIntn(len(trees))]
		}
	}

	// Create a world, using height to apply various biomes.
	tiles := GenTerrain(100, 100, func(o Offset, h float64) *Tile {
		t := NewTile(o)
		switch {
		case h < .4:
			sea(t)
		case h < .8:
			grassland(t)
		case h <= 1:
			forest(t)
		}
		return t
	})

	// Add an impassible fense at the edge of the world.
	GenFence(tiles, func(t *Tile) {
		t.Pass = false
		t.Lite = false
		t.Face = Char('#')
	})
}

func ExampleGenCave() {
	// This is the basic pattern for Gen functions which use GenTileBool to
	// create individual tiles.
	GenCave(500, func(o Offset, pass bool) *Tile {
		t := NewTile(o)
		if !pass {
			t.Face = Char('#')
			t.Pass = false
			t.Lite = false
		}
		return t
	})
}

func TestGenCave(t *testing.T) {
	verifyConnectivity(t, GenCave(500, stubbedBoolGen))
}

func TestGenMaze(t *testing.T) {
	verifyConnectivity(t, GenMaze(500, stubbedBoolGen))
}

func TestGenTileGrid(t *testing.T) {
	tiles := GenTileGrid(20, 10, NewTile)
	GenFence(tiles, func(t *Tile) {
		t.Pass = false
	})
	verifyConnectivity(t, tiles)
}

func stubbedBoolGen(o Offset, pass bool) *Tile {
	t := NewTile(o)
	t.Pass = pass
	return t
}

func verifyConnectivity(t *testing.T, tiles []*Tile) {
	tileset := make(map[*Tile]struct{})
	for _, tile := range tiles {
		tileset[tile] = struct{}{}
	}

	for _, tile := range tiles {
		if !tile.Pass {
			continue
		}

		for _, d := range dirs8 {
			adj := tile.Adjacent[d]
			if adj == nil {
				t.Error("Missing adj")
			}
			if _, ok := tileset[adj]; !ok {
				t.Error("Invalid adj")
			}
			if !adj.Pass && adj.Adjacent[d.Neg()] != tile {
				t.Error("Bad wall")
			}
		}
	}
}
