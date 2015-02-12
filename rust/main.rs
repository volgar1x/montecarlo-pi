#![feature(core)]
#![feature(box_syntax)]
#![feature(os)]
#![feature(rand)]
#![feature(std_misc)]

use std::iter::AdditiveIterator;
use std::rand;
use std::rand::distributions::{IndependentSample, Range};
use std::str::FromStr;
use std::sync::mpsc;
use std::thread::Thread;

fn main() {
    let args = std::os::args();
    let n: i32 = match FromStr::from_str(&args[1]) {
        Ok(r) => r,
        _ => panic!("The `n` argument was not found. Input expected: {n (i32)} {concurrency level (i32)}")
    };
    let concurrency : i32 = match FromStr::from_str(&args[2]) {
        Ok(r) => r,
        _ => panic!("The `concurrency` argument was not found. Input expected: {n (i32)} {concurrency level (i32)}")
    };

    let iters = n / concurrency;

    let (tx, rx) = mpsc::channel::<i32>();

    for _ in range(0, concurrency) {
        let tx = tx.clone();

        Thread::spawn(move || {
            let between = Range::new(0f64, 1.0);
            let mut rng = rand::thread_rng();

            tx.send(range(0, iters).map(|_| { 
                let x = between.ind_sample(&mut rng);
                let y = between.ind_sample(&mut rng);

                match x*x+y*y { 
                    r if(r < 1.0) => 1,
                    _ => 0 
                }
            }).sum()).ok().expect("Could not send the result");
        });
    }

    let result = range(0, concurrency)
                .map(|_| rx.recv().unwrap())
                .sum();
    let pi: f64 =  (result as f64) / (n as f64) * 4.0;
    println!("{}", pi);
}
