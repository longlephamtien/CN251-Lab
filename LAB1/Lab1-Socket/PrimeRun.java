/**
 * PrimeRun class implements Runnable to compute prime numbers
 * larger than a given minimum value in a separate thread.
 */
class PrimeRun implements Runnable {
    private long minPrime;
    
    public PrimeRun(long minPrime) {
        this.minPrime = minPrime;
    }
    
    public void run() {
        System.out.println("Thread " + Thread.currentThread().getName() + 
                          " started searching for primes larger than " + minPrime);
        
        // Compute primes larger than minPrime
        long candidate = minPrime + 1;
        int foundPrimes = 0;
        final int MAX_PRIMES = 10; // Limit output for demonstration
        
        while (foundPrimes < MAX_PRIMES && !Thread.currentThread().isInterrupted()) {
            if (isPrime(candidate)) {
                System.out.println("Thread " + Thread.currentThread().getName() + 
                                 " found prime: " + candidate);
                foundPrimes++;
            }
            candidate++;
            
            // Add a small delay to make the threading more visible
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Thread " + Thread.currentThread().getName() + " was interrupted");
                Thread.currentThread().interrupt(); // Restore interrupted status
                break;
            }
        }
        
        System.out.println("Thread " + Thread.currentThread().getName() + " finished");
    }
    
    /**
     * Helper method to check if a number is prime
     */
    private boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Example usage of PrimeRun
     */
    public static void main(String[] args) {
        System.out.println("Starting PrimeRun demonstration...");
        
        // Create and start multiple threads
        PrimeRun p1 = new PrimeRun(143);
        PrimeRun p2 = new PrimeRun(200);
        PrimeRun p3 = new PrimeRun(300);
        
        Thread thread1 = new Thread(p1, "PrimeThread-1");
        Thread thread2 = new Thread(p2, "PrimeThread-2");
        Thread thread3 = new Thread(p3, "PrimeThread-3");
        
        thread1.start();
        thread2.start();
        thread3.start();
        
        // Wait for threads to complete
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread was interrupted");
        }
        
        System.out.println("All threads completed!");
    }
}