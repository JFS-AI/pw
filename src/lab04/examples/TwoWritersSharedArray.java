package lab04.examples;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class TwoWritersSharedArray {
    private static final int LINES_COUNT = 5;
    private static final int LINE_LENGTH = 60;
    private static final int WRITERS_COUNT = 2;
    private static final int N_LETTERS_PER_WRITER = LINE_LENGTH * LINES_COUNT / WRITERS_COUNT;

    private static final char[] buffer = new char[WRITERS_COUNT * N_LETTERS_PER_WRITER];

    private static final AtomicInteger nextLetterIndex = new AtomicInteger(0);

    private static class Writer implements Runnable {
        private final char firstChar;
        private final char lastChar;

        public Writer(char firstChar, char lastChar) {
            this.firstChar = firstChar;
            this.lastChar = lastChar;
        }

        @Override
        public void run() {
            try {
                char c = firstChar;
                for (int i = 0; i < N_LETTERS_PER_WRITER; ++i) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1, 10));

                    // Threads can write to this shared buffer safely
                    // because we know each call to `getAndIncrement()`
                    // will return a different index.
                    int index = nextLetterIndex.getAndIncrement();
                    buffer[index] = c;

                    ++c;
                    if (c > lastChar) {
                        c = firstChar;
                    }
                }
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + " interrupted.");
            }
        }
    }

    public static void main(String[] args) {
        Arrays.fill(buffer, ' ');
        Thread letters = new Thread(new Writer('a', 'z'), "Letters");
        Thread digits = new Thread(new Writer('0', '9'), "Digits");
        letters.start();
        digits.start();

        try {
            letters.join();
            digits.join();
        } catch (InterruptedException e) {
            System.err.println("Main thread interrupted.");
            letters.interrupt();
            digits.interrupt();
        }

        int column = 0;
        for (char c : buffer) {
            System.out.print(c);
            ++column;
            if (column == LINE_LENGTH) {
                System.out.println();
                column = 0;
            }
        }
        System.out.println();

    }
}
