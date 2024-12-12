import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;

public class ChatClientGUI extends JFrame {
    private JTextPane chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private ClientImpl client;
    private String otherName;
    private StyledDocument chatDocument;

    public ChatClientGUI(ClientImpl client, String otherName) {
        this.client = client;
        this.otherName = otherName;

        // Set up modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configure frame
        setTitle("Chat with " + otherName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 249));

        // Chat header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(255, 255, 255));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel chatTitle = new JLabel(otherName);
        chatTitle.setFont(new Font("Inter", Font.BOLD, 18));
        chatTitle.setForeground(new Color(50, 50, 70));
        headerPanel.add(chatTitle, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Create the chat area with styled document
        chatArea = new JTextPane();
        chatDocument = chatArea.getStyledDocument();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Inter", Font.PLAIN, 14));
        chatArea.setBackground(Color.WHITE);

        // Create styles for different message types
        Style defaultStyle = chatArea.addStyle("default", null);
        StyleConstants.setFontFamily(defaultStyle, "Inter");
        StyleConstants.setFontSize(defaultStyle, 14);

        Style myMessageStyle = chatArea.addStyle("myMessage", defaultStyle);
        StyleConstants.setForeground(myMessageStyle, new Color(70, 130, 180));

        Style otherMessageStyle = chatArea.addStyle("otherMessage", defaultStyle);
        StyleConstants.setForeground(otherMessageStyle, new Color(80, 80, 80));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        scrollPane.setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Message input panel
        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        messagePanel.setOpaque(false);

        // Message input field
        messageField = new JTextField();
        messageField.setFont(new Font("Inter", Font.PLAIN, 14));
        messageField.putClientProperty("JComponent.roundRect", true);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Send button with gradient
        sendButton = new JButton("Send") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setPaint(new GradientPaint(
                        0, 0, new Color(100, 149, 237),
                        0, getHeight(), new Color(70, 130, 180)
                ));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        sendButton.setFont(new Font("Inter", Font.BOLD, 14));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setPreferredSize(new Dimension(100, 40));

        // Add components to message panel
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        add(messagePanel, BorderLayout.SOUTH);

        // Event handlers
        sendButton.addActionListener(this::sendMessage);
        messageField.addActionListener(this::sendMessage);

        // Center the window and make visible
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void sendMessage(ActionEvent e) {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                client.sendMessage(otherName, message);
                appendToChat("You", message, "myMessage");
                messageField.setText("");
            } catch (RemoteException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error sending message. Please try again.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        }
    }

    public void displayReceivedMessage(String message) {
        appendToChat(otherName, message, "otherMessage");
    }

    private void appendToChat(String sender, String message, String styleKey) {
        try {
            Style style = chatArea.getStyle(styleKey);
            chatDocument.insertString(
                    chatDocument.getLength(),
                    sender + ": " + message + "\n",
                    style
            );
            // Auto-scroll to bottom
            chatArea.setCaretPosition(chatDocument.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}