package lab02.examples;

import java.util.ArrayList;
import java.time.Duration;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class SpawnableWorkers {
    private static final boolean EXIT_SHOULD_INTERRUPT = true;
    private static class Worker implements Runnable {
        private final int id;
        private final Duration workTime;

        public Worker(int id, Duration workTime) {
            this.id = id;
            this.workTime = workTime;
        }

        @Override
        public void run() {
            try {
                System.out.println("Worker " + id + " started.");
                Thread.sleep(workTime.toMillis());
                System.out.println("Worker " + id + " finished after " + workTime.toSeconds() + " s.");
            } catch (InterruptedException e) {
                System.out.println("Worker " + id + " interrupted!");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("A positive number n starts a new worker which will work for n seconds.");
        System.out.println("A negative number -n interrupts the worker with id n.");
        System.out.println("Zero ends the main thread.");

        int nextWorkerId = 1;
        List<Thread> workers = new ArrayList<>();

        // We use "try-with-resources": automatically call scanner.close() at the end.
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                int number = 0;
                try {
                    number = scanner.nextInt(); // Blocks the thread until some input is read.
                } catch (InputMismatchException e) {
                    System.out.println("Incorrect input, retry.");
                    continue;
                }

                if (number > 0) {
                    // Start new worker.
                    Thread thread = new Thread(new Worker(nextWorkerId, Duration.ofSeconds(number)));
                    workers.add(thread);
                    nextWorkerId++;
                    thread.start();
                } else if (number < 0) {
                    // Interrupt worker.
                    int id = -number;
                    if (id >= nextWorkerId) {
                        System.out.println("No worker with such id");
                    } else if (!workers.get(id - 1).isAlive()) {
                        System.out.println("Worker already finished");
                    } else {
                        workers.get(id - 1).interrupt();
                    }
                } else {
                    // Exit.
                    break;
                }
            }

            if (EXIT_SHOULD_INTERRUPT) {
                System.out.println("Interrupting all workers...");
                for (Thread thread : workers) {
                    thread.interrupt();
                }
                for (Thread thread : workers) {
                    thread.join();
                }
            }
            System.out.println("Main thread finished.");
        } catch (InterruptedException e) {
            System.out.println("Main thread interrupted!");
        }
    }
}
