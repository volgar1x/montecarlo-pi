import java.util.stream.IntStream;
import java.util.Random;

public class MontecarloPI {
    static IntStream repeat(int value, int times) {
        int[] tab = new int[times];
        for (int i = 0; i < times; i++) {
            tab[i] = value;
        }
        return IntStream.of(tab);
    }

    static int sample(int iters) {
        Random r = new Random(System.nanoTime());
        int inside = 0;
        for (int i = 0; i < iters; i++) {
            double x = r.nextDouble(),
                   y = r.nextDouble();

            if (x*x+y*y < 1.0) {
                inside++;
            }
        }
        return inside;
    }

    public static void main(String[] args) {
        final int precision   = Integer.parseInt(args[0]);
        final int concurrency = Integer.parseInt(args[1]);
        final int iters       = precision / concurrency;

        int result = repeat(iters, concurrency)
            .parallel()
            .map(MontecarloPI::sample)
            .reduce(0, Integer::sum);

        double pi = (double) result / precision * 4;
        System.out.println(pi);
    }
}
