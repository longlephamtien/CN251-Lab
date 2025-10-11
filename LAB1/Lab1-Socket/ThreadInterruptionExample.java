import java.io.*;
import java.net.*;

/**
 * ThreadInterruptionExample demonstrates how to properly use Thread.interrupt()
 * to stop threads gracefully, including scenarios with blocking operations.
 */
public class ThreadInterruptionExample {
    
    /**
     * Simple worker thread that can be interrupted
     */
    static class SimpleWorker implements Runnable {
        private volatile boolean running = false;
        
        public void stop() {
            running = false;
            Thread.currentThread().interrupt();
        }
        
        @Override
        public void run() {
            running = true;
            int counter = 0;
            
            while (running && !Thread.currentThread().isInterrupted()) {
                System.out.println("SimpleWorker working... " + counter++);
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("SimpleWorker was interrupted during sleep");
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break;
                }
            }
            
            System.out.println("SimpleWorker stopped gracefully");
        }
    }
    
    /**
     * Server thread that demonstrates interruption with socket operations
     */
    static class ServerWorker implements Runnable {
        private volatile boolean running = false;
        private ServerSocket serverSocket;
        private final int port;
        
        public ServerWorker(int port) {
            this.port = port;
        }
        
        public void stop() {
            running = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close(); // This will cause accept() to throw SocketException
                } catch (IOException e) {
                    System.out.println("Error closing server socket: " + e.getMessage());
                }
            }
            Thread.currentThread().interrupt();
        }
        
        @Override
        public void run() {
            running = true;
            
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server listening on port " + port);
                
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        System.out.println("Waiting for client connection...");
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                        
                        // Handle client in a separate thread (simplified)
                        handleClient(clientSocket);
                        
                    } catch (SocketException e) {
                        if (running) {
                            System.out.println("Server socket was closed unexpectedly");
                        } else {
                            System.out.println("Server socket closed gracefully");
                        }
                        break;
                    } catch (IOException e) {
                        if (running) {
                            System.out.println("Error accepting client connection: " + e.getMessage());
                        }
                        break;
                    }
                }
                
            } catch (IOException e) {
                System.out.println("Error starting server: " + e.getMessage());
            } finally {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        System.out.println("Error closing server socket: " + e.getMessage());
                    }
                }
                System.out.println("ServerWorker stopped gracefully");
            }
        }
        
        private void handleClient(Socket clientSocket) {
            try {
                // Simple echo server functionality
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received: " + inputLine);
                    out.println("Echo: " + inputLine);
                    if ("bye".equalsIgnoreCase(inputLine)) {
                        break;
                    }
                }
                
                clientSocket.close();
                System.out.println("Client disconnected");
                
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Thread Interruption Example");
        
        // Example 1: Simple worker thread
        System.out.println("\n=== Example 1: Simple Worker Thread ===");
        SimpleWorker worker = new SimpleWorker();
        Thread workerThread = new Thread(worker, "SimpleWorker");
        workerThread.start();
        
        // Let it run for 3 seconds, then interrupt
        try {
            Thread.sleep(3000);
            System.out.println("Interrupting SimpleWorker...");
            workerThread.interrupt();
            workerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread was interrupted");
        }
        
        // Example 2: Server worker thread
        System.out.println("\n=== Example 2: Server Worker Thread ===");
        ServerWorker serverWorker = new ServerWorker(8080);
        Thread serverThread = new Thread(serverWorker, "ServerWorker");
        serverThread.start();
        
        // Let it run for 5 seconds, then stop
        try {
            Thread.sleep(5000);
            System.out.println("Stopping ServerWorker...");
            serverWorker.stop();
            serverThread.join();
        } catch (InterruptedException e) {
            System.out.println("Main thread was interrupted");
        }
        
        System.out.println("\nAll examples completed!");
    }
}