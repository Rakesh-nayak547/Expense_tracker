import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
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

    public ExpenseTracker() {
        initializeUI();
    }

    private void initializeUI() {
        // Main frame setup
        frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Main panel
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
        String[] columnNames = {"Category", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        // Summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        totalLabel = new JLabel("Total Expenses: ₹0.00");
        summaryPanel.add(totalLabel);

        // Adding components to the main panel
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(tableScrollPane, BorderLayout.SOUTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(summaryPanel, BorderLayout.SOUTH);

        // Add Expense Button Action
        addButton.addActionListener(e -> addExpense());

        // Clear Button Action
        clearButton.addActionListener(e -> clearExpenses());

        frame.setVisible(true);
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            String category = (String) categoryBox.getSelectedItem();

            // Add to table
            tableModel.addRow(new Object[]{category, "₹" + amount});

            // Update total and category summary
            totalExpenses += amount;
            categorySummary.put(category, categorySummary.getOrDefault(category, 0.0) + amount);

            updateSummary();
            amountField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearExpenses() {
        tableModel.setRowCount(0);
        totalExpenses = 0.0;
        categorySummary.clear();
        updateSummary();
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