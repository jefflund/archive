package gorl

// GenTile generates Tile for various map generators. The typical use case is to
// wrap other GenTile types for use with NewTileGrid.
type GenTile func(o Offset) *Tile

// GenTileFloat generates Tiles from float64 values.
type GenTileFloat func(o Offset, h float64) *Tile

// GenTileBool generates Tiles from bool values.
type GenTileBool func(o Offset, p bool) *Tile

// GenTileGrid creates a 2D grid of Tile using the given GenTile.
func GenTileGrid(cols, rows int, f GenTile) []*Tile {
	grid := make(map[Offset]*Tile)
	for x := 0; x < cols; x++ {
		for y := 0; y < rows; y++ {
			grid[Offset{x, y}] = f(Offset{x, y})
		}
	}
	return connectMap(grid, nil)
}

// GenTileMutate mutates extisting Tiles to modify existing maps.
type GenTileMutate func(*Tile)

// GenFence mutates any Tile which is not 8-connected.
func GenFence(tiles []*Tile, f GenTileMutate) {
	for _, tile := range tiles {
		if len(tile.Adjacent) < 8 {
			f(tile)
		}
	}
}

// GenTransform applies a transformation to an entire map.
func GenTransform(tiles []*Tile, f GenTileMutate) {
	for _, tile := range tiles {
		f(tile)
	}
}

// dirs4 gives the 4 cardinal directions.
var dirs4 = []Offset{
	{0, 1},
	{0, -1},
	{1, 0},
	{-1, 0},
}

// dirs8 gives the 8 point compass rose directions.
var dirs8 = []Offset{
	{0, 1},
	{0, -1},
	{1, 0},
	{-1, 0},
	{1, 1},
	{1, -1},
	{-1, 1},
	{-1, -1},
}

// GenCave creates a 2D map of Tile using the given GenTileBool, where true
// indicates a passable cave floor, and false indicates an impassible cave
// wall. Size is the number of floor tiles before the cave is finished by
// rounding edges, and adding walls, meaning that the final cave will be
// slightly larger than the given size.
func GenCave(size int, f GenTileBool) []*Tile {
	cave := make(map[Offset]*Tile)

	var walk func(Offset)
	walk = func(o Offset) {
		if _, visited := cave[o]; visited {
			return
		}
		cave[o] = f(o, true)

		if len(cave) < size {
			for _, i := range RandPerm(len(dirs4)) {
				walk(o.Add(dirs4[i]))
			}
		}
	}
	walk(Offset{})

	return connectMap(cave, func(o Offset) *Tile {
		return f(o, false)
	})
}

// GenMaze creates a 2D map of Tile using the given GenTileBool, where true
// indicates a passable maze floor, and false indicates an impassible maze
// wall. Size is the number of floor tiles before the maze is finished by
// adding walls, meaning that the final maze will be slightly larger than the
// given size.
func GenMaze(size int, f GenTileBool) []*Tile {
	maze := map[Offset]*Tile{{}: f(Offset{}, true)}

	var walk func(Offset, Offset)
	walk = func(src, delta Offset) {
		if len(maze) > size {
			return
		}

		// Step twice in delta direction, stopping if we reach a tile we've
		// already visited. Since we always step twice, the in between step
		// will add a loop if we revisit a tile.
		between := src.Add(delta)
		maze[between] = f(between, true)
		dst := between.Add(delta)
		if _, visited := maze[dst]; visited {
			return
		}
		maze[dst] = f(dst, true)

		// Recursively perform random walk to dig maze. Discourage long runs by
		// swapping the first and last steps if the first step is a repeat.
		perm := RandPerm(len(dirs4))
		if dirs4[perm[0]] == delta {
			perm[0], perm[3] = perm[3], perm[0]
		}
		for _, i := range perm {
			walk(dst, dirs4[i])
		}
	}
	walk(Offset{}, dirs4[RandIntn(len(dirs4))])

	return connectMap(maze, func(o Offset) *Tile {
		return f(o, false)
	})
}

// connect 8-connects each tile in a tilemap to its neighbors. If f is non-nil,
// border tiles (presumably walls) are added to complete the map. The entire
// map is returned as a slice.
func connectMap(tilemap map[Offset]*Tile, f GenTile) []*Tile {
	// Connect a tile to its neighbor, possibly creating the dst tile if it
	// doesn't already exist.
	link := func(src, dst Offset) {
		a := tilemap[src]
		if _, ok := tilemap[dst]; !ok && f != nil {
			tilemap[dst] = f(dst)
		}
		b := tilemap[dst]

		// Forward connect and back-connect b (in case b is a border tile).
		if b != nil {
			a.Adjacent[dst.Sub(src)] = b
			b.Adjacent[src.Sub(dst)] = a
		}
	}

	// Save existing tiles so created border additions are not linked.
	var existing []Offset
	for offset := range tilemap {
		existing = append(existing, offset)
	}

	// 8-connect each existing tile.
	for _, t := range existing {
		for _, d := range dirs8 {
			link(t, t.Sub(d))
		}
	}

	// Convert tilemap to a slice.
	var tiles []*Tile
	for _, t := range tilemap {
		tiles = append(tiles, t)
	}
	return tiles
}
