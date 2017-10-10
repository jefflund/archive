package main

import (
	"time"
)

func main() {
	var obj *int

	init := func() {
		if obj == nil {
			obj = new(int)
		}
	}

	go init()
	go init()

	time.Sleep(time.Second)
}
