import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * MultithreadChatServer - A comprehensive multi-threaded chat server with GUI monitoring
 * 
 * Threading Architecture:
 * - Main Thread: GUI management and server control
 * - Connection Listener Thread: Accepts incoming client connections
 * - Connection Handler Threads: One per client for message handling
 * - Input Stream Handler Threads: One per client for reading incoming messages
 */
public class MultithreadChatServer extends JFrame {
    private static final int DEFAULT_PORT = 12345;
    private static final int MAX_CLIENTS = 50;
    
    // GUI Components
    private JTextArea logArea;
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;
    
    // Server Components
    private ServerSocket serverSocket;
    private boolean isServerRunning = false;
    private ConnectionListener connectionListener;
    private final Set<ClientConnectionHandler> connectedClients;
    private int currentPort;
    
    public MultithreadChatServer() {
        connectedClients = ConcurrentHashMap.newKeySet();
        initializeGUI();
    }
    
    /**
     * Initialize the server monitoring GUI
     */
    private void initializeGUI() {
        setTitle("Multithreaded Chat Server - Control Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Top panel for server controls
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Center panel with log area and client list
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel for status
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
                System.exit(0);
            }
        });
    }
    
    /**
     * Create the control panel with start/stop buttons
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("Server Control"));
        
        panel.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(DEFAULT_PORT), 8);
        panel.add(portField);
        
        startButton = new JButton("Start Server");
        startButton.addActionListener(e -> startServer());
        panel.add(startButton);
        
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopServer());
        panel.add(stopButton);
        
        return panel;
    }
    
    /**
     * Create the center panel with log and client list
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(new TitledBorder("Server Log"));
        
        // Client list
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setPreferredSize(new Dimension(200, 0));
        JScrollPane clientScroll = new JScrollPane(clientList);
        clientScroll.setBorder(new TitledBorder("Connected Clients"));
        
        panel.add(logScroll, BorderLayout.CENTER);
        panel.add(clientScroll, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Create the status panel
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        statusLabel = new JLabel("Server Stopped");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, BorderLayout.WEST);
        
        clientCountLabel = new JLabel("Clients: 0");
        panel.add(clientCountLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Start the server
     */
    private void startServer() {
        try {
            currentPort = Integer.parseInt(portField.getText().trim());
            if (currentPort < 1 || currentPort > 65535) {
                throw new NumberFormatException("Port out of range");
            }
            
            serverSocket = new ServerSocket(currentPort);
            isServerRunning = true;
            
            // Start the connection listener thread
            connectionListener = new ConnectionListener();
            new Thread(connectionListener, "ConnectionListener").start();
            
            // Update GUI
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);
            statusLabel.setText("Server Running on port " + currentPort);
            statusLabel.setForeground(Color.GREEN);
            
            logMessage("Server started on port " + currentPort);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to start server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            logMessage("Failed to start server: " + e.getMessage());
        }
    }
    
    /**
     * Stop the server
     */
    private void stopServer() {
        isServerRunning = false;
        
        try {
            // Close all client connections
            for (ClientConnectionHandler client : connectedClients) {
                client.closeConnection();
            }
            connectedClients.clear();
            
            // Close server socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Update GUI
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            portField.setEnabled(true);
            statusLabel.setText("Server Stopped");
            statusLabel.setForeground(Color.RED);
            clientListModel.clear();
            updateClientCount();
            
            logMessage("Server stopped");
            
        } catch (IOException e) {
            logMessage("Error stopping server: " + e.getMessage());
        }
    }
    
    /**
     * Log a message with timestamp
     */
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Update client count display
     */
    private void updateClientCount() {
        SwingUtilities.invokeLater(() -> {
            clientCountLabel.setText("Clients: " + connectedClients.size());
        });
    }
    
    /**
     * Add client to the list
     */
    private void addClientToList(String clientInfo) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.addElement(clientInfo);
            updateClientCount();
        });
    }
    
    /**
     * Remove client from the list
     */
    private void removeClientFromList(String clientInfo) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.removeElement(clientInfo);
            updateClientCount();
        });
    }
    
    /**
     * Broadcast message to all connected clients
     */
    private void broadcastMessage(String message, ClientConnectionHandler sender) {
        for (ClientConnectionHandler client : connectedClients) {
            if (client != sender && client.isConnected()) {
                client.sendMessage(message);
            }
        }
        logMessage("Broadcast: " + message);
    }
    
    /**
     * Connection Listener Thread - Child Thread
     * Continuously listens for incoming client connections
     */
    private class ConnectionListener implements Runnable {
        @Override
        public void run() {
            logMessage("Connection listener started");
            
            while (isServerRunning && !serverSocket.isClosed()) {
                try {
                    // Accept new client connection
                    Socket clientSocket = serverSocket.accept();
                    
                    if (connectedClients.size() >= MAX_CLIENTS) {
                        logMessage("Maximum clients reached, rejecting connection from: " + 
                                 clientSocket.getInetAddress());
                        clientSocket.close();
                        continue;
                    }
                    
                    // Create handler for this client - Grand-child Thread
                    ClientConnectionHandler clientHandler = new ClientConnectionHandler(clientSocket);
                    connectedClients.add(clientHandler);
                    
                    // Start the client handler thread
                    new Thread(clientHandler, "ClientHandler-" + clientSocket.getInetAddress()).start();
                    
                    logMessage("New client connected: " + clientSocket.getInetAddress() + 
                             ":" + clientSocket.getPort());
                    
                } catch (IOException e) {
                    if (isServerRunning) {
                        logMessage("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
            
            logMessage("Connection listener stopped");
        }
    }
    
    /**
     * Client Connection Handler - Grand-child Thread
     * Handles individual client connections and creates input stream handler
     */
    private class ClientConnectionHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;
        private String clientInfo;
        private boolean connected = true;
        private boolean disconnectNotified = false;
        private InputStreamHandler inputHandler;
        
        public ClientConnectionHandler(Socket socket) {
            this.clientSocket = socket;
            this.clientInfo = socket.getInetAddress() + ":" + socket.getPort();
        }
        
        @Override
        public void run() {
            try {
                // Initialize streams
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                // Get client name
                out.println("ENTER_NAME");
                clientName = in.readLine();
                if (clientName == null || clientName.trim().isEmpty()) {
                    clientName = "Anonymous-" + clientSocket.getPort();
                }
                
                clientInfo = clientName + " (" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + ")";
                addClientToList(clientInfo);
                
                // Send welcome message
                out.println("WELCOME");
                
                // Notify other clients
                broadcastMessage("*** " + clientName + " joined the chat ***", this);
                
                // Start input stream handler - Grand-grand-child Thread
                inputHandler = new InputStreamHandler();
                new Thread(inputHandler, "InputHandler-" + clientName).start();
                
                // Keep this thread alive while client is connected
                while (connected && !clientSocket.isClosed()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
            } catch (IOException e) {
                if (connected) {
                    logMessage("Client handler error for " + clientInfo + ": " + e.getMessage());
                }
            } finally {
                closeConnection();
            }
        }
        
        /**
         * Send message to this client
         */
        public void sendMessage(String message) {
            if (out != null && connected) {
                out.println(message);
            }
        }
        
        /**
         * Check if client is connected
         */
        public boolean isConnected() {
            return connected && !clientSocket.isClosed();
        }
        
        /**
         * Close the connection
         */
        public void closeConnection() {
            connected = false;
            
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            
            // Remove from connected clients
            connectedClients.remove(this);
            removeClientFromList(clientInfo);
            
            // Notify other clients (only once)
            if (clientName != null && !disconnectNotified) {
                disconnectNotified = true;
                broadcastMessage("*** " + clientName + " left the chat ***", this);
                logMessage("Client disconnected: " + clientInfo);
            }
        }
        
        /**
         * Input Stream Handler - Grand-grand-child Thread
         * Handles reading from the client's input stream
         */
        private class InputStreamHandler implements Runnable {
            @Override
            public void run() {
                try {
                    String message;
                    while (connected && (message = in.readLine()) != null) {
                        if (message.trim().isEmpty()) {
                            continue;
                        }
                        
                        // Format and broadcast the message
                        String formattedMessage = clientName + ": " + message;
                        broadcastMessage(formattedMessage, ClientConnectionHandler.this);
                    }
                } catch (IOException e) {
                    if (connected) {
                        logMessage("Input stream error for " + clientInfo + ": " + e.getMessage());
                    }
                } finally {
                    closeConnection();
                }
            }
        }
    }
    
    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Use system look and feel if available
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // Use default look and feel
            }
            
            new MultithreadChatServer().setVisible(true);
        });
    }
}