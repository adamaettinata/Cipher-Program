import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CipherProgram extends JFrame {
    private JTextArea inputArea, outputArea;
    private JTextField keyField;
    private JComboBox<String> cipherSelect;
    private JButton encryptButton, decryptButton, loadFileButton;
    
    public CipherProgram() {
        initializeUI();
        setupListeners();
    }
    
    private void initializeUI() {
        setTitle("Cipher Program");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        inputArea = new JTextArea(10, 30);
        outputArea = new JTextArea(10, 30);
        keyField = new JTextField(20);
        cipherSelect = new JComboBox<>(new String[]{"Vigenere Cipher", "Playfair Cipher", "Hill Cipher"});
        encryptButton = new JButton("Encrypt");
        decryptButton = new JButton("Decrypt");
        loadFileButton = new JButton("Unggah File");
        
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Pilih Cipher: "));
        topPanel.add(cipherSelect);
        topPanel.add(new JLabel("Kunci: "));
        topPanel.add(keyField);
        return topPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(new JScrollPane(inputArea));
        centerPanel.add(new JScrollPane(outputArea));
        return centerPanel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(loadFileButton);
        bottomPanel.add(encryptButton);
        bottomPanel.add(decryptButton);
        return bottomPanel;
    }
    
    private void setupListeners() {
        loadFileButton.addActionListener(e -> loadFile());
        encryptButton.addActionListener(e -> performCipher(true));
        decryptButton.addActionListener(e -> performCipher(false));
    }
    
    private void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                inputArea.read(reader, null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "File tidak dapat dibaca", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void performCipher(boolean encrypt) {
        String input = inputArea.getText();
        String key = keyField.getText();
        String selectedCipher = (String) cipherSelect.getSelectedItem();
        
        if (key.length() < 12) {
            JOptionPane.showMessageDialog(this, "Kunci minimal 12 karakter!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String result = "";
        switch (selectedCipher) {
            case "Vigenere Cipher":
                result = vigenereCipher(input, key, encrypt);
                break;
            case "Playfair Cipher":
                result = playfairCipher(input, key, encrypt);
                break;
            case "Hill Cipher":
                result = hillCipher(input, key, encrypt);
                break;
        }
        outputArea.setText(result);
    }
    
    private String vigenereCipher(String text, String key, boolean encrypt) {
        StringBuilder result = new StringBuilder();
        text = text.toUpperCase();
        key = key.toUpperCase();
        int keyLength = key.length();
        int textLength = text.length();
        
        for (int i = 0, j = 0; i < textLength; i++) {
            char c = text.charAt(i);
            if (c < 'A' || c > 'Z') {
                result.append(c);
            } else {
                int shift = key.charAt(j % keyLength) - 'A';
                char encryptedChar;
                if (encrypt) {
                    encryptedChar = (char) ((c - 'A' + shift) % 26 + 'A');
                } else {
                    encryptedChar = (char) ((c - 'A' - shift + 26) % 26 + 'A');
                }
                result.append(encryptedChar);
                j++;
            }
        }
        return result.toString();
    }
    
    private String playfairCipher(String text, String key, boolean encrypt) {
        text = text.replaceAll("[^A-Za-z]", "").toUpperCase();
        key = key.replaceAll("[^A-Za-z]", "").toUpperCase();
        char[][] matrix = generatePlayfairMatrix(key);
        List<String> digraphs = prepareDigraphs(text);
        StringBuilder result = new StringBuilder();
        
        for (String digraph : digraphs) {
            char a = digraph.charAt(0);
            char b = digraph.charAt(1);
            int[] posA = findPosition(matrix, a);
            int[] posB = findPosition(matrix, b);
            
            if (posA[0] == posB[0]) { 
                result.append(matrix[posA[0]][(posA[1] + (encrypt ? 1 : 4)) % 5]);
                result.append(matrix[posB[0]][(posB[1] + (encrypt ? 1 : 4)) % 5]);
            } else if (posA[1] == posB[1]) { 
                result.append(matrix[(posA[0] + (encrypt ? 1 : 4)) % 5][posA[1]]);
                result.append(matrix[(posB[0] + (encrypt ? 1 : 4)) % 5][posB[1]]);
            } else { 
                result.append(matrix[posA[0]][posB[1]]);
                result.append(matrix[posB[0]][posA[1]]);
            }
        }
        return result.toString();
    }
    
    private char[][] generatePlayfairMatrix(String key) {
        char[][] matrix = new char[5][5];
        Set<Character> used = new HashSet<>();
        int row = 0, col = 0;
        
        for (char c : (key + "ABCDEFGHIKLMNOPQRSTUVWXYZ").toCharArray()) {
            if (c == 'J') c = 'I'; 
            if (!used.contains(c)) {
                matrix[row][col] = c;
                used.add(c);
                if (++col == 5) {
                    col = 0;
                    if (++row == 5) break;
                }
            }
        }
        return matrix;
    }
    
    private List<String> prepareDigraphs(String text) {
        List<String> digraphs = new ArrayList<>();
        for (int i = 0; i < text.length(); i += 2) {
            if (i == text.length() - 1) {
                digraphs.add(text.substring(i) + "X");
            } else if (text.charAt(i) == text.charAt(i + 1)) {
                digraphs.add(text.substring(i, i + 1) + "X");
                i--;
            } else {
                digraphs.add(text.substring(i, i + 2));
            }
        }
        return digraphs;
    }
    
    private int[] findPosition(char[][] matrix, char c) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (matrix[i][j] == c) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1};
    }
    
    private String hillCipher(String text, String key, boolean encrypt) {
        text = text.replaceAll("[^A-Za-z]", "").toUpperCase();
        key = key.replaceAll("[^A-Za-z]", "").toUpperCase();
        int keySize = (int) Math.sqrt(key.length());
        
        if (keySize * keySize != key.length()) {
            JOptionPane.showMessageDialog(this, "Panjang kunci harus merupakan kuadrat sempurna!", "Error", JOptionPane.ERROR_MESSAGE);
            return "";
        }
        
        int[][] keyMatrix = new int[keySize][keySize];
        int keyIndex = 0;
        for (int i = 0; i < keySize; i++) {
            for (int j = 0; j < keySize; j++) {
                keyMatrix[i][j] = key.charAt(keyIndex++) - 'A';
            }
        }
        
        while (text.length() % keySize != 0) {
            text += "X";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i += keySize) {
            int[] vector = new int[keySize];
            for (int j = 0; j < keySize; j++) {
                vector[j] = text.charAt(i + j) - 'A';
            }
            int[] encrypted = new int[keySize];
            for (int row = 0; row < keySize; row++) {
                for (int col = 0; col < keySize; col++) {
                    encrypted[row] += keyMatrix[row][col] * vector[col];
                }
                encrypted[row] %= 26;
            }
            for (int value : encrypted) {
                result.append((char) (value + 'A'));
            }
        }
        return result.toString();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CipherProgram().setVisible(true));
    }
}