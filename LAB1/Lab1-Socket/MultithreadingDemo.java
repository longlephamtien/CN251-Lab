/**
 * MultithreadingDemo - A comprehensive demonstration of multithreading concepts
 * This class demonstrates all the key concepts from your requirements:
 * 1. Creating threads with Runnable interface
 * 2. Thread interruption and graceful shutdown
 * 3. Multithreaded server applications
 */
public class MultithreadingDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Java Multithreading Demonstration ===\n");
        
        // Demo 1: Basic PrimeRun example
        demonstratePrimeRun();
        
        // Demo 2: Thread interruption
        demonstrateThreadInterruption();
        
        // Demo 3: Information about other examples
        demonstrateOtherExamples();
        
        System.out.println("=== Demo completed! ===");
    }
    
    /**
     * Demonstrates the basic PrimeRun example from your requirements
     */
    private static void demonstratePrimeRun() {
        System.out.println("1. PrimeRun Example:");
        System.out.println("   Creating PrimeRun with minPrime = 143");
        
        // This follows your exact example:
        PrimeRun p = new PrimeRun(143);
        Thread primeThread = new Thread(p);
        primeThread.start();
        
        try {
            // Wait for the thread to complete (with timeout for demo purposes)
            primeThread.join(3000); // Wait max 3 seconds
            if (primeThread.isAlive()) {
                System.out.println("   Thread is still running after 3 seconds, interrupting...");
                primeThread.interrupt();
                primeThread.join(1000); // Wait another second for graceful shutdown
            }
        } catch (InterruptedException e) {
            System.out.println("   Main thread was interrupted");
        }
        
        System.out.println("   PrimeRun demonstration completed\n");
    }
    
    /**
     * Demonstrates thread interruption concepts
     */
    private static void demonstrateThreadInterruption() {
        System.out.println("2. Thread Interruption Example:");
        
        // Create a simple thread that can be interrupted
        InterruptibleWorker worker = new InterruptibleWorker();
        Thread workerThread = new Thread(worker, "InterruptibleWorker");
        
        System.out.println("   Starting InterruptibleWorker...");
        workerThread.start();
        
        try {
            // Let it run for 2 seconds
            Thread.sleep(2000);
            
            System.out.println("   Calling stop() method (which calls interrupt())...");
            worker.stop();
            
            // Wait for graceful shutdown
            workerThread.join(2000);
            
        } catch (InterruptedException e) {
            System.out.println("   Main thread was interrupted");
        }
        
        System.out.println("   Thread interruption demonstration completed\n");
    }
    
    /**
     * Provides information about other examples in the workspace
     */
    private static void demonstrateOtherExamples() {
        System.out.println("3. Additional Examples Available:");
        System.out.println("   - ThreadInterruptionExample.java: Comprehensive thread interruption examples");
        System.out.println("     * Shows how to handle interruption in various scenarios");
        System.out.println("     * Demonstrates server socket interruption");
        System.out.println("     * Proper cleanup and resource management");
        System.out.println();
        System.out.println("   - MultithreadedServerExample.java: Complete multithreaded server");
        System.out.println("     * Implements the architecture from your diagram");
        System.out.println("     * Uses thread pools for efficient resource management");
        System.out.println("     * Handles multiple client connections concurrently");
        System.out.println("     * Includes test clients to demonstrate functionality");
        System.out.println();
        System.out.println("   To run these examples:");
        System.out.println("   $ javac ThreadInterruptionExample.java && java ThreadInterruptionExample");
        System.out.println("   $ javac MultithreadedServerExample.java && java MultithreadedServerExample");
        System.out.println();
    }
    
    /**
     * Simple interruptible worker class for demonstration
     */
    static class InterruptibleWorker implements Runnable {
        private volatile boolean running = false;
        
        /**
         * Stop method that sets running flag and interrupts the thread
         * This follows your example pattern
         */
        public void stop() {
            running = false;
            Thread.currentThread().interrupt();
        }
        
        @Override
        public void run() {
            running = true;
            int counter = 0;
            
            System.out.println("   Worker started in thread: " + Thread.currentThread().getName());
            
            while (running && !Thread.currentThread().isInterrupted()) {
                System.out.println("   Working... iteration " + (++counter));
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("   Worker was interrupted during sleep");
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break;
                }
            }
            
            System.out.println("   Worker stopped gracefully after " + counter + " iterations");
        }
    }
}