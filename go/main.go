package main

import (
	"fmt"
	"math/rand"
	"runtime"
	"time"
)

func sample(n int) (inside int) {
	rnd := rand.New(rand.NewSource(time.Now().UnixNano()))

	inside = 0
	for i := 0; i < n; i++ {
		x := rnd.Float64()
		y := rnd.Float64()

		z := x*x + y*y
		if z < 1.0 {
			inside++
		}
	}

	return
}

func th_sample(n int, out chan<- int) {
	result := sample(n)
	out <- result
}

func run(n int, c int) float64 {
	var iters int
	var sum int
	results := make(chan int)

	iters = n / c

	for i := 0; i < c; i++ {
		go th_sample(iters, results)
	}

	for i := 0; i < c; i++ {
		sum += <-results
	}

	return float64(sum) / float64(n) * 4.0
}

func main() {
	runtime.GOMAXPROCS(4)
	pi := run(1000000000, 4)
	fmt.Println(pi)
}
