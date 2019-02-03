package gorl

import (
	"math"
	"testing"
)

// LowerIncompleteGamma approximates the following integral:
// \sum_{k=0}^\inf \frac{(-1)^k z^{s+k}}{k!(s+k)}
func LowerIncompleteGamma(s, z float64) float64 {
	integral := 0.0
	for k := 0.0; k < 500; k++ {
		inc := (math.Pow(-1, k) * math.Pow(z, s+k)) / (math.Gamma(k+1) * (s + k))
		integral += inc
		if math.Abs(inc) < 1e-20 {
			break
		}
	}
	return integral
}

// ChiSquaredCDF computes the cumulative distribution function for the
// Chi-squared distribution.
func ChiSquaredCDF(x float64, k int) float64 {
	return LowerIncompleteGamma(float64(k)/2, x/2) / math.Gamma(float64(k)/2)
}

// ComputePearonStats computes the test statistics needed to run Pearon's
// Chi-squared goodness of fit test.
func ComputePearsonStats(obs, exp []int) (X2 float64, df int) {
	// Both obs and exp need to have the same number of categories, and same
	// total count.
	if len(obs) != len(exp) {
		panic("observed and expected dimension mismatch")
	}
	diff := 0
	for i := 0; i < len(obs); i++ {
		diff += obs[i] - exp[i]
	}
	if diff != 0 {
		panic("observed and expected total mismatch")
	}

	// Calculate Chi^2 as \sum_{i=1}^n \frac{(O_i - E_i)^2}{E_i}
	X2 = 0
	for i := 0; i < len(obs); i++ {
		X2 += math.Pow(float64(obs[i]-exp[i]), 2) / float64(exp[i])
	}

	// Degrees of freedom is 1 less than the number of categories since final
	// count is constrained by the total.
	df = len(obs) - 1

	return X2, df
}

func PearsonGoodness(obs, exp []int, alpha float64) (accept bool) {
	X2, df := ComputePearsonStats(obs, exp)
	pValue := ChiSquaredCDF(X2, df)
	return pValue <= alpha
}

// TestPearsonsGoodness is a test of the other dice tests!
func TestPearsonsGoodness(t *testing.T) {
	cases := []struct {
		Obs, Exp []int
		X2       float64
		DF       int
		PValue   float64
	}{
		{
			[]int{44, 56}, []int{50, 50},
			1.44, 1, .7699,
		}, {
			[]int{5, 8, 9, 8, 10, 20}, []int{10, 10, 10, 10, 10, 10},
			13.4, 5, 0.9801,
		}, {
			[]int{5, 8, 9, 8, 10, 20}, []int{10, 10, 10, 10, 10, 10},
			13.4, 5, 0.9801,
		},
	}
	for _, c := range cases {
		X2, df := ComputePearsonStats(c.Obs, c.Exp)
		if X2 != c.X2 || df != c.DF {
			t.Errorf("ComputePearsonStats(%v, %v) = (%f, %d) != (%f, %d)", c.Obs, c.Exp, X2, df, c.X2, c.DF)
			t.FailNow()
		}
		if pValue := ChiSquaredCDF(X2, df); math.Abs(pValue-c.PValue) > 1e-4 {
			t.Errorf("ChiSquaredCDF(%f, %d) = %f != %f", X2, df, pValue, c.PValue)
		}
	}
}

// We will be using Pearson's to do a sanity check on our dice functions. We
// could of course run a more complete statistical test of randomness (such as
// Diehard or similar), but really we just want to make sure that we aren't
// causing some pathological errors. Consequently, we'll be content to just
// run Pearson's Chi-Squared test on some of the functions we added to the rand
// package. Note that these tests are all run using the global Dice object,
// which of course tests the underlying Dice methods.

// We'll reuse a common set of random seeds in order to have deterministic tests.
var seeds = []int64{
	0xFEE15600D,
	0xFEE15BAD,
	0xFA7CA7,
	0xBADD06,
	0x0B5E55ED,
	0xFA151F1AB1E,
}

func TestRandSeed(t *testing.T) {
	n := 1000
	exp := make(map[int64][]int)
	for _, seed := range seeds {
		exp[seed] = make([]int, n)
		RandSeed(seed)
		for i := 0; i < n; i++ {
			exp[seed][i] = RandInt()
		}
	}

	for _, seed := range seeds {
		RandSeed(seed)
		for i := 0; i < n; i++ {
			if obs := RandInt(); obs != exp[seed][i] {
				t.Logf("Seed(%#X) failed to produce consistent results", seed)
				t.FailNow()
			}
		}
	}
}

func TestRandBool(t *testing.T) {
	for _, seed := range seeds {
		RandSeed(seed)
		n := 1000
		obs := []int{0, 0}
		for i := 0; i < n; i++ {
			if RandBool() {
				obs[0]++
			} else {
				obs[1]++
			}
		}
		exp := []int{n / 2, n / 2}
		if !PearsonGoodness(obs, exp, .99) {
			t.Errorf("RandBool() failed Chi-squared test (seed=%#X)", seed)
		}
	}
}

func TestRandChance(t *testing.T) {
	cases := []float64{.01, .25, .5, .75, .99}
	for _, chance := range cases {
		for _, seed := range seeds {
			RandSeed(seed)
			n := 1000
			obs := []int{0, 0}
			for i := 0; i < n; i++ {
				if RandChance(chance) {
					obs[0]++
				} else {
					obs[1]++
				}
			}
			expTrue := int(float64(n) * chance)
			exp := []int{expTrue, n - expTrue}
			if !PearsonGoodness(obs, exp, .99) {
				t.Errorf("RandChance(%f) failed Chi-squared test (%v !~= %v, seed=%#X)", chance, obs, exp, seed)
			}
		}
	}
}

func TestRandChance_bounds(t *testing.T) {
	for _, seed := range seeds {
		RandSeed(seed)
		for i := 0; i < 100; i++ {
			if RandChance(0) {
				t.Logf("RandChance(0) returned true (seed=%#X)", seed)
				t.FailNow()
			}
			if !RandChance(1) {
				t.Logf("RandChance(1) returned false (seed=%#X", seed)
				t.FailNow()
			}
		}
	}
}

func TestRandRange(t *testing.T) {
	cases := []struct {
		Min, Max int
	}{
		{-1, 1},
		{0, 10},
		{20, 28},
	}
	for _, c := range cases {
		for _, seed := range seeds {
			RandSeed(seed)
			spread := c.Max - c.Min + 1
			n := 1000

			obs := make([]int, spread)
			for i := 0; i < n; i++ {
				obs[RandRange(c.Min, c.Max)-c.Min]++
			}

			exp := make([]int, spread)
			expTotal := 0
			for i := 0; i < spread-1; i++ {
				exp[i] = n / spread
				expTotal += exp[i]
			}
			exp[spread-1] = n - expTotal

			if !PearsonGoodness(obs, exp, .99) {
				t.Errorf("RandRange(%d, %d) failed Chi-squared test (%v !~= %v, seed=%#X)", c.Min, c.Max, obs, exp, seed)
			}
		}
	}
}

func TestRandXdY(t *testing.T) {
	cases := []struct {
		X, Y int
		Dist []float64
	}{
		{
			3, 6, []float64{
				0.462962962963,
				1.38888888889,
				2.77777777778,
				4.62962962963,
				6.94444444444,
				9.72222222222,
				11.5740740741,
				12.5,
				12.5,
				11.5740740741,
				9.72222222222,
				6.94444444444,
				4.62962962963,
				2.77777777778,
				1.38888888889,
				0.462962962963,
			},
		}, {
			2, 4, []float64{
				6.25,
				12.5,
				18.75,
				25,
				18.75,
				12.5,
				6.25,
			},
		},
	}
	for _, c := range cases {
		for _, seed := range seeds {
			RandSeed(seed)
			n := 1000

			total := 0
			exp := make([]int, len(c.Dist))
			for i, prob := range c.Dist {
				exp[i] = int(prob * float64(n))
				total += exp[i]
			}

			obs := make([]int, len(c.Dist))
			for i := 0; i < total; i++ {
				obs[RandXdY(c.X, c.Y)-c.X]++
			}

			if !PearsonGoodness(obs, exp, .99) {
				t.Errorf("RollXdY(%d, %d) failed Chi-squared test (%v !~= %v, seed=%#X)", c.X, c.Y, obs, exp, seed)
			}
		}
	}
}

func TestRandBetaBin(t *testing.T) {
	cases := []struct {
		N           int
		Alpha, Beta float64
		Expected    []int
	}{
		{10, 6, 4, []int{31, 143, 375, 727, 1146, 1528, 1750, 1715, 1393, 867, 325}},
		{6, 10, 10, []int{283, 1130, 2220, 2733, 2220, 1130, 283}},
		{6, .01, .01, []int{4888, 59, 37, 33, 37, 59, 4888}},
		{6, .3, .6, []int{4140, 1331, 940, 800, 762, 820, 1207}},
		{6, .1, .3, []int{6213, 703, 450, 381, 386, 487, 1379}},
	}
	for _, seed := range seeds {
		RandSeed(seed)
		for _, c := range cases {
			samples := 0
			for _, count := range c.Expected {
				samples += count
			}

			obs := make([]int, len(c.Expected))
			for i := 0; i < samples; i++ {
				obs[RandBetaBin(c.N, c.Alpha, c.Beta)]++
			}

			if !PearsonGoodness(obs, c.Expected, .99) {
				t.Errorf("RandBetaBin(%d, %f, %f) failed Chi-squared test (%v !~= %v, seed=%#X)", c.N, c.Alpha, c.Beta, obs, c.Expected, seed)
			}
		}
	}
}

func bakeIntn(seed int64, n, peak int) {
	RandSeed(seed)
	skip := 0
	for RandIntn(n) != peak {
		skip++
	}
	RandSeed(seed)
	for i := 0; i < skip; i++ {
		RandIntn(n)
	}
}

func TestRandTile(t *testing.T) {
	cases := []struct {
		Pool  []bool
		Intn  int
		Index int
	}{
		{[]bool{true, true, true, true}, 3, 3},
		{[]bool{false, true, false, true}, 3, 3},
		{[]bool{false, false, false, false}, 3, -1},
	}
	for i, c := range cases {
		tiles := make([]*Tile, len(c.Pool))
		for i, pass := range c.Pool {
			tiles[i] = &Tile{Pass: pass}
		}
		bakeIntn(0xDEADBEEF, len(tiles), c.Intn)
		var expected *Tile
		if c.Index >= 0 {
			expected = tiles[c.Index]
		}
		actual := RandTile(tiles, func(t *Tile) bool {
			return t.Pass
		})
		if actual != expected {
			t.Errorf("RandTile case %d failed", i)
		}
	}
}
