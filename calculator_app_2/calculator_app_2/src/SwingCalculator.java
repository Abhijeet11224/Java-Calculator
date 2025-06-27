import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SwingCalculator extends JFrame implements ActionListener {
    private JTextField display;
    private String currentInput = "";
    private String operator = "";
    private double num1, num2, result;

    // SQLite DB URL
    private static final String DB_URL = "jdbc:sqlite:calculator.db";

    public static void main(String[] args) {
        try {
            // Force creation of the database file
            Connection conn = DriverManager.getConnection("jdbc:sqlite:calculator.db");
            System.out.println("✅ SQLite DB created or already exists at: " + System.getProperty("user.dir"));
            conn.close();  // Close it after testing
        } catch (SQLException e) {
            System.out.println("❌ Could not create DB!");
            e.printStackTrace();
        }
    
        
        new SwingCalculator();
    }

    // Constructor (with updated UI)
    public SwingCalculator() {
        createDatabaseTable();

        setTitle("🧮 Calculator with History");
        setSize(350, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top display
        display = new JTextField();
        display.setFont(new Font("Courier New", Font.PLAIN, 26));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setBackground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(display, BorderLayout.NORTH);

        // Buttons
        JPanel panel = new JPanel(new GridLayout(5, 4, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] buttons = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "C", "0", "=", "+",
            "History"
        };

        for (String text : buttons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(240, 240, 240));
            btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            btn.addActionListener(this);
            panel.add(btn);
        }

        add(panel, BorderLayout.CENTER);

        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    // Handle button clicks
    @Override
public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();

    // Digits
    if (command.matches("[0-9]")) {
        currentInput += command;
        display.setText((operator.isEmpty() ? "" : num1 + " " + operator + " ") + currentInput);
    }

    // Operators
    else if (command.matches("[+\\-*/]")) {
        if (!currentInput.isEmpty()) {
            num1 = Double.parseDouble(currentInput);
            operator = command;
            currentInput = "";
            display.setText(num1 + " " + operator + " ");
        }
    }

    // Equals
    else if (command.equals("=")) {
        if (!currentInput.isEmpty() && !operator.isEmpty()) {
            num2 = Double.parseDouble(currentInput);
            switch (operator) {
                case "+": result = num1 + num2; break;
                case "-": result = num1 - num2; break;
                case "*": result = num1 * num2; break;
                case "/": result = (num2 != 0) ? num1 / num2 : 0; break;
            }

            String expression = num1 + " " + operator + " " + num2;
            display.setText(expression + " = " + result);
            currentInput = String.valueOf(result);  // for next op
            operator = "";

            saveCalculation(expression, result);
        }
    }

    // Clear
    else if (command.equals("C")) {
        currentInput = "";
        num1 = num2 = result = 0;
        operator = "";
        display.setText("");
    }

    // History
    else if (command.equals("History")) {
        showHistory();
    }
}

    // Save to DB
    private void saveCalculation(String expression, double result) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO calculations (expression, result) VALUES (?, ?)")) {
            pstmt.setString(1, expression);
            pstmt.setString(2, String.valueOf(result));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show history
    private void showHistory() {
        JFrame historyFrame = new JFrame("Calculation History");
        historyFrame.setSize(400, 300);
        historyFrame.setLayout(new BorderLayout());

        JTextArea historyArea = new JTextArea();
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        historyArea.setEditable(false);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM calculations ORDER BY timestamp DESC")) {

            StringBuilder history = new StringBuilder();
            while (rs.next()) {
                history.append(rs.getString("timestamp"))
                       .append(" -> ")
                       .append(rs.getString("expression"))
                       .append(" = ")
                       .append(rs.getString("result"))
                       .append("\n");
            }
            historyArea.setText(history.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        historyFrame.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        historyFrame.setVisible(true);
    }

    // Create DB table
    private void createDatabaseTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS calculations (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "expression TEXT, " +
                         "result TEXT, " +
                         "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
