package main

import (
	"fmt"

	"ford/dataset"
)

func main() {
	c := dataset.Newsgroups.Import()
	for d := 0; d < c.M; d++ {
		fmt.Println(d, c.Titles[d], c.GetText(d))
	}
}
