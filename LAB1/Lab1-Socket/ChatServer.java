import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ChatServer - A multi-threaded chat server that handles multiple client connections
 * Uses ServerSocket to listen for incoming connections and broadcasts messages to all clients
 */
public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static ServerSocket serverSocket;
    
    public static void main(String[] args) {
        System.out.println("Chat Server starting on port " + PORT + "...");
        
        try {
            // Create server socket and listen for connections
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);
            
            // Accept client connections in a loop
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Create a new thread to handle this client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            closeServer();
        }
    }
    
    /**
     * Broadcast a message to all connected clients
     */
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    /**
     * Remove a client from the server
     */
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected. Active clients: " + clients.size());
    }
    
    /**
     * Close the server and all client connections
     */
    public static void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.closeConnection();
            }
            clients.clear();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
}

/**
 * ClientHandler - Handles individual client connections in separate threads
 */
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }
    
    @Override
    public void run() {
        try {
            // Set up input and output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            // Get client name (sent directly by GUI client)
            clientName = in.readLine();
            if (clientName == null || clientName.trim().isEmpty()) {
                clientName = "Anonymous";
            }
            
            // Notify other clients about new user
            ChatServer.broadcastMessage(clientName + " joined the chat!", this);
            out.println("Welcome to the chat, " + clientName + "!");
            
            // Listen for messages from this client
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("/quit")) {
                    break;
                }
                
                // Broadcast the message to all other clients
                String formattedMessage = clientName + ": " + message;
                System.out.println(formattedMessage);
                ChatServer.broadcastMessage(formattedMessage, this);
            }
        } catch (IOException e) {
            System.err.println("Error handling client " + clientName + ": " + e.getMessage());
        } finally {
            closeConnection();
            ChatServer.removeClient(this);
            ChatServer.broadcastMessage(clientName + " left the chat.", this);
        }
    }
    
    /**
     * Send a message to this client
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    /**
     * Close the connection to this client
     */
    public void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}