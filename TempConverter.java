import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TempConverter {
    public static void main(String[] args) {
        // 1. Create the frame (the window)
        JFrame frame = new JFrame("Celsius to Fahrenheit");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 150);

        // 2. Create components
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter Celsius:");
        JTextField textField = new JTextField(10);
        JButton button = new JButton("Convert");
        JLabel resultLabel = new JLabel("Result: --");

        // 3. Add logic to the button
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get text, convert to double
                    double celsius = Double.parseDouble(textField.getText());
                    // Formula: F = (C * 9/5) + 32
                    double fahrenheit = (celsius * 9 / 5) + 32;
                    resultLabel.setText(String.format("Result: %.2f °F", fahrenheit));
                } catch (NumberFormatException ex) {
                    resultLabel.setText("Please enter a valid number!");
                }
            }
        });

        // 4. Add components to the panel and frame
        panel.add(label);
        panel.add(textField);
        panel.add(button);
        panel.add(resultLabel);

        frame.add(panel);

        // 5. Center and show
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}