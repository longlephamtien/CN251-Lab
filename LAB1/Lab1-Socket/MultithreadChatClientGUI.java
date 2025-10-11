import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * MultithreadChatClientGUI - A comprehensive multi-threaded chat client with GUI
 * 
 * Threading Architecture:
 * - Main Thread: GUI management and user interaction
 * - Chat Window Thread: Manages chat interface and server connection
 * - Input Stream Handler Thread: Handles reading incoming messages from server
 */
public class MultithreadChatClientGUI extends JFrame {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 12345;
    
    // Main window components
    private JTextField hostField;
    private JTextField portField;
    private JTextField nameField;
    private JButton connectButton;
    private JLabel statusLabel;
    
    // Connection state
    private boolean isConnected = false;
    private ChatWindow chatWindow;
    
    public MultithreadChatClientGUI() {
        initializeMainGUI();
    }
    
    /**
     * Initialize the main connection GUI
     */
    private void initializeMainGUI() {
        setTitle("Multithreaded Chat Client - Connection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Center panel for connection form
        JPanel formPanel = createConnectionForm();
        add(formPanel, BorderLayout.CENTER);
        
        // Bottom panel for status
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
        
        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (chatWindow != null) {
                    chatWindow.disconnect();
                }
                System.exit(0);
            }
        });
    }
    
    /**
     * Create the connection form
     */
    private JPanel createConnectionForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Server Connection"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Host field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Server Host:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        hostField = new JTextField(DEFAULT_HOST, 15);
        panel.add(hostField, gbc);
        
        // Port field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Server Port:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        portField = new JTextField(String.valueOf(DEFAULT_PORT), 15);
        panel.add(portField, gbc);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Your Name:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        nameField = new JTextField("User" + System.currentTimeMillis() % 1000, 15);
        panel.add(nameField, gbc);
        
        // Connect button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        connectButton = new JButton("Connect to Server");
        connectButton.addActionListener(this::connectToServer);
        panel.add(connectButton, gbc);
        
        return panel;
    }
    
    /**
     * Create the status panel
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Ready to connect");
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel);
        return panel;
    }
    
    /**
     * Connect to the server
     */
    private void connectToServer(ActionEvent e) {
        if (isConnected) {
            return;
        }
        
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        String name = nameField.getText().trim();
        
        // Validate inputs
        if (host.isEmpty()) {
            showError("Please enter server host");
            return;
        }
        
        if (name.isEmpty()) {
            showError("Please enter your name");
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Port out of range");
            }
        } catch (NumberFormatException ex) {
            showError("Invalid port number");
            return;
        }
        
        // Update UI state
        connectButton.setEnabled(false);
        statusLabel.setText("Connecting...");
        statusLabel.setForeground(Color.ORANGE);
        
        // Create and start chat window thread
        chatWindow = new ChatWindow(host, port, name);
        new Thread(chatWindow, "ChatWindow").start();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Update connection status
     */
    private void updateConnectionStatus(boolean connected, String message) {
        SwingUtilities.invokeLater(() -> {
            isConnected = connected;
            statusLabel.setText(message);
            statusLabel.setForeground(connected ? Color.GREEN : Color.RED);
            connectButton.setEnabled(!connected);
            
            if (!connected) {
                setVisible(true);
                toFront();
            }
        });
    }
    
    /**
     * Chat Window Thread - Child Thread
     * Manages the chat interface and server connection
     */
    private class ChatWindow implements Runnable {
        private final String serverHost;
        private final int serverPort;
        private final String userName;
        
        // GUI Components
        private JFrame chatFrame;
        private JTextArea messageArea;
        private JTextField inputField;
        private JButton sendButton;
        private JButton disconnectButton;
        private JLabel connectionStatusLabel;
        
        // Network Components
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private boolean connected = false;
        private InputStreamHandler inputHandler;
        
        public ChatWindow(String host, int port, String name) {
            this.serverHost = host;
            this.serverPort = port;
            this.userName = name;
        }
        
        @Override
        public void run() {
            try {
                // Connect to server
                socket = new Socket(serverHost, serverPort);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                connected = true;
                
                // Initialize chat GUI
                SwingUtilities.invokeAndWait(this::initializeChatGUI);
                
                // Handle server handshake
                String serverMessage = in.readLine();
                if ("ENTER_NAME".equals(serverMessage)) {
                    out.println(userName);
                    serverMessage = in.readLine();
                    if ("WELCOME".equals(serverMessage)) {
                        updateConnectionStatus(true, "Connected to " + serverHost + ":" + serverPort);
                        appendMessage("*** Connected to chat server ***");
                        
                        // Start input stream handler - Grand-child Thread
                        inputHandler = new InputStreamHandler();
                        new Thread(inputHandler, "InputHandler-" + userName).start();
                        
                        // Keep this thread alive while connected
                        while (connected && !socket.isClosed()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    } else {
                        throw new IOException("Unexpected server response: " + serverMessage);
                    }
                } else {
                    throw new IOException("Server handshake failed");
                }
                
            } catch (Exception ex) {
                updateConnectionStatus(false, "Connection failed: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    if (chatFrame != null) {
                        chatFrame.dispose();
                    }
                });
            } finally {
                disconnect();
            }
        }
        
        /**
         * Initialize the chat GUI
         */
        private void initializeChatGUI() {
            chatFrame = new JFrame("Chat - " + userName + " @ " + serverHost + ":" + serverPort);
            chatFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            chatFrame.setSize(600, 500);
            chatFrame.setLocationRelativeTo(MultithreadChatClientGUI.this);
            chatFrame.setLayout(new BorderLayout());
            
            // Message display area
            messageArea = new JTextArea();
            messageArea.setEditable(false);
            messageArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            messageArea.setBackground(Color.WHITE);
            JScrollPane messageScroll = new JScrollPane(messageArea);
            messageScroll.setBorder(new TitledBorder("Chat Messages"));
            messageScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            chatFrame.add(messageScroll, BorderLayout.CENTER);
            
            // Input panel
            JPanel inputPanel = createInputPanel();
            chatFrame.add(inputPanel, BorderLayout.SOUTH);
            
            // Status panel
            JPanel statusPanel = createChatStatusPanel();
            chatFrame.add(statusPanel, BorderLayout.NORTH);
            
            // Window closing event
            chatFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    disconnect();
                }
            });
            
            // Show chat window and hide main window
            chatFrame.setVisible(true);
            MultithreadChatClientGUI.this.setVisible(false);
            
            // Focus on input field
            inputField.requestFocus();
        }
        
        /**
         * Create input panel for sending messages
         */
        private JPanel createInputPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new TitledBorder("Send Message"));
            
            inputField = new JTextField();
            inputField.addActionListener(this::sendMessage);
            panel.add(inputField, BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            sendButton = new JButton("Send");
            sendButton.addActionListener(this::sendMessage);
            buttonPanel.add(sendButton);
            
            disconnectButton = new JButton("Disconnect");
            disconnectButton.addActionListener(e -> disconnect());
            buttonPanel.add(disconnectButton);
            
            panel.add(buttonPanel, BorderLayout.EAST);
            
            return panel;
        }
        
        /**
         * Create chat status panel
         */
        private JPanel createChatStatusPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            connectionStatusLabel = new JLabel("Connecting...");
            connectionStatusLabel.setForeground(Color.ORANGE);
            panel.add(connectionStatusLabel);
            return panel;
        }
        
        /**
         * Send message to server
         */
        private void sendMessage(ActionEvent e) {
            String message = inputField.getText().trim();
            if (!message.isEmpty() && connected && out != null) {
                out.println(message);
                // Display the message in our own chat window
                appendMessage(userName + ": " + message);
                inputField.setText("");
                inputField.requestFocus();
            }
        }
        
        /**
         * Append message to chat area
         */
        private void appendMessage(String message) {
            SwingUtilities.invokeLater(() -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                messageArea.append("[" + timestamp + "] " + message + "\n");
                messageArea.setCaretPosition(messageArea.getDocument().getLength());
            });
        }
        
        /**
         * Update chat window status
         */
        private void updateChatStatus(String status, Color color) {
            SwingUtilities.invokeLater(() -> {
                if (connectionStatusLabel != null) {
                    connectionStatusLabel.setText(status);
                    connectionStatusLabel.setForeground(color);
                }
            });
        }
        
        /**
         * Disconnect from server
         */
        public void disconnect() {
            connected = false;
            
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignore
            }
            
            SwingUtilities.invokeLater(() -> {
                if (chatFrame != null) {
                    chatFrame.dispose();
                }
                updateConnectionStatus(false, "Disconnected");
            });
        }
        
        /**
         * Input Stream Handler - Grand-child Thread
         * Handles reading incoming messages from the server
         */
        private class InputStreamHandler implements Runnable {
            @Override
            public void run() {
                updateChatStatus("Connected", Color.GREEN);
                
                try {
                    String message;
                    while (connected && (message = in.readLine()) != null) {
                        appendMessage(message);
                    }
                } catch (IOException e) {
                    if (connected) {
                        appendMessage("*** Connection error: " + e.getMessage() + " ***");
                        updateChatStatus("Connection Lost", Color.RED);
                    }
                } finally {
                    if (connected) {
                        appendMessage("*** Disconnected from server ***");
                        updateChatStatus("Disconnected", Color.RED);
                    }
                    disconnect();
                }
            }
        }
    }
    
    /**
     * Main method to start the client
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
            
            new MultithreadChatClientGUI().setVisible(true);
        });
    }
}