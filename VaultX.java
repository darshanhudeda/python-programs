import java.util.Scanner;
import java.io.*;

public class VaultX {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- VAULT-X SECURITY SYSTEM ---");
        
        try {
            System.out.print("Choose: (1) Encrypt New Message (2) Decrypt from hidden.txt: ");
            int mode = scanner.nextInt();

            System.out.print("Enter Secret Key (Number): ");
            int key = scanner.nextInt();
            scanner.nextLine(); 

            if (mode == 1) {
            
                System.out.print("Enter Message: ");
                String message = scanner.nextLine();

                StringBuilder encrypted = new StringBuilder();
                for (int i = 0; i < message.length(); i++) {
                    // Bitwise XOR logic
                    encrypted.append((char) (message.charAt(i) ^ key));
                }

                PrintWriter writer = new PrintWriter(new FileWriter("hidden.txt"));
                writer.print(encrypted.toString());
                writer.close();
                System.out.println("[SUCCESS] Encrypted content saved to hidden.txt");

            } else {
                
                File file = new File("hidden.txt");
                if (!file.exists()) {
                    System.out.println("[ERROR] No 'hidden.txt' found. Encrypt something first!");
                } else {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String encryptedContent = reader.readLine();
                    reader.close();

                    // Check if file is empty
                    if (encryptedContent == null) {
                        System.out.println("[ERROR] The file is empty.");
                    } else {
                        StringBuilder decrypted = new StringBuilder();
                        for (int i = 0; i < encryptedContent.length(); i++) {
                            decrypted.append((char) (encryptedContent.charAt(i) ^ key));
                        }
                        System.out.println("\n[!] DECRYPTED MESSAGE: " + decrypted.toString());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("System Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}