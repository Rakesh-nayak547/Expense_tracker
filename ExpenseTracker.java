import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ExpenseTracker {
    private JFrame frame;
    private JTextField amountField;
    private JComboBox<String> categoryBox;
    private DefaultTableModel tableModel;
    private JLabel totalLabel;

    private double totalExpenses = 0.0;
    private Map<String, Double> categorySummary = new HashMap<>();

    private Connection connection;

    public ExpenseTracker() {
        initializeDatabase();
        initializeUI();
        loadExpensesFromDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:expenses.db");
            Statement statement = connection.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "amount REAL, " +
                    "category TEXT, " +
                    "date TEXT, " +
                    "time TEXT)";
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Expense"));

        JLabel amountLabel = new JLabel("Amount:");
        amountField = new JTextField();

        JLabel categoryLabel = new JLabel("Category:");
        categoryBox = new JComboBox<>(new String[]{"Food", "Transport", "Shopping", "Utilities", "Others"});

        inputPanel.add(amountLabel);
        inputPanel.add(amountField);
        inputPanel.add(categoryLabel);
        inputPanel.add(categoryBox);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Expense");
        JButton clearButton = new JButton("Clear Expenses");

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);

        // Table for displaying expenses
        String[] columnNames = {"Date", "Time", "Category", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        totalLabel = new JLabel("Total Expenses: ₹0.00");
        summaryPanel.add(totalLabel);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(tableScrollPane, BorderLayout.SOUTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(summaryPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addExpense());
        clearButton.addActionListener(e -> clearExpenses());

        frame.setVisible(true);
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String category = (String) categoryBox.getSelectedItem();

            LocalDateTime now = LocalDateTime.now();
            String date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // Add to database
            String insertSQL = "INSERT INTO expenses (amount, category, date, time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                preparedStatement.setDouble(1, amount);
                preparedStatement.setString(2, category);
                preparedStatement.setString(3, date);
                preparedStatement.setString(4, time);
                preparedStatement.executeUpdate();
            }

            // Add to table
            tableModel.addRow(new Object[]{date, time, category, "₹" + amount});

            // Update total and category summary
            totalExpenses += amount;
            categorySummary.put(category, categorySummary.getOrDefault(category, 0.0) + amount);

            updateSummary();
            amountField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearExpenses() {
        try {
            String deleteSQL = "DELETE FROM expenses";
            try (Statement statement = connection.createStatement()) {
                statement.execute(deleteSQL);
            }
            tableModel.setRowCount(0);
            totalExpenses = 0.0;
            categorySummary.clear();
            updateSummary();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadExpensesFromDatabase() {
        try {
            String querySQL = "SELECT date, time, category, amount FROM expenses";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(querySQL)) {

                while (resultSet.next()) {
                    String date = resultSet.getString("date");
                    String time = resultSet.getString("time");
                    String category = resultSet.getString("category");
                    double amount = resultSet.getDouble("amount");

                    tableModel.addRow(new Object[]{date, time, category, "₹" + amount});

                    totalExpenses += amount;
                    categorySummary.put(category, categorySummary.getOrDefault(category, 0.0) + amount);
                }
                updateSummary();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummary() {
        StringBuilder summaryText = new StringBuilder("Total Expenses: ₹" + totalExpenses + "\n");
        for (Map.Entry<String, Double> entry : categorySummary.entrySet()) {
            summaryText.append(entry.getKey()).append(": ₹").append(entry.getValue()).append("\n");
        }
        totalLabel.setText("<html>" + summaryText.toString().replace("\n", "<br>") + "</html>");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTracker::new);
    }
}
