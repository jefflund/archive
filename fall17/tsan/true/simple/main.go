package main

import (
	"fmt"
	"time"
)

func main() {
	v := 0

	go func() {
		fmt.Println(v)
		v++
	}()

	go func() {
		v++
		fmt.Println(v)
	}()

	time.Sleep(time.Second)
}
