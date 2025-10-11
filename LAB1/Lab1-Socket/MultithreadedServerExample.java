import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MultithreadedServerExample demonstrates a complete multithreaded server
 * application with proper thread management, similar to the architecture
 * shown in your attached diagram.
 */
public class MultithreadedServerExample {
    
    private static final int PORT = 8081;
    private static final int MAX_THREADS = 10;
    
    /**
     * Main Server class that manages the server socket and thread pool
     */
    static class MultithreadedServer {
        private ServerSocket serverSocket;
        private ExecutorService threadPool;
        private volatile boolean running = false;
        private final AtomicInteger clientCounter = new AtomicInteger(0);
        
        public void start() throws IOException {
            serverSocket = new ServerSocket(PORT);
            threadPool = Executors.newFixedThreadPool(MAX_THREADS);
            running = true;
            
            System.out.println("Multithreaded Server started on port " + PORT);
            System.out.println("Thread pool size: " + MAX_THREADS);
            
            // Main server loop - listens for incoming connections
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientCounter.incrementAndGet();
                    
                    System.out.println("New client connected: " + 
                                     clientSocket.getRemoteSocketAddress() + 
                                     " (Client #" + clientId + ")");
                    
                    // Create a new ClientHandler for each connection
                    ClientHandler handler = new ClientHandler(clientSocket, clientId);
                    
                    // Submit the handler to the thread pool
                    threadPool.submit(handler);
                    
                } catch (SocketException e) {
                    if (running) {
                        System.out.println("Server socket error: " + e.getMessage());
                    } else {
                        System.out.println("Server socket closed gracefully");
                    }
                    break;
                } catch (IOException e) {
                    System.out.println("Error accepting client connection: " + e.getMessage());
                    break;
                }
            }
        }
        
        public void stop() {
            running = false;
            
            System.out.println("Stopping server...");
            
            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing server socket: " + e.getMessage());
                }
            }
            
            // Shutdown thread pool
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
                try {
                    // Wait up to 30 seconds for threads to finish
                    if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                        System.out.println("Thread pool did not terminate gracefully, forcing shutdown...");
                        threadPool.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted while waiting for thread pool termination");
                    threadPool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            
            System.out.println("Server stopped");
        }
    }
    
    /**
     * ClientHandler class - handles individual client connections
     * Each instance runs in its own thread from the thread pool
     */
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final int clientId;
        
        public ClientHandler(Socket socket, int clientId) {
            this.clientSocket = socket;
            this.clientId = clientId;
        }
        
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("Thread " + threadName + " handling Client #" + clientId);
            
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                // Send welcome message
                out.println("Welcome to Multithreaded Server! You are Client #" + clientId);
                out.println("Type 'help' for commands, 'quit' to disconnect");
                
                String inputLine;
                while ((inputLine = in.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                    System.out.println("Client #" + clientId + " (" + threadName + "): " + inputLine);
                    
                    // Process client commands
                    String response = processCommand(inputLine, clientId, threadName);
                    out.println(response);
                    
                    if ("quit".equalsIgnoreCase(inputLine.trim())) {
                        break;
                    }
                }
                
            } catch (IOException e) {
                System.out.println("Error handling Client #" + clientId + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Client #" + clientId + " disconnected from thread " + threadName);
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
        
        private String processCommand(String command, int clientId, String threadName) {
            command = command.trim().toLowerCase();
            
            switch (command) {
                case "help":
                    return "Available commands: help, time, thread, echo <message>, quit";
                    
                case "time":
                    return "Server time: " + new java.util.Date();
                    
                case "thread":
                    return "You are being handled by thread: " + threadName;
                    
                case "quit":
                    return "Goodbye Client #" + clientId + "!";
                    
                default:
                    if (command.startsWith("echo ")) {
                        return "Echo: " + command.substring(5);
                    } else {
                        return "Unknown command: " + command + ". Type 'help' for available commands.";
                    }
            }
        }
    }
    
    /**
     * Test client to demonstrate the server functionality
     */
    static class TestClient implements Runnable {
        private final int clientNumber;
        
        public TestClient(int clientNumber) {
            this.clientNumber = clientNumber;
        }
        
        @Override
        public void run() {
            try (Socket socket = new Socket("localhost", PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                System.out.println("Test Client " + clientNumber + " connected");
                
                // Read welcome messages
                String response = in.readLine();
                System.out.println("Client " + clientNumber + " received: " + response);
                response = in.readLine();
                System.out.println("Client " + clientNumber + " received: " + response);
                
                // Send some test commands
                String[] commands = {"help", "time", "thread", "echo Hello from client " + clientNumber, "quit"};
                
                for (String command : commands) {
                    out.println(command);
                    response = in.readLine();
                    System.out.println("Client " + clientNumber + " sent '" + command + "', received: " + response);
                    
                    // Add small delay between commands
                    Thread.sleep(500);
                }
                
                System.out.println("Test Client " + clientNumber + " finished");
                
            } catch (IOException | InterruptedException e) {
                System.out.println("Test Client " + clientNumber + " error: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Multithreaded Server Example ===");
        
        MultithreadedServer server = new MultithreadedServer();
        
        // Start server in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                System.out.println("Server error: " + e.getMessage());
            }
        }, "ServerThread");
        
        serverThread.start();
        
        // Wait a moment for server to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create and start test clients
        System.out.println("\nStarting test clients...");
        Thread[] clientThreads = new Thread[3];
        
        for (int i = 0; i < 3; i++) {
            TestClient client = new TestClient(i + 1);
            clientThreads[i] = new Thread(client, "TestClient-" + (i + 1));
            clientThreads[i].start();
            
            // Stagger client connections
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Wait for all test clients to finish
        for (Thread clientThread : clientThreads) {
            try {
                clientThread.join();
            } catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for client threads");
                Thread.currentThread().interrupt();
            }
        }
        
        // Stop the server after a short delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\nStopping server...");
        server.stop();
        
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            System.out.println("Interrupted while waiting for server thread");
        }
        
        System.out.println("Example completed!");
    }
}