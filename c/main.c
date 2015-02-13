#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <pthread.h>

void* 
sample(void* pn) {
    srand((int)time(NULL) ^ (int)pthread_self());

    int n, i, inside;
    double x, y, z;

    n = *((int*) pn);

    inside = 0;
    for (i = 0; i < n; i++) {
        x = ((double)rand()/(double)RAND_MAX);
        y = ((double)rand()/(double)RAND_MAX);

        z = x*x+y*y;

        if (z < 1.0) {
            inside++;
        }
    }


    return (void *)inside;
}


int 
main(int argc, char **argv) {
    int n = 1000000000;
    int concurrency = 4;
    int iters = n / concurrency;

    int i, rc, result;
    void *ret;

    pthread_t threads[concurrency];

    for (i = 0; i < concurrency; i++) {
        rc = pthread_create(threads+1, NULL, sample, &iters);
    }

    result = 0;

    for (i = 0; i < concurrency; i++) {
        rc = pthread_join(threads[i], &ret);
        result += (int)ret;
    }

    double pi = (double) result / (double) n * 4.0;

    printf("%f\n", pi);

    return 0;
}
