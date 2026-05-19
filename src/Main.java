import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static long countPoints(long n) {
        long count = 0;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (long i = 0; i < n; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x * x + y * y <= 1.0) {
                count++;
            }
        }
        return count;
    }

    public static double calculate(long N, int M) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(M);
        long batchSize = N / M;
        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < M; i++) {
            futures.add(executor.submit(() -> countPoints(batchSize)));
        }

        long totalInCircle = 0;
        for (Future<Long> future : futures) {
            totalInCircle += future.get();
        }

        executor.shutdown();
        return 4.0 * totalInCircle / N;
    }

    public static void main(String[] args) {
        long[] nValues = {1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L};
        int[] mValues = {1, 2, 4, 8, 16, 32, 64, 128};

        String path = "results/pi_monte_carlo_parallel_results.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            StringBuilder header = new StringBuilder("N / M");
            for (int m : mValues) header.append(",").append(m);
            writer.println(header.toString());

            for (long n : nValues) {
                StringBuilder row = new StringBuilder(String.valueOf(n));
                for (int m : mValues) {
                    System.out.printf("Running N=%d, M=%d...%n", n, m);

                    long startTime = System.nanoTime();
                    calculate(n, m);
                    long endTime = System.nanoTime();

                    double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
                    row.append(String.format(",%.4f", durationSeconds));
                    System.out.printf("Done: %.4fs%n", durationSeconds);
                }
                writer.println(row.toString());
                writer.flush();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}