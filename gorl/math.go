package gorl

import (
	"math"
)

// Max returns the maximum of x and y.
func Max(x, y int) int {
	if y > x {
		return y
	}
	return x
}

// Min returns the minimum of x and y.
func Min(x, y int) int {
	if y < x {
		return y
	}
	return x
}

// Mod returns x modulo y (not the same as x % y, which is remainder).
func Mod(x, y int) int {
	z := x % y
	if z < 0 {
		z += y
	}
	return z
}

// Abs returns the absolute value of x.
func Abs(x int) int {
	if x < 0 {
		return -x
	}
	return x
}

// Clamp limits a value to a specific range.
func Clamp(min, val, max int) int {
	if val < min {
		return min
	} else if val > max {
		return max
	}
	return val
}

// Sign returns 1 if x > 0, -1 if x < 0, and 0 if x = 0.
func Sign(x int) int {
	return Clamp(-1, x, 1)
}

// Round returns x rounded to the nearest integer.
func Round(x float64) int {
	if math.Abs(x) < .5 {
		return 0
	}
	return int(x + math.Copysign(.5, x))
}
