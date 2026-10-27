package lab03.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class ProducersConsumers {
    private enum Product {
        TOILET_PAPER, TOOTHPASTE, RICE, BOTTLED_WATER
    }

    private static final int CONSUMERS_COUNT = 8;
    private static final int[] PRODUCER_COUNTS = { 10, 7, 2, 1 }; // for each type of product
    private static final int PRODUCED_COUNT = 200;
    private static final int CONSUMED_COUNT = 500;
    private static final int BUFFER_SIZE = 10;

    private static int firstTaken = 0;
    private static int firstFree = 0;

    private static final Product[] buffer = new Product[BUFFER_SIZE];

    private static final Semaphore takenCount = new Semaphore(0, true);
    private static final Semaphore freeCount = new Semaphore(BUFFER_SIZE, true);

    private static final Semaphore takenMutex = new Semaphore(1, true);
    private static final Semaphore freeMutex = new Semaphore(1, true);

    private static Product consume() throws InterruptedException {
        takenCount.acquire();
        takenMutex.acquire();
        Product product = buffer[firstTaken];
        firstTaken = (firstTaken + 1) % BUFFER_SIZE;
        takenMutex.release();
        freeCount.release();
        return product;
    }

    private static void produce(Product product) throws InterruptedException {
        freeCount.acquire();
        freeMutex.acquire();
        buffer[firstFree] = product;
        firstFree = (firstFree + 1) % BUFFER_SIZE;
        freeMutex.release();
        takenCount.release();
    }

    private static class Producer implements Runnable {
        private final Product productType;
        private final int productionDelay;
        private int producedProducts = 0;

        public Producer(Product productType, int productionDelay) {
            this.productType = productType;
            this.productionDelay = productionDelay;
        }

        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            try {
                for (int i = 0; i < PRODUCED_COUNT; ++i) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(productionDelay));
                    produce(productType);
                    producedProducts++;
                }

                System.out.println(name + " has produced " + producedProducts + " units of " + productType + ".");
            } catch (InterruptedException e) {
                System.err.println(name + " interrupted.");
            }
        }
    }

    private static class Consumer implements Runnable {
        private final int consumingDelay;
        private final int[] shoppingBag;

        private Consumer(int consumingDelay) {
            this.consumingDelay = consumingDelay;
            this.shoppingBag = new int[Product.values().length];
        }

        @Override
        public void run() {
            String name = Thread.currentThread().getName();
            try {
                for (int i = 0; i < CONSUMED_COUNT; ++i) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(consumingDelay));
                    Product product = consume();
                    shoppingBag[product.ordinal()]++;
                }

                System.out.println(name + " has consumed:");
                for (int i = 0; i < Product.values().length; i++) {
                    System.out.println(" - " + shoppingBag[i] + " units of " + Product.values()[i]);
                }
            } catch (InterruptedException e) {
                System.err.println(name + " interrupted.");
            }
        }
    }

    public static void main(String[] args) {
        if (PRODUCER_COUNTS.length != Product.values().length) {
            throw new AssertionError();
        }
        if (Arrays.stream(PRODUCER_COUNTS).sum() * PRODUCED_COUNT != CONSUMERS_COUNT * CONSUMED_COUNT) {
            throw new AssertionError();
        }

        List<Thread> threads = new ArrayList<>();
        for (int producerType = 0; producerType < PRODUCER_COUNTS.length; producerType++) {
            for (int i = 0; i < PRODUCER_COUNTS[producerType]; i++) {
                Product product = Product.values()[producerType];
                int delay = 1 + ThreadLocalRandom.current().nextInt(100);
                threads.add(new Thread(new Producer(product, delay), "Producer" + product + i));
            }
        }

        for (int i = 0; i < CONSUMERS_COUNT; ++i) {
            int delay = 1 + ThreadLocalRandom.current().nextInt(100);
            threads.add(new Thread(new Consumer(delay), "Consumer" + i));
        }

        for (Thread t : threads) {
            t.start();
        }

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Main interrupted");
        }
    }
}
