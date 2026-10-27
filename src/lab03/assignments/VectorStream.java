package lab03.assignments;

import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.IntBinaryOperator;

public class VectorStream {
    private static final int STREAM_LENGTH = 10;
    private static final int VECTOR_LENGTH = 100;
    private static int sum = 0;
    private static final int[] vector = new int[VECTOR_LENGTH];
    private static int vectorNo = 1;
    private static final CyclicBarrier barrier = new CyclicBarrier(VECTOR_LENGTH,
        new BarrierAction());

    private final static class BarrierAction implements Runnable {
        @Override
        public void run() {
            sum = 0;
            for (int x : vector) {
                sum += x;
            }
            System.out.println(vectorNo++ + " -> " + sum);
        }
    }

    /**
     * Function that defines how vectors are computed: the i-th element depends on
     * the previous sum and the index i.
     * The sum of elements in the previous vector is initially given as zero.
     */
    private final static IntBinaryOperator vectorDefinition = (previousSum, i) -> {
        int a = 2 * i + 1;
        return (previousSum / VECTOR_LENGTH + 1) * (a % 4 - 2) * a + 1;
    };

    private final static class Helper implements Runnable {
        final int idx;
        Helper(int idx) {
            this.idx = idx;
        }

        @Override
        public void run() {
            for (int i = 0; i < STREAM_LENGTH; i++) {
                vector[idx] = vectorDefinition.applyAsInt(sum, idx);
                try {
                    barrier.await();
                } catch (BrokenBarrierException e) {
                    System.out.println("Broken barrier");
                } catch (InterruptedException e) {
                    System.out.println("Interrupted");
                }
            }
        }
    }

    private static void computeVectorStreamSequentially() {
        int[] vector = new int[VECTOR_LENGTH];
        int sum = 0;
        for (int vectorNo = 0; vectorNo < STREAM_LENGTH; ++vectorNo) {
            for (int i = 0; i < VECTOR_LENGTH; ++i) {
                vector[i] = vectorDefinition.applyAsInt(sum, i);
            }
            sum = 0;
            for (int x : vector) {
                sum += x;
            }
            System.out.println(vectorNo + " -> " + sum);
        }
    }

    private static void computeVectorStreamInParallel() throws InterruptedException {
        // FIXME: implement, using VECTOR_LENGTH threads.
        ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < VECTOR_LENGTH; ++i) {
            threads.add(new Thread(new Helper(i)));
            threads.get(i).start();
        }

        for (Thread t : threads) {
            while (t.isAlive()) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    System.out.println("Interrupted while joining.");
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("-- Sequentially --");
            computeVectorStreamSequentially();
            System.out.println("-- Parallel --");
            computeVectorStreamInParallel();
            System.out.println("-- End --");
        } catch (InterruptedException e) {
            System.err.println("Main interrupted.");
        }
    }
}
