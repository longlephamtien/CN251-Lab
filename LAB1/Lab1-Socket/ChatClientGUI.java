import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * ChatClientGUI - A graphical user interface for the chat client
 * Provides a user-friendly interface with message display, input field, and connection controls
 */
public class ChatClientGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    // GUI Components
    private JTextArea messageArea;
    private JTextField inputField;
    private JTextField nameField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton sendButton;
    private JLabel statusLabel;
    
    // Network Components
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;
    
    public ChatClientGUI() {
        initializeGUI();
    }
    
    /**
     * Initialize the graphical user interface
     */
    private void initializeGUI() {
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Create top panel for connection controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Create center panel for message display
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Create bottom panel for message input
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Set initial button states
        updateButtonStates();
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                System.exit(0);
            }
        });
    }
    
    /**
     * Create the top panel with connection controls
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new EmptyBorder(10, 10, 5, 10));
        
        panel.add(new JLabel("Name:"));
        nameField = new JTextField(15);
        panel.add(nameField);
        
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connect());
        panel.add(connectButton);
        
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> disconnect());
        panel.add(disconnectButton);
        
        statusLabel = new JLabel("Not connected");
        statusLabel.setForeground(Color.RED);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(statusLabel);
        
        return panel;
    }
    
    /**
     * Create the center panel with message display area
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        messageArea.setBackground(Color.WHITE);
        messageArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Chat Messages"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create the bottom panel with message input controls
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 10, 10, 10));
        
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        
        panel.add(new JLabel("Message:"), BorderLayout.WEST);
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Connect to the chat server
     */
    private void connect() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Create socket and connect to server
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            isConnected = true;
            updateButtonStates();
            statusLabel.setText("Connected to " + SERVER_HOST + ":" + SERVER_PORT);
            statusLabel.setForeground(Color.GREEN);
            
            appendMessage("Connected to chat server!");
            
            // Send name to server
            out.println(name);
            
            // Start listening for messages
            Thread messageListener = new Thread(this::listenForMessages);
            messageListener.setDaemon(true);
            messageListener.start();
            
            inputField.requestFocus();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to server: " + e.getMessage(), 
                                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            appendMessage("Failed to connect to server: " + e.getMessage());
        }
    }
    
    /**
     * Disconnect from the chat server
     */
    private void disconnect() {
        if (isConnected) {
            try {
                if (out != null) {
                    out.println("/quit");
                    out.close();
                }
                if (in != null) in.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                appendMessage("Error disconnecting: " + e.getMessage());
            }
            
            isConnected = false;
            updateButtonStates();
            statusLabel.setText("Disconnected");
            statusLabel.setForeground(Color.RED);
            appendMessage("Disconnected from server.");
        }
    }
    
    /**
     * Send a message to the server
     */
    private void sendMessage() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "Not connected to server!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // Display the message in our own GUI
            String clientName = nameField.getText().trim();
            appendMessage(clientName + ": " + message);
            
            // Send the message to the server
            out.println(message);
            inputField.setText("");
            inputField.requestFocus();
        }
    }
    
    /**
     * Listen for messages from the server (runs in separate thread)
     */
    private void listenForMessages() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                final String msg = message;
                SwingUtilities.invokeLater(() -> appendMessage(msg));
            }
        } catch (IOException e) {
            if (isConnected) {
                SwingUtilities.invokeLater(() -> {
                    appendMessage("Connection lost: " + e.getMessage());
                    disconnect();
                });
            }
        }
    }
    
    /**
     * Append a message to the message area
     */
    private void appendMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }
    
    /**
     * Update button states based on connection status
     */
    private void updateButtonStates() {
        connectButton.setEnabled(!isConnected);
        disconnectButton.setEnabled(isConnected);
        sendButton.setEnabled(isConnected);
        inputField.setEnabled(isConnected);
        nameField.setEnabled(!isConnected);
    }
    
    /**
     * Main method to start the GUI chat client
     */
    public static void main(String[] args) {
        // Set system look and feel if available
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);
        });
    }
}