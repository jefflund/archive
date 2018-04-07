package main

import (
	"time"
)

func main() {
	done := false

	go func() {
		for !done {
			time.Sleep(time.Second)
		}
	}()

	go func() {
		done = true
	}()

	time.Sleep(time.Second)
}
