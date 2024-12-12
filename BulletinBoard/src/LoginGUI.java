import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

public class LoginGUI extends JFrame {
    private JTextField clientField;
    private JTextField otherClientField;
    private JButton loginButton;

    private String clientName;
    private String otherClientName;

    public LoginGUI() {
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
        setTitle("Connect");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        // Use a modern, clean layout
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(245, 245, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title label
        JLabel titleLabel = new JLabel("Connect to Chat", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 70));
        gbc.weighty = 0.1;
        add(titleLabel, gbc);

        // Username field
        gbc.weighty = 0;
        JLabel clientLabel = new JLabel("Your Username");
        clientLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        add(clientLabel, gbc);

        clientField = new JTextField(20);
        clientField.setFont(new Font("Inter", Font.PLAIN, 16));
        clientField.putClientProperty("JComponent.roundRect", true);
        add(clientField, gbc);

        // Receiver field
        JLabel otherClientLabel = new JLabel("Receiver's Username");
        otherClientLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        add(otherClientLabel, gbc);

        otherClientField = new JTextField(20);
        otherClientField.setFont(new Font("Inter", Font.PLAIN, 16));
        otherClientField.putClientProperty("JComponent.roundRect", true);
        add(otherClientField, gbc);

        // Login button
        loginButton = new JButton("Start Conversation") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                g2.setPaint(new GradientPaint(
                        0, 0, new Color(100, 149, 237),
                        0, getHeight(), new Color(70, 130, 180)
                ));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        loginButton.setFont(new Font("Inter", Font.BOLD, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setContentAreaFilled(false);

        gbc.weighty = 0.3;
        loginButton.addActionListener(this::login);
        add(loginButton, gbc);

        pack();
        setVisible(true);
    }

    private void login(ActionEvent e) {
        clientName = clientField.getText();
        otherClientName = otherClientField.getText();

        if (clientName.isEmpty() || otherClientName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both usernames",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        dispose();
    }

    public String getClientName() {
        return clientName;
    }

    public String getOtherClientName() {
        return otherClientName;
    }

    public boolean isReady() {
        return clientName != null && otherClientName != null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginGUI::new);
    }
}