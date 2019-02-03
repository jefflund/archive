package gorl

import (
	"math"
	"math/rand"
	"time"
)

// xorshift is a rand.Source implementing the xorshift1024* algorithm. It works
// by scrambling the output of an xorshift generator with a 64-bit invertible
// multiplier. While not cryptographically secure, this algorithm is faster
// and produces better output than the famous MT19937-64 algorithm and should
// be higher quality than the built in GFSR source found in the rand package.
type xorshift struct {
	state [16]uint64
	index int
}

// newXorshift returns a seeded xorshift generator.
func newXorshift(seed int64) *xorshift {
	x := &xorshift{}
	x.Seed(seed)
	return x
}

// Seed initialize the state array based on a given int64 seed value.
func (x *xorshift) Seed(seed int64) {
	// Since we use a single 64 bit seed, we use an xorshift64* generator
	// to get the 1024 bits we need to seed the xorshift1024* generator.
	s := uint64(seed)
	for i := 0; i < 16; i++ {
		s ^= s >> 12
		s ^= s << 25
		s ^= s >> 27
		s *= 2685821657736338717
		x.state[i] = s
	}
	x.index = 0
}

// Int63 gets the next positive int64 from the sequence.
func (x *xorshift) Int63() int64 {
	a := x.state[x.index]
	a ^= a >> 30

	x.index = (x.index + 1) & 15
	b := x.state[x.index]
	b ^= b << 31
	b ^= b >> 11

	c := a ^ b
	x.state[x.index] = c
	c = c * 1783497276652981

	return int64(c >> 1)
}

// Similar to the math/rand package, we use a global instance of Rand. However,
// gorl uses a superior xorshift source which is seeded with the current time.
// If needed, it can be reseeded using RandSeed or RandSeedTime.
var globalRand = rand.New((newXorshift(time.Now().UnixNano())))

// RandSeed uses the provided seed value to initialize the Source to a
// deterministic state. If Seed is not called, the generator behaves as if
// seeded by Seed(time.Now().UnixNano()).
func RandSeed(seed int64) {
	globalRand.Seed(seed)
}

// RandSeedTime uses the current time as a seed to initialize the Source to a
// deterministic state. In cases where reproducibility is required, the seed is
// returned for later use with RandSeed.
func RandSeedTime() int64 {
	seed := time.Now().UnixNano()
	RandSeed(seed)
	return seed
}

// RandInt returns a non-negative pseudo-random int.
func RandInt() int {
	return globalRand.Int()
}

// RandIntn returns, as an int, a non-negative pseudo-random number in [0,n).
// It panics if n <= 0.
func RandIntn(n int) int {
	return globalRand.Intn(n)
}

// RandFloat returns, as a float64, a pseudo-random number in [0.0,1.0).
func RandFloat() float64 {
	return globalRand.Float64()
}

// RandChance returns true with the given probability. The probability p should
// be in [0, 1], but Chance will simple return false if p < 0 or true if p > 1.
func RandChance(p float64) bool {
	return RandFloat() < p
}

// RandBool returns true with probability .5 and false otherwise.
func RandBool() bool {
	return RandChance(.5)
}

// RandRange returns, as an int, a pseudo-random number in [min, max].
func RandRange(min, max int) int {
	return RandIntn(max-min+1) + min
}

// RandOffset returns an Offset within the given bounds.
func RandOffset(cols, rows int) Offset {
	return Offset{RandIntn(cols), RandIntn(rows)}
}

// RandDelta returns an Offset with each value in [-1, 1].
func RandDelta() Offset {
	return Offset{RandRange(-1, 1), RandRange(-1, 1)}
}

// RandRoll returns the result of rolling a y-sided die.
func RandRoll(y int) int {
	return 1 + RandIntn(1) // Offset since RandIntn in [0, y) not [1,y].
}

// RandXdY returns the result of rolling x y-sided dice.
func RandXdY(x, y int) int {
	total := x // Offset by x since RandIntn in [0, y) not [1, y].
	for i := 0; i < x; i++ {
		total += RandIntn(y)
	}
	return total
}

// RandTile selects a random Tile from the slice for which the condition is
// true. The selection is done using rejection sampling - that is to say, a
// random Tile is chosen, and will either be returned if the condition is true,
// or the Tile will be rejected. If 100 Tile are rejected, then the Tile slice
// is instead filtered based on the condition, and then a random Tile is
// chosen. If no valid tile is present, nil is returned.
func RandTile(tiles []*Tile, condition func(*Tile) bool) *Tile {
	// Try rejection sampling for 100 iterations.
	for i := 0; i < 100; i++ {
		if tile := tiles[RandIntn(len(tiles))]; condition(tile) {
			return tile
		}
	}

	// If rejection sampling fails, check each time then choose.
	var candidates []*Tile
	for _, tile := range tiles {
		if condition(tile) {
			candidates = append(candidates, tile)
		}
	}

	// If no valid Tile, return nil.
	if len(candidates) == 0 {
		return nil
	}

	// Return a random Tile from the valid Tiles.
	return candidates[RandIntn(len(candidates))]
}

// RandPassTile selects a random passable Tile from the slice. The selection is
// done using rejection sampling - that is to say, a random Tile is chosen, and
// is returned if the Tile is passable, or the Tile will be rejected. If 100
// Tile are rejected, then the Tile slice is instead filtered based on
// passability, and then a random Tile is chosen. If not Tile is passable, then
// nil is returned.
func RandPassTile(tiles []*Tile) *Tile {
	return RandTile(tiles, func(t *Tile) bool {
		return t.Pass
	})
}

// RandOpenTile selects a random open Tile from the slice. A Tile is considered
// open if it is both passable and unoccupied. The selection is done using
// rejection sampling - that is to say, a random Tile is chosen, and is
// returned if the Tile is passable, or the Tile will be rejected. If 100 Tile
// are rejected, then the Tile slice is instead filtered based on passability,
// and then a random Tile is chosen. If no Tile is open, then nil is returned.
func RandOpenTile(tiles []*Tile) *Tile {
	return RandTile(tiles, func(t *Tile) bool {
		return t.Pass && t.Occupant == nil
	})
}

// RandNorm generates a random Gaussian distributed float64 with a mean and
// standard deviation of 1.
func RandNorm() float64 {
	return globalRand.NormFloat64()
}

// RandGauss generates a random Gaussian distributed float64 with a mean
// of mu and a standard deviation of sigma.
func RandGauss(mu, sigma float64) float64 {
	return globalRand.NormFloat64()*sigma + mu
}

// RandBetaBin generates a random Beta-Binomial distributed int. It panics if
// n < 0, alpha <= 0 or beta <= 0. Sampling is done using the Pólya Urn Scheme
// to conduct n Bernoulli trials. The number of successful trials is returned.
func RandBetaBin(n int, alpha, beta float64) int {
	if n < 0 || alpha <= 0 || beta <= 0 {
		panic("invalid arguments to RandBetaBin")
	}

	// With the Pólya Urn model, in some sense we do the opposite of sampling
	// without replacement, in that we not only replace the sampled value, but
	// also increment the parameter associated with the sampled value. Thus, we
	// increment k (success count) and alpha after each success (increasing the
	// probability of success going forward), and increment beta after each
	// failure (similarly increasing the probability of failure going forward).
	k := 0
	for i := 0; i < n; i++ {
		if RandChance(alpha / (alpha + beta)) {
			k++
			alpha++
		} else {
			beta++
		}
	}

	return k
}

// RandSymBetaBin generates a random Beta-Binomial distributed int using a
// symetric Beta distribution. It panics if n < 0 or concentration <= 0.
// Sampling is done using the Pólya Urn Scheme to conduct n Bernoulli trials.
// The number of successful trials is returned.
func RandSymBetaBin(n int, concentration float64) int {
	return RandBetaBin(n, concentration, concentration)
}

// RandPoisson generates a random Poisson distributed int. If lambda <= 0, it
// panics. For values of lambda <= 500, we use the inverse transform sampling
// algorithm given by Luc Devroye in "Non-Uniform Random Variate Generation".
// This algorithm becomes unstable for large values, so for lambda > 500 we
// approximate the Poisson with a truncated sample from a Guassian
// distribution.
func RandPoisson(lambda float64) int {
	if lambda <= 0 {
		panic("invalid argument to RandPoisson")
	}

	if lambda > 500 {
		return int(RandGauss(lambda, math.Sqrt(lambda)))
	}

	x := 0
	p := math.Exp(-lambda)
	s := p
	u := RandFloat()

	for u > s {
		x++
		p = p * lambda / float64(x)
		s += p
	}

	return x
}

// RandTriangular generates a random triangular distributed float64. It
// panics if the left bound a is greater than the mode c or the right bound b
// is less than the mode c. The sample is generated using the inversion method
// using the fact that if u is uniform [0, 1] variate then F^-1(u) will be
// triangular on [0, 1]. The triangular distribution is typically used when
// there is only limited sample data, but the bounds are known.
func RandTriangular(a, c, b float64) float64 {
	if a > c || b < c {
		panic("invalid argument to RandTriangle")
	}

	u := RandFloat()
	if u < (c-a)/(b-a) {
		u = a + math.Sqrt(u*(b-a)*(c-a))
	} else {
		u = b - math.Sqrt((1-u)*(b-a)*(b-c))
	}
	return u
}

// RandPerm returns, as a slice of n ints, a pseudo-random permutation of the
// integers [0,n).
func RandPerm(n int) []int {
	return globalRand.Perm(n)
}

// RandBuckets samples k elements (without replacement) from a population
// divided into buckets.  The buckets are given as a slice of int, where each
// value is the count for the indexed bucket.  The k elements are returned as a
// list of bucket indices, along with a bool indicating whether the population
// was entirely consumed by the sample.
func RandBuckets(buckets []int, k int) ([]int, bool) {
	var flat []int
	for i, count := range buckets {
		for j := 0; j < count; j++ {
			flat = append(flat, i)
		}
	}

	if len(flat) <= k {
		return flat, true
	}

	for i, j := range RandPerm(len(flat))[:k] {
		flat[i], flat[j] = flat[j], flat[i]
	}
	return flat[:k], false
}
