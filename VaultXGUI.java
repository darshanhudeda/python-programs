import javax.swing.*;
import java.awt.*;
import java.io.*;

public class VaultXGUI {

    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        // Setup Main Window
        JFrame frame = new JFrame("Vault-X Security System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout(10, 10));

        // Main Container Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Key Input Area
        JPanel keyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        keyPanel.add(new JLabel("Secret Key (Number):"));
        JTextField keyField = new JTextField(15);
        keyPanel.add(keyField);
        mainPanel.add(keyPanel);

        // Message Input/Output Area
        mainPanel.add(new JLabel("Message / Decrypted Output:"));
        JTextArea messageArea = new JTextArea(10, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        mainPanel.add(scrollPane);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton encryptBtn = new JButton("Encrypt to File");
        JButton decryptBtn = new JButton("Decrypt from File");
        buttonPanel.add(encryptBtn);
        buttonPanel.add(decryptBtn);
        mainPanel.add(buttonPanel);

        // Status Label (replaces console outputs)
        JLabel statusLabel = new JLabel("Status: Ready", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        statusLabel.setForeground(Color.BLUE);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        // --- ENCRYPT ACTION ---
        encryptBtn.addActionListener(e -> {
            try {
                int key = Integer.parseInt(keyField.getText().trim());
                String message = messageArea.getText();

                if (message.isEmpty()) {
                    statusLabel.setText("Status: Message cannot be empty.");
                    statusLabel.setForeground(Color.RED);
                    return;
                }

                StringBuilder encrypted = new StringBuilder();
                for (int i = 0; i < message.length(); i++) {
                    encrypted.append((char) (message.charAt(i) ^ key));
                }

                try (PrintWriter writer = new PrintWriter(new FileWriter("hidden.txt"))) {
                    writer.print(encrypted.toString());
                    statusLabel.setText("Status: [SUCCESS] Encrypted content saved to hidden.txt");
                    statusLabel.setForeground(new Color(0, 150, 0)); // Green
                    messageArea.setText(""); // Clear area to hide the original message
                }

            } catch (NumberFormatException ex) {
                statusLabel.setText("Status: [ERROR] Secret Key must be a valid whole number.");
                statusLabel.setForeground(Color.RED);
            } catch (IOException ex) {
                statusLabel.setText("Status: [ERROR] Failed to write to file.");
                statusLabel.setForeground(Color.RED);
            }
        });

        // --- DECRYPT ACTION ---
        decryptBtn.addActionListener(e -> {
            try {
                int key = Integer.parseInt(keyField.getText().trim());
                File file = new File("hidden.txt");

                if (!file.exists()) {
                    statusLabel.setText("Status: [ERROR] No 'hidden.txt' found. Encrypt something first.");
                    statusLabel.setForeground(Color.RED);
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    // Read character by character because XOR can create newline characters
                    StringBuilder sb = new StringBuilder();
                    int c;
                    while ((c = reader.read()) != -1) {
                        sb.append((char) c);
                    }
                    String encryptedContent = sb.toString();

                    if (encryptedContent.isEmpty()) {
                        statusLabel.setText("Status: [ERROR] The file is empty.");
                        statusLabel.setForeground(Color.RED);
                    } else {
                        StringBuilder decrypted = new StringBuilder();
                        for (int i = 0; i < encryptedContent.length(); i++) {
                            decrypted.append((char) (encryptedContent.charAt(i) ^ key));
                        }
                        
                        messageArea.setText(decrypted.toString());
                        statusLabel.setText("Status: [SUCCESS] Message decrypted.");
                        statusLabel.setForeground(new Color(0, 150, 0)); // Green
                    }
                }

            } catch (NumberFormatException ex) {
                statusLabel.setText("Status: [ERROR] Secret Key must be a valid whole number.");
                statusLabel.setForeground(Color.RED);
            } catch (IOException ex) {
                statusLabel.setText("Status: [ERROR] Failed to read from file.");
                statusLabel.setForeground(Color.RED);
            }
        });

        // Show Window
        frame.setLocationRelativeTo(null); // Centers window on screen
        frame.setVisible(true);
    }
}