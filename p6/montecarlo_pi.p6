sub sample(Int $n) {
  my $inside = 0;
  for ^$n {
    my $x = rand;
    my $y = rand;
    if ($x*$x+$y*$y) < 1.0 {
      $inside++;
    }
  }
  return $inside;
}

multi sub MAIN(Int $n = 1000000, Int $c = 4) {
  my $iters = ($n / $c).floor;

  my $result = [+] await do for ^$c {
    start {
      sample($iters)
    }
  }

  my $pi = $result / $n * 4;
  say $pi;
}

