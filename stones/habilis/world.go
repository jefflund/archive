package habilis

import (
	"github.com/jefflund/gorl"
)

func NewWorld() []*gorl.Tile {
	tiles := gorl.GenTerrain(100, 100, func(o gorl.Offset, h float64) *gorl.Tile {
		t := gorl.NewTile(o)
		switch {
		case h < .3:
			t.Face = gorl.ColoredChar('~', gorl.ColorBlue)
			t.Pass = false
			t.AddComponent(gorl.Name("water"))
		default:
			if gorl.RandChance(.95) {
				t.Face = gorl.ColoredChar('.', gorl.ColorGreen)
			} else {
				t.Face = gorl.ColoredChar('%', gorl.ColorGreen)
				t.Pass = false
				t.Lite = false
				t.AddComponent(gorl.Name("tree"))
			}
		}
		return t
	})
	gorl.GenFence(tiles, func(t *gorl.Tile) {
		t.Pass = false
		t.Lite = false
		t.Face = gorl.Char('#')
		t.AddComponent(gorl.Name("void"))
	})
	return tiles
}
