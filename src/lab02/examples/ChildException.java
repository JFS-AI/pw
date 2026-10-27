package lab02.examples;

public class ChildException {
    private static class MessException extends RuntimeException {
        public MessException() {
            super("mess");
        }
    }

    public static void main(String[] args) {
        Thread child = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " will throw.");
            throw new MessException();
        }, "BadChild");

        boolean noticed = false;
        try {
            child.start();
            child.join();
            System.out.println("child.isAlive() = " + child.isAlive());
            System.out.println("child.isInterrupted() = " + child.isInterrupted());
        } catch (MessException e) {
            System.out.println("Main got MessException.");
            noticed = true;
        } catch (InterruptedException e) {
            System.out.println("Main got InterruptedException.");
            noticed = true;
        }

        if (!noticed)
            System.out.println("Main saw no exception.");
    }
}
