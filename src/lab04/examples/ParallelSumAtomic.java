package lab04.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelSumAtomic {
    private static final int THREADS_COUNT = 100;
    private static final int COUNT = 1000;

    // "Final" here means the reference cannot be changed,
    // but the referenced object can be changed.
    // (In Java, all non-primitive types, i.e. other than
    // boolean, char, int, long, float, double,
    // are stored as references.
    private static final AtomicInteger sum = new AtomicInteger(0);

    public static void main(String[] args) {
        // We create THREADS_COUNT threads, each simultaneously
        // trying to increase a single shared variable COUNT times
        // but this time we use an atomic variable
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < COUNT; j++) {
                    sum.incrementAndGet();
                }
            }));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted.");
            for (Thread thread : threads) {
                thread.interrupt();
            }
        }

        // It should print exactly 100000 ;)
        System.out.println(sum);
    }
}
