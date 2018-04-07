package main

import (
	"fmt"
	"sync"
	"time"
)

func main() {
	sem1 := make(chan struct{})
	sem2 := make(chan struct{})
	lock := new(sync.Mutex)
	var x int

	go func() {
		x = 1
		sem1 <- struct{}{} // sem1.Post()

		<-sem2 // sem2.Wait()

		time.Sleep(time.Nanosecond)
		lock.Lock()
		fmt.Println("x = 3")
		x = 3
		lock.Unlock()
	}()

	go func() {
		<-sem1
		fmt.Println(x)
		sem2 <- struct{}{}

		lock.Lock()
		fmt.Println("x = 4")
		x = 4
		lock.Unlock()
		time.Sleep(time.Nanosecond)

		fmt.Println(x)
	}()

	time.Sleep(time.Second)
}
