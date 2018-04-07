package main

import (
	"fmt"
	"time"
)

func main() {
	var obj *int

	go func() {
		obj = new(int)
	}()

	go func() {
		for obj == nil {
			time.Sleep(0)
		}
		fmt.Println(*obj)
	}()

	time.Sleep(time.Second)
}
