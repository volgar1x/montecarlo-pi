n = 1000000000

inside = 0

for i in [1..n]
	x = Math.random()
	y = Math.random()

	z = x*x+y*y
	if z < 1.0
		inside += 1

pi = inside / n * 4

console.log(pi)

