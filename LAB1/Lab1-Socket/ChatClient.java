import java.io.*;
import java.net.*;

/**
 * ChatClient - A simple console-based chat client that connects to the ChatServer
 * Uses Socket to establish TCP connection and exchange messages
 */
public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader consoleInput;
    
    /**
     * Connect to the chat server
     */
    public boolean connect() {
        try {
            // Create socket and connect to server
            System.out.println("Connecting to server at " + SERVER_HOST + ":" + SERVER_PORT + "...");
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            
            // Set up input and output streams
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            consoleInput = new BufferedReader(new InputStreamReader(System.in));
            
            System.out.println("Connected to server!");
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Start the chat session
     */
    public void startChat() {
        try {
            // Start a thread to listen for messages from server
            Thread messageListener = new Thread(this::listenForMessages);
            messageListener.start();
            
            // Read user input and send to server
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                out.println(userInput);
                
                // Exit if user types /quit
                if (userInput.equalsIgnoreCase("/quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error during chat: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    /**
     * Listen for messages from the server (runs in separate thread)
     */
    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                System.err.println("Error receiving messages: " + e.getMessage());
            }
        }
    }
    
    /**
     * Disconnect from the server
     */
    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (consoleInput != null) consoleInput.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    /**
     * Main method to start the chat client
     */
    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        
        if (client.connect()) {
            System.out.println("Type '/quit' to exit the chat.");
            client.startChat();
        } else {
            System.out.println("Failed to connect to chat server.");
        }
    }
}