package main

import (
	"fmt"
	"sync"
)

var names = []string{
	"Alice",
	"Bob",
	"Carol",
	"Dennis",
	"Egbert",
	"Farquad",
	"George",
	"Heather",
	"Igrid",
	"Jessica",
}

func main() {
	wg := new(sync.WaitGroup)
	people := make(chan string)
	go func() {
		for _, name := range names { // Pretend this came from file or network
			wg.Add(1)
			people <- name
		}
		close(people)
	}()

	match := make(chan string)

	for name := range people {
		go func() {
			select {
			case peer := <-match:
				fmt.Printf("%s matched with %s\n", name, peer)
			case match <- name:
				// wait for someone to get message
			}
			wg.Done()
		}()
	}

	wg.Wait()
}
