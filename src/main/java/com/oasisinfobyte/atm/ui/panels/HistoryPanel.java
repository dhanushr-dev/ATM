package com.oasisinfobyte.atm.ui.panels;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.ui.DashboardFrame;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Transaction History panel — full sortable table.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class HistoryPanel extends JPanel {

    private static final String[] COLS = {"#", "Date & Time", "Type", "Amount", "Balance After", "Reference"};

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    private DefaultTableModel tableModel;
    private JLabel            countLabel = new JLabel(" ");

    public HistoryPanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 4, 0));
        p.add(DepositPanel.buildPageHeader("📋", "Transaction History", "All your account transactions"), BorderLayout.CENTER);

        JButton exportBtn = ATMTheme.createSecondaryButton("  📊 Export CSV  ");
        exportBtn.addActionListener(e -> exportCSV());

        JButton refresh = ATMTheme.createPrimaryButton("  🔄 Refresh  ");
        refresh.addActionListener(e -> loadData());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(exportBtn);
        right.add(refresh);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? ATMTheme.BG_CARD : new Color(16, 30, 58));
                }
                return c;
            }
        };

        table.setFont(ATMTheme.FONT_BODY);
        table.setForeground(ATMTheme.TEXT_WHITE);
        table.setBackground(ATMTheme.BG_CARD);
        table.setRowHeight(36);
        table.setGridColor(ATMTheme.DIVIDER);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(0, 60, 120));
        table.setSelectionForeground(ATMTheme.ACCENT);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Header
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0, 40, 80));
        header.setForeground(ATMTheme.ACCENT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));

        // Column widths
        int[] widths = {40, 160, 130, 120, 140, 200};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(3).setCellRenderer(new AmountRenderer());
        table.setAutoCreateRowSorter(true);

        loadData();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(ATMTheme.BORDER_SUBTLE, 1));
        scroll.getViewport().setBackground(ATMTheme.BG_CARD);
        scroll.setBackground(ATMTheme.BG_DEEP);
        // Style scrollbar
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor     = ATMTheme.BORDER_SUBTLE;
                trackColor     = ATMTheme.BG_DEEP;
            }
        });
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        countLabel.setFont(ATMTheme.FONT_SMALL);
        countLabel.setForeground(ATMTheme.TEXT_DIM);
        p.add(countLabel);

        JButton back = ATMTheme.createSecondaryButton("  ← BACK  ");
        back.addActionListener(e -> dashboard.showPanel(DashboardFrame.CARD_HOME));
        p.add(back);
        return p;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Transaction> txns = controller.getTransactionHistory();
            int row = 1;
            for (Transaction t : txns) {
                tableModel.addRow(new Object[]{
                        row++,
                        t.getFormattedDate(),
                        t.getTransactionType().getDisplayName(),
                        t.getSignedAmountString(),
                        String.format("%,.2f", t.getBalanceAfter()),
                        t.getReferenceNumber()
                });
            }
            countLabel.setText(txns.size() + " transaction(s)");
        } catch (Exception ex) {
            countLabel.setText("Error: " + ex.getMessage());
            countLabel.setForeground(ATMTheme.RED);
        }
    }

    private void exportCSV() {
        try {
            List<Transaction> txns = controller.getTransactionHistory();
            if (txns.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transactions available to export.", "Export CSV", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("Transaction_History_" + controller.getCurrentAccount().getAccountNumber() + ".csv"));
            int choice = chooser.showSaveDialog(this);

            if (choice == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                    pw.println("Transaction_ID,Date_Time,Type,Amount,Balance_After,Description,Reference_Number");
                    for (Transaction t : txns) {
                        pw.printf("%d,\"%s\",\"%s\",%.2f,%.2f,\"%s\",\"%s\"%n",
                                t.getTransactionId(),
                                t.getFormattedDate(),
                                t.getTransactionType().getDisplayName(),
                                t.getAmount(),
                                t.getBalanceAfter(),
                                t.getDescription().replace("\"", "\"\""),
                                t.getReferenceNumber());
                    }
                    JOptionPane.showMessageDialog(this, "✓ Transaction history exported successfully to:\n" + file.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Colour-coded amount renderer ─────────────────────────────────────────

    private static class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            setHorizontalAlignment(RIGHT);
            setFont(new Font("Consolas", Font.BOLD, 12));
            if (val != null) {
                String s = val.toString();
                setForeground(s.startsWith("+") ? ATMTheme.GREEN
                           : s.startsWith("-") ? ATMTheme.RED
                           : ATMTheme.TEXT_MUTED);
            }
            return this;
        }
    }
}
