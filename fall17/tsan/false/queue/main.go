package main

import (
	"fmt"
	"sync/atomic"
	"time"
)

type RefCounter struct {
	refs int64
}

func (r *RefCounter) AtomicUnref() {
	if atomic.AddInt64(&r.refs, -1) == 0 {
		fmt.Println("delete")
	}
}

func (r *RefCounter) UnsyncUnref() {
	r.refs--
	if r.refs == 0 {
		fmt.Println("delete")
	}
}

func main() {
	r := &RefCounter{10}
	for i := 0; i < 10; i++ {
		go func() {
			r.AtomicUnref()
		}()
	}

	time.Sleep(time.Second)
}
