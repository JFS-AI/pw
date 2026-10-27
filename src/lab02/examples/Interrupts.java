package lab02.examples;

import java.time.Duration;
import java.time.Instant;

public class Interrupts {
    private static Instant startTime = Instant.now();

    private static void printTime() {
        System.out.println("Time since start: " + Duration.between(startTime, Instant.now()).toMillis() + " ms");
    }

    private static void doUninterruptibleWork(Duration workTime) {
        Instant start = Instant.now();
        while (Instant.now().isBefore(start.plus(workTime))) {
            Math.random();
        }
    }

    private static void doBusyWork(Duration workTime) throws InterruptedException {
        Instant start = Instant.now();
        while (Instant.now().isBefore(start.plus(workTime))) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            doUninterruptibleWork(Duration.ofMillis(200));
        }
    }

    private static class Helper implements Runnable {
        private final Duration sleepTime;
        private final Duration workTime;
        public String message = "";

        public Helper(Duration sleepTime, Duration workTime) {
            this.sleepTime = sleepTime;
            this.workTime = workTime;
        }

        @Override
        public void run() {
            String phase = "sleep";
            try {
                Thread.sleep(sleepTime.toMillis());

                phase = "computations";
                doBusyWork(workTime);
                // doUninterruptibleWork(workTime);

                phase = "finished";
                message = Thread.currentThread().getName() + " finished without seeing any interrupt.";

            } catch (InterruptedException e) {
                Thread current = Thread.currentThread();
                message = current.getName() + " saw interrupt during " + phase + ", isInterrupted()=" + current.isInterrupted();
            }
        }
    }

    public static void main(String args[]) {
        try {

            Thread first = new Thread(new Helper(Duration.ofSeconds(1), Duration.ofSeconds(1)), "First");
            Helper sleeper = new Helper(Duration.ofSeconds(10), Duration.ZERO);
            Thread sleeperThread = new Thread(sleeper, "Sleeper");
            Helper worker = new Helper(Duration.ZERO, Duration.ofSeconds(10));
            Thread workerThread = new Thread(worker, "Worker");
            first.start();
            sleeperThread.start();
            workerThread.start();
            printTime();

            first.join();
            printTime();

            sleeperThread.interrupt();
            sleeperThread.join();
            printTime();

            workerThread.interrupt();
            workerThread.join();
            printTime();

            System.err.println(sleeperThread.getName() + ".message = " + sleeper.message);
            System.err.println(workerThread.getName() + ".message = " + worker.message);

        } catch (InterruptedException e) {
            System.out.println("Main interrupted.");
        }
    }
}
