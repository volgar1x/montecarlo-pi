#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>

static double dd = (double) RAND_MAX + 1;

double rand_rd(unsigned int *seed) {
  int val = rand_r(seed);
  return val / dd;
}

int sample(int n) {
  unsigned int seed = time(NULL);

  int i;
  int inside;
  double x, y, z;

  inside = 0;
  for (i = 0; i < n; i++) {
    x = rand_rd(&seed);
    y = rand_rd(&seed);

    z = x*x+y*y;

    if (z < 1.0) {
      inside++;
    }
  }

  return inside;
}

int main(int argc, char **argv) {

  int n = 1000000000;
  int result = sample(n);
  double pi = (double) result / (double) n * 4.0;

  printf("%f\n", pi);

  return 0;
}
