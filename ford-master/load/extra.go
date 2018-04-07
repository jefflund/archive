package load

import (
	"bufio"
	"math"
	"os"
	"strings"
)

func GetConstraints(filename string) (int, [][]string) {
	file, err := os.Open(filename)
	if err != nil {
		panic(err)
	}

	minlength := math.MaxInt32
	constraints := make([][]string, 0)
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		constraint := strings.Fields(scanner.Text())
		if len(constraint) < minlength {
			minlength = len(constraint)
		}
		constraints = append(constraints, constraint)
	}
	if err := scanner.Err(); err != nil {
		panic(err)
	}

	return minlength, constraints
}
