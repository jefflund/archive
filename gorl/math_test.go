package gorl

import (
	"math"
	"testing"
)

func TestMax(t *testing.T) {
	cases := []struct {
		x, y     int
		expected int
	}{
		{-4, 4, 4},
		{0, 4, 4},
		{4, 4, 4},
		{8, 4, 8},
		{4, 0, 4},
		{4, -4, 4},
	}
	for _, c := range cases {
		if actual := Max(c.x, c.y); c.expected != actual {
			t.Errorf("Max(%d, %d) = %d != %d", c.x, c.y, actual, c.expected)
		}
	}
}

func TestMin(t *testing.T) {
	cases := []struct {
		x, y     int
		expected int
	}{
		{-4, 4, -4},
		{0, 4, 0},
		{4, 4, 4},
		{8, 4, 4},
		{4, 0, 0},
		{4, -4, -4},
	}
	for _, c := range cases {
		if actual := Min(c.x, c.y); c.expected != actual {
			t.Errorf("Min(%d, %d) = %d != %d", c.x, c.y, actual, c.expected)
		}
	}
}

func TestMod(t *testing.T) {
	cases := []struct {
		x, y     int
		expected int
	}{
		{-10, 5, 0},
		{-9, 5, 1},
		{-8, 5, 2},
		{-7, 5, 3},
		{-6, 5, 4},
		{-5, 5, 0},
		{-4, 5, 1},
		{-3, 5, 2},
		{-2, 5, 3},
		{-1, 5, 4},
		{0, 5, 0},
		{1, 5, 1},
		{2, 5, 2},
		{3, 5, 3},
		{4, 5, 4},
		{5, 5, 0},
		{6, 5, 1},
		{7, 5, 2},
		{8, 5, 3},
		{9, 5, 4},
		{10, 5, 0},
	}
	for _, c := range cases {
		if actual := Mod(c.x, c.y); c.expected != actual {
			t.Errorf("Mod(%d, %d) = %d != %d", c.x, c.y, actual, c.expected)
		}
	}
}

func TestAbs(t *testing.T) {
	cases := []struct {
		x        int
		expected int
	}{
		{1, 1},
		{2, 2},
		{3, 3},
		{0, 0},
		{-1, 1},
		{-2, 2},
		{-3, 3},
	}
	for _, c := range cases {
		if actual := Abs(c.x); c.expected != actual {
			t.Errorf("Abs(%d) = %d != %d", c.x, actual, c.expected)
		}
	}
}

func TestClamp(t *testing.T) {
	cases := []struct {
		min, val, max int
		expected      int
	}{
		{-1, -2, 1, -1},
		{-1, -1, 1, -1},
		{-1, 0, 1, 0},
		{-1, 1, 1, 1},
		{-1, 2, 1, 1},

		{0, -1, 10, 0},
		{0, 0, 10, 0},
		{0, 1, 10, 1},
		{0, 5, 10, 5},
		{0, 9, 10, 9},
		{0, 10, 10, 10},
		{0, 11, 10, 10},
	}
	for _, c := range cases {
		if actual := Clamp(c.min, c.val, c.max); c.expected != actual {
			t.Errorf("Clamp(%d, %d, %d) = %d != %d", c.min, c.val, c.max, actual, c.expected)
		}
	}
}

func TestSign(t *testing.T) {
	cases := []struct {
		x, expected int
	}{
		{-10, -1},
		{-100, -1},
		{-1, -1},
		{0, 0},
		{1, 1},
		{10, 1},
		{100, 1},
	}
	for _, c := range cases {
		if actual := Sign(c.x); c.expected != actual {
			t.Errorf("Sign(%d) = %d != %d", c.x, actual, c.expected)
		}
	}
}

func TestRound(t *testing.T) {
	cases := []struct {
		x        float64
		expected int
	}{
		{math.Nextafter(-.5, 0), 0},
		{-0.5, -1},
		{math.Nextafter(-.5, -1), -1},
		{math.Nextafter(0, -1), 0},
		{0, 0},
		{math.Nextafter(0, 1), 0},
		{math.Nextafter(.5, 0), 0},
		{.5, 1},
		{math.Nextafter(.5, 1), 1},
		{math.Nextafter(1, 0), 1},
		{1, 1},
		{math.Nextafter(1, 2), 1},
		{math.Nextafter(1.5, 1), 1},
		{1.5, 2},
		{math.Nextafter(1.5, 2), 2},
		{math.Nextafter(-1.5, -1), -1},
		{-1.5, -2},
		{math.Nextafter(-1.5, -2), -2},
	}
	for _, c := range cases {
		if actual := Round(c.x); actual != c.expected {
			t.Errorf("Round(%f) = %d != %d", c.x, actual, c.expected)
		}
	}
}
