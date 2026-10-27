package lab03.examples;

import java.util.concurrent.CountDownLatch;

public class CountDown {
    private static final int ITERATIONS = 100000;
    private static final int ITERATIONS_BEFORE_WAKEUP = 50000;

    private static volatile int counter = 0;
    private static final CountDownLatch latch = new CountDownLatch(ITERATIONS_BEFORE_WAKEUP);

    public static void main(String[] args) {
        Thread timekeeper = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; ++i) {
                if (Thread.interrupted()) {
                    System.err.println("Timekeeper interrupted.");
                    break;
                }
                counter = i + 1;
                latch.countDown();
            }
        });

        timekeeper.start();

        try {
            // Main thread sleeps until timekeeper finishes ITERATIONS_BEFORE_WAKEUP iterations:
            latch.await();
            System.out.println(counter); // >= ITERATIONS_BEFORE_WAKEUP
            timekeeper.join();
            System.out.println(counter); // == ITERATIONS
        } catch (InterruptedException e) {
            System.err.println("Main interrupted");
        }
    }
}
