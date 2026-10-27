package lab02.examples;

import java.util.concurrent.ThreadLocalRandom;

public class TwoWritersPeterson {
    private static final int MAX_BUSY_TIME_MS = 100;
    private static final int LINES_COUNT = 100;
    private static final int LINE_LENGTH = 50;

    private enum WriterType {
        LETTERS, DIGITS
    }

    private static volatile boolean lettersWants = false;
    private static volatile boolean digitsWants = false;
    private static volatile WriterType waiting = WriterType.LETTERS;

    private static void busyWork() throws InterruptedException {
        Thread.sleep(ThreadLocalRandom.current().nextInt(MAX_BUSY_TIME_MS));
    }

    /**
     * Write a line of LINE_LENGTH chars from the range first..last, inclusive.
     * Characters are repeated in a loop, starting with `current`.
     * Returns the next character after the last one written.
     */
    private static char writeLine(char first, char current, char last) {
        char c = current;
        for (int j = 0; j < LINE_LENGTH; ++j) {
            System.out.print(c);
            ++c;
            if (c > last) {
                c = first;
            }
        }
        System.out.println();
        return c;
    }

    private static class Letters implements Runnable {
        private final char FIRST_CHAR = 'a';
        private final char LAST_CHAR = 'z';

        @Override
        public void run() {
            try {
                char c = FIRST_CHAR;
                for (int i = 0; i < LINES_COUNT; ++i) {
                    busyWork();
                    lettersWants = true;
                    waiting = WriterType.LETTERS;
                    while (digitsWants && (waiting == WriterType.LETTERS)) {
                        Thread.yield();
                    }
                    c = writeLine(FIRST_CHAR, c, LAST_CHAR);
                    lettersWants = false;
                }
            } catch (InterruptedException e) {
                System.err.println("Letters interrupted.");
            }
        }
    }

    private static class Digits implements Runnable {
        private final char FIRST_CHAR = '0';
        private final char LAST_CHAR = '9';

        @Override
        public void run() {
            try {
                char c = FIRST_CHAR;
                for (int i = 0; i < LINES_COUNT; ++i) {
                    busyWork();
                    digitsWants = true;
                    waiting = WriterType.DIGITS;
                    while (lettersWants && (waiting == WriterType.DIGITS)) {
                        Thread.yield();
                    }
                    c = writeLine(FIRST_CHAR, c, LAST_CHAR);
                    digitsWants = false;
                }
            } catch (InterruptedException e) {
                System.err.println("Digits interrupted.");
            }
        }
    }

    public static void main(String[] args) {
        Thread letters = new Thread(new Letters());
        Thread digits = new Thread(new Digits());
        System.out.println("Started.");
        letters.start();
        digits.start();

        try {
            letters.join();
            digits.join();
            System.out.println("Finished.");
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted.");
            letters.interrupt();
            digits.interrupt();
        }
    }
}
