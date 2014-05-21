package load

import (
	"bufio"
	"os"
	"strings"
)

func GetConstraints(filename string) [][]string {
	file, err := os.Open(filename)
	if err != nil {
		panic(err)
	}
	defer file.Close()

	constraints := make([][]string, 0)
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		constraint := strings.Fields(scanner.Text())
		constraints = append(constraints, constraint)
	}
	if err := scanner.Err(); err != nil {
		panic(err)
	}

	return constraints
}
