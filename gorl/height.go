package gorl

import (
	"math"
	"sort"
)

// Heightmap is a grid of float64 used for generation of overworld maps.
type Heightmap struct {
	cols, rows int
	buf        [][]float64
}

// NewHeightmap creates a Heightmap with the given dimensions and all zeros.
func NewHeightmap(cols, rows int) *Heightmap {
	b := make([][]float64, cols)
	for x := 0; x < cols; x++ {
		b[x] = make([]float64, rows)
	}
	return &Heightmap{cols, rows, b}
}

// Dim gets the number of columns and rows of the Heightmap.
func (h *Heightmap) Dim() (int, int) {
	return h.cols, h.rows
}

// Read gets the height of the Heightmap at (x, y).
func (h *Heightmap) Read(x, y int) float64 {
	return h.buf[x][y]
}

// Write sets the height of the Heightmap at (x, y).
func (h *Heightmap) Write(x, y int, v float64) {
	h.buf[x][y] = v
}

// Apply uses the given callback with each value in the Heightmap.
func (h *Heightmap) Apply(f func(x, y int, v float64)) {
	for x := 0; x < h.cols; x++ {
		for y := 0; y < h.rows; y++ {
			f(x, y, h.buf[x][y])
		}
	}
}

// Transform sets the value of each value in the Heightmap using the output of
// the given callback with the current values.
func (h *Heightmap) Transform(f func(x, y int, v float64) float64) {
	for x := 0; x < h.cols; x++ {
		for y := 0; y < h.rows; y++ {
			h.buf[x][y] = f(x, y, h.buf[x][y])
		}
	}
}

// Gen creates a 2D grid of Tile using the given GenTileFloat with the
// heights from the Heightmap.
func (h *Heightmap) Gen(f GenTileFloat) []*Tile {
	return GenTileGrid(h.cols, h.rows, func(o Offset) *Tile {
		return f(o, h.buf[o.X][o.Y])
	})
}

// GenTerrain creates a 2D grid of Tile using a GenTileFloat with height values
// from a Heightmap created using NewHeightTerrain.
func GenTerrain(cols, rows int, f GenTileFloat) []*Tile {
	return NewHeightmapTerrain(cols, rows).Gen(f)
}

// NewHeightmapTerrain creates a new Heightmap representing elevation. The
// terrain representated by the elevation map will be mostly smooth with evenly
// distributed values in [0, 1].
func NewHeightmapTerrain(cols, rows int) *Heightmap {
	h := NewHeightmap(cols, rows)

	// Generate terrain with various ellipses.
	n := h.cols + h.rows
	h.ellipses(h.cols/8, h.rows/8, n, 1)
	h.ellipses(h.cols/16, h.rows/16, n/2, -1)
	h.ellipses(h.cols/16, h.rows/16, n/2, 2)

	// Clean up ellipses for better terrain-like appearance.
	h.smooth(h.cols / 100)
	h.equalize()
	h.normalize()
	return h
}

// NewHeightmapTemperature creates a new Heightmap from an elevation Heightmap
// with temperature values in [0, 1].
func NewHeightmapTemperature(h *Heightmap, sealevel float64) *Heightmap {
	t := NewHeightmap(h.cols, h.rows)

	// Compute base temperature with softsign on latitude minus elevation from
	// the sealevel.
	equator := float64(h.rows) / 2
	t.Transform(func(x, y int, v float64) float64 {
		lat := 1 - math.Abs(float64(y)-equator)/equator
		v = 2 * lat / (1 + lat)
		v -= (math.Max(h.buf[x][y], sealevel) - sealevel)
		return v
	})

	// Find the max temperature on land.
	max := t.buf[0][0]
	t.Apply(func(x, y int, v float64) {
		if h.buf[x][y] > sealevel {
			max = math.Max(max, v)
		}
	})

	// Normalize sea temps by max land temp.
	t.Transform(func(x, y int, v float64) float64 {
		if h.buf[x][y] <= sealevel {
			v /= max
		}
		return v
	})

	// Clean up the temperature map for a smoother appearance.
	t.smooth(h.cols / 100)
	t.normalize()
	return t
}

// Direction of tradewinds loosely based on Hadley cells.
var winds = []Offset{
	{0, 0},
	{0, 1},
	{0, -1},
	{1, 0},
	{-1, 1},
	{-1, 1},
	{0, 0},
	{-1, -1},
	{-1, -1},
	{1, 0},
	{0, 1},
	{0, -1},
	{0, 0},
}

// NewHeightmapRain creates a new Heightmap from an elevation Heightmap with
// precipitation values in [0, 1].
func NewHeightmapRain(h *Heightmap, sealevel float64) *Heightmap {
	p := NewHeightmap(h.cols, h.rows)

	// Initialize frontier with ocean tiles set to max precipitation.
	frontier := []Offset{}
	p.Transform(func(x, y int, v float64) float64 {
		if h.buf[x][y] <= sealevel {
			v = 1
			frontier = append(frontier, Offset{x, y})
		}
		return v
	})

	// Breadth-first expansion of frontier to update rain values.
	for len(frontier) > 0 {
		// Pop top value front frontier queue.
		c := frontier[0]
		frontier = frontier[1:]

		// Compute prevaling wind direction from latitude.
		w := winds[int(float64(c.Y)/float64(h.rows)*float64(len(winds)))]
		// Compute height and current rain value.
		z := math.Max(h.buf[c.X][c.Y], sealevel)
		r := p.buf[c.X][c.Y]

		// Visit each neighbor.
		for dx := -1; dx <= 1; dx++ {
			for dy := -1; dy <= 1; dy++ {
				tx, ty := Mod(c.X+dx, h.cols), c.Y+dy
				if ty < 0 || ty >= h.rows {
					continue
				}

				// Compute elevation change, and devation from prevailing wind.
				dz := math.Abs(math.Max(h.buf[tx][ty], sealevel) - z)
				wz := float64(Abs(dx-w.X)+Abs(dy-w.Y)) / 2
				// Propagate rain, discounting for elevation and wind.
				if v := r * (1 - dz) * (1 - wz); v > p.buf[tx][ty] {
					p.buf[tx][ty] = v
					// After updating, have to reexplore neighbors.
					frontier = append(frontier, Offset{tx, ty})
				}
			}
		}
	}

	// Set sea tiles to average rain value for better distribution of rain over
	// land after smoothing and normalizing.
	var sum, n float64
	p.Apply(func(x, y int, v float64) {
		if h.buf[x][y] > sealevel {
			sum += v
			n++
		}
	})
	avg := sum / n
	p.Transform(func(x, y int, v float64) float64 {
		if h.buf[x][y] <= sealevel {
			return avg
		}
		return v
	})

	p.smooth(h.cols / 50)
	p.normalize()
	return p
}

// ellipses adds n randomly placed ellipses with radii rx and ry with height z.
func (h *Heightmap) ellipses(rx, ry, n int, z float64) {
	rx2, ry2 := float64(rx*rx), float64(ry*ry)
	for i := 0; i < n; i++ {
		cx, cy := RandIntn(h.cols), RandRange(-ry, h.rows+ry)
		for dx := -rx; dx <= rx; dx++ {
			for dy := -ry; dy <= ry; dy++ {
				// If outside the ellipse, skip the point.
				if float64(dx*dx)/rx2+float64(dy*dy)/ry2 >= 1 {
					continue
				}

				// Raise the horizontally wrapped point if it is in bounds.
				x, y := Mod(cx+dx, h.cols), cy+dy
				if 0 <= y && y < h.rows {
					h.buf[x][y] += z
				}
			}
		}
	}
}

// smooth sets each cell to be the average of its neighbors within radius r.
func (h *Heightmap) smooth(r int) {
	// Copy the buffer so we can update while reading previous state.
	c := make([][]float64, h.cols)
	for x := 0; x < h.cols; x++ {
		c[x] = make([]float64, h.rows)
		copy(c[x], h.buf[x])
	}

	// Update values to be average of neighbors values.
	for x := 0; x < h.cols; x++ {
		for y := 0; y < h.rows; y++ {
			sum, n := 0., 0.
			for tx := x - r; tx <= x+r; tx++ {
				for ty := y - r; ty <= y+r; ty++ {
					if ty < 0 || ty >= h.rows {
						continue
					}
					sum += c[Mod(tx, h.cols)][ty]
					n++
				}
			}
			h.buf[x][y] = sum / n
		}
	}
}

// equalize performs histogram equalization on the Heightmap.
func (h *Heightmap) equalize() {
	// Compute the histogram function.
	hist := make([]float64, h.cols*h.rows)
	h.Apply(func(x, y int, v float64) {
		hist[x+y*h.cols] = v
	})
	sort.Float64s(hist)

	// Compute the transfer function from the cumulative distribution.
	cumulative := 0.0
	transfer := make(map[float64]float64)
	for _, height := range hist {
		cumulative += height
		transfer[height] = cumulative
	}

	// Apply the transfer function to the Heightmap.
	h.Transform(func(x, y int, v float64) float64 {
		return transfer[v]
	})
}

// normalize maps every value of the Heightmap to the range [0, 1].
func (h *Heightmap) normalize() {
	// Compute the min and max heights.
	min, max := h.buf[0][0], h.buf[0][0]
	h.Apply(func(x, y int, v float64) {
		min = math.Min(min, v)
		max = math.Max(max, v)
	})

	// Normalize the heights to [0, 1].
	span := max - min
	h.Transform(func(x, y int, v float64) float64 {
		return (v - min) / span
	})
}
