package gorl

import (
	"math"
	"testing"
)

func TestOffset_Sub(t *testing.T) {
	cases := []struct {
		a, b, expected Offset
	}{
		{Offset{5, 4}, Offset{3, 6}, Offset{2, -2}},
		{Offset{7, 3}, Offset{-1, -1}, Offset{8, 4}},
		{Offset{7, 5}, Offset{6, 3}, Offset{1, 2}},
	}
	for _, c := range cases {
		if actual := c.a.Sub(c.b); actual != c.expected {
			t.Errorf("%v.Sub(%v) = %v != %v", c.a, c.b, actual, c.expected)
		}
	}
}

func TestOffset_Add(t *testing.T) {
	cases := []struct {
		a, b, expected Offset
	}{
		{Offset{5, 4}, Offset{3, 6}, Offset{8, 10}},
		{Offset{7, 3}, Offset{-1, -1}, Offset{6, 2}},
		{Offset{7, 5}, Offset{6, 3}, Offset{13, 8}},
	}
	for _, c := range cases {
		if actual := c.a.Add(c.b); actual != c.expected {
			t.Errorf("%v.Add(%v) = %v != %v", c.a, c.b, actual, c.expected)
		}
	}
}

func TestOffset_Manhattan(t *testing.T) {
	cases := []struct {
		o        Offset
		expected int
	}{
		{Offset{4, 6}, 10},
		{Offset{-4, 6}, 10},
		{Offset{4, -6}, 10},
		{Offset{-4, -6}, 10},
	}
	for _, c := range cases {
		if actual := c.o.Manhattan(); actual != c.expected {
			t.Errorf("%v.Manhattan() = %v != %v", c.o, actual, c.expected)
		}
	}
}

func TestOffset_Euclidean(t *testing.T) {
	cases := []struct {
		o        Offset
		expected float64
	}{
		{Offset{3, 4}, 5},
		{Offset{-3, 4}, 5},
		{Offset{3, -4}, 5},
		{Offset{-3, -4}, 5},
		{Offset{2, 4}, math.Sqrt(20)},
		{Offset{-2, 4}, math.Sqrt(20)},
		{Offset{2, -4}, math.Sqrt(20)},
		{Offset{-2, -4}, math.Sqrt(20)},
	}
	for _, c := range cases {
		if actual := c.o.Euclidean(); actual != c.expected {
			t.Errorf("%v.Euclidean() = %v != %v", c.o, actual, c.expected)
		}
	}
}

func TestOffset_Chebyshev(t *testing.T) {
	cases := []struct {
		o        Offset
		expected int
	}{
		{Offset{4, 6}, 6},
		{Offset{-4, 6}, 6},
		{Offset{4, -6}, 6},
		{Offset{-4, -6}, 6},
	}
	for _, c := range cases {
		if actual := c.o.Chebyshev(); actual != c.expected {
			t.Errorf("%v.Chebyshev() = %v != %v", c.o, actual, c.expected)
		}
	}
}
