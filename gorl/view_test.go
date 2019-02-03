package gorl

import (
	"testing"
)

func ExampleFoV() {
	// Normally generation is a bit more interesting, but here we just stub it.
	tiles := GenTileGrid(80, 80, NewTile)
	origin := RandOpenTile(tiles)
	radius := 5

	// Computing a field of view is easy!
	fov := FoV(origin, radius)

	// The resulting fov is a map keyed by Offset (relative to the origin) with
	// the corresponding Tile as the value, so we can just range over it.
	for offset, tile := range fov {
		TermDraw(offset.X+radius, offset.Y+radius, tile.Face)
	}
}

type StrGrid []string

func (g StrGrid) FoVCase() (*Tile, map[Offset]*Tile) {
	cols, rows := len(g[0]), len(g)
	var origin *Tile
	fov := make(map[Offset]*Tile)
	GenTileGrid(cols, rows, func(o Offset) *Tile {
		t := NewTile(o)
		ch := g[o.Y][o.X] // care it really is y, x (not x, y)
		t.Face = Char(rune(ch))
		switch ch {
		case '@':
			origin = t
			fov[t.Offset] = t
		case '*':
			fov[t.Offset] = t
		case '%':
			fov[t.Offset] = t
			t.Pass = false
			t.Lite = false
		case '#':
			t.Pass = false
			t.Lite = false
		}
		return t
	})
	expected := make(map[Offset]*Tile)
	for off, tile := range fov {
		expected[off.Sub(origin.Offset)] = tile
	}
	return origin, expected
}

func FoVEquals(actual, expected map[Offset]*Tile) bool {
	if len(actual) != len(expected) {
		return false
	}
	for off, atile := range actual {
		if etile, ok := expected[off]; !ok || atile != etile {
			return false
		}
	}
	return true
}

func TestFoV(t *testing.T) {
	cases := []struct {
		g StrGrid
		r int
	}{
		{
			StrGrid{
				"%%%%%##",
				"%****.#",
				"%*@**.#",
				"%****.#",
				"%****.#",
				"#######",
			}, 2,
		}, {
			StrGrid{
				"%%%%%%##",
				"%*****.#",
				"%*@***.#",
				"%*****.#",
				"%*****.#",
				"%*****.#",
				"#......#",
				"########",
			}, 3,
		}, {
			StrGrid{
				"%%%%%%%%%%#####",
				"%**********...#",
				"%**********...#",
				"%*@***%.......#",
				"%**********...#",
				"%**********...#",
				"%%%%%%%%%%#####",
			}, 8,
		}, {
			StrGrid{
				"%%%%%%%%%%#####",
				"%**********...#",
				"%*****%.......#",
				"%*@***%.......#",
				"%**********...#",
				"%**********...#",
				"%%%%%%%%%%#####",
			}, 8,
		}, {
			StrGrid{
				"%%%%%%#########",
				"%****.........#",
				"%***..........#",
				"%*@%..........#",
				"%***..........#",
				"%****.........#",
				"%%%%%%#########",
			}, 8,
		},
	}
	for i, c := range cases {
		origin, expected := c.g.FoVCase()
		actual := FoV(origin, c.r)
		if !FoVEquals(actual, expected) {
			t.Errorf("FoV cased %d failed", i)
		}
	}
}

func (g StrGrid) LoSCase() (origin, goal *Tile, expected map[*Tile]struct{}) {
	cols, rows := len(g[0]), len(g)
	expected = make(map[*Tile]struct{})
	GenTileGrid(cols, rows, func(o Offset) *Tile {
		t := NewTile(o)
		ch := g[o.Y][o.X]
		t.Face = Char(rune(ch))
		switch ch {
		case '@':
			origin = t
		case '$':
			goal = t
			expected[t] = struct{}{}
		case 'x':
			expected[t] = struct{}{}
		case '#':
			t.Lite = false
		}
		return t
	})

	if len(expected) <= 1 {
		return origin, goal, nil
	}
	return origin, goal, expected
}

func LoSEquals(actual []*Tile, ok bool, expected map[*Tile]struct{}) bool {
	if expected == nil && (ok || actual != nil) {
		return false
	}
	if expected != nil && (!ok || actual == nil) {
		return false
	}

	actualSet := make(map[*Tile]struct{})
	for _, tile := range actual {
		actualSet[tile] = struct{}{}
	}

	if len(actualSet) != len(expected) {
		return false
	}
	for tile := range expected {
		if _, ok := actualSet[tile]; !ok {
			return false
		}
	}

	return true
}

func TestLoS(t *testing.T) {
	cases := []StrGrid{
		{
			"###############",
			"#.............#",
			"#..@xxxxxxx$..#",
			"#.............#",
			"###############",
		}, {
			"###############",
			"#.............#",
			"#..@...#...$..#",
			"#.............#",
			"###############",
		}, {
			"###############",
			"#.............#",
			"#..@x###......#",
			"#....xxxxxxxx$#",
			"###############",
		}, {
			"###############",
			"#.............#",
			"#..$.###......#",
			"#............@#",
			"###############",
		}, {
			"##########################",
			"#@......................$#",
			"##########################",
		},
	}
	for i, g := range cases {
		origin, goal, expected := g.LoSCase()
		actual, ok := LoS(origin, goal, 15)
		if !LoSEquals(actual, ok, expected) {
			t.Errorf("LoS case %d failed", i)
		}
	}
}

func TestLoS_levels(t *testing.T) {
	g := StrGrid{
		"###############",
		"#.............#",
		"#..@.......$..#",
		"#.............#",
		"###############",
	}
	origin, _, _ := g.LoSCase()
	_, goal, expected := g.LoSCase()
	actual, ok := LoS(origin, goal, 15)
	if !LoSEquals(actual, ok, expected) {
		t.Errorf("LoS levels case failed")
	}
}

func TestLoS_nil(t *testing.T) {
	origin, goal, _ := StrGrid{
		"###############",
		"#.............#",
		"#..@.......$..#",
		"#.............#",
		"###############",
	}.LoSCase()

	if actual, ok := LoS(origin, nil, 15); !LoSEquals(actual, ok, nil) {
		t.Errorf("LoS(origin, nil) case failed")
	}
	if actual, ok := LoS(nil, goal, 15); !LoSEquals(actual, ok, nil) {
		t.Errorf("LoS(nil, goal) case failed")
	}
	if actual, ok := LoS(nil, nil, 15); !LoSEquals(actual, ok, nil) {
		t.Errorf("LoS(nil, nil) case failed")
	}
}

func TestLoS_match(t *testing.T) {
	for i := 0; i < 10; i++ {
		grid := GenTileGrid(20, 20, func(o Offset) *Tile {
			t := NewTile(o)
			if RandChance(.1) {
				t.Pass = false
				t.Lite = false
			}
			return t
		})
		GenFence(grid, func(t *Tile) {
			t.Pass = false
			t.Lite = false
		})
		origin, goal := RandOpenTile(grid), RandOpenTile(grid)
		fov := FoV(origin, 10)
		fovok := false
		for _, tile := range fov {
			if tile == goal {
				fovok = true
				break
			}
		}
		_, losok := LoS(origin, goal, 10)
		if fovok != losok {
			t.Errorf("LoS does not match FoV")
		}
	}
}
