import os, math, threadpool, strutils

proc sample(iters: int): int =
    for k in 1..iters:
        let x = random(1.0)
        let y = random(1.0)
        if x*x + y*y < 1.0:
            result += 1;


proc run(n: int, concurrency: int): float64 = 
    let iters = int(n / concurrency)
    var acc = newSeq[int](concurrency)
    parallel:
        for i in 0..acc.high:
            acc[i] = spawn sample(iters)

    for i in 0..acc.high:
        result += float(acc[i])

    result = 4.0 * result / float(n)

let args = commandLineParams()
echo(run(parseInt(args[0]), parseInt(args[1])))