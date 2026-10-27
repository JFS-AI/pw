package lab02.examples;

public class RogueThread {
    public static volatile double busyWork = 0;

    private static class Rogue implements Runnable {
        @Override
        public void run() {
            while (true) {
                System.out.println("Never gonna stop!");
                for (int i = 0; i < 20000000; ++i) {
                    busyWork += Math.random();
                }
            }
        }

    }

    public static void main(String[] args) {
        Thread t = new Thread(new Rogue());
        t.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Main interrupted during sleep.");
        }

        System.err.println("Trying to stop the bad guy.");
        t.interrupt();
        System.err.println("Oops, it didn't work...");
    }
}
