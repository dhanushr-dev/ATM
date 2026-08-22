package com.oasisinfobyte.atm.ui.panels;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.model.User;
import com.oasisinfobyte.atm.ui.DashboardFrame;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;
import com.oasisinfobyte.atm.utility.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Mini Statement panel — last 5 transactions in receipt style.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class MiniStatementPanel extends JPanel {

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    private JPanel receiptPanel;

    public MiniStatementPanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 12));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(DepositPanel.buildPageHeader("📄", "Mini Statement", "Last 5 transactions"), BorderLayout.CENTER);

        JButton exportBtn = ATMTheme.createSecondaryButton("  💾 Save Receipt  ");
        exportBtn.addActionListener(e -> exportReceipt());

        JButton refresh = ATMTheme.createPrimaryButton("  🔄 Refresh  ");
        refresh.addActionListener(e -> loadStatement());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(exportBtn);
        right.add(refresh);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildBody() {
        receiptPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ATMTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(ATMTheme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 16, 16));
            }
        };
        receiptPanel.setLayout(new BoxLayout(receiptPanel, BoxLayout.Y_AXIS));
        receiptPanel.setOpaque(false);
        receiptPanel.setBorder(new EmptyBorder(20, 28, 20, 28));

        loadStatement();

        JScrollPane scroll = new JScrollPane(receiptPanel);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = ATMTheme.BORDER_SUBTLE;
                trackColor = ATMTheme.BG_DEEP;
            }
        });
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        JButton back = ATMTheme.createSecondaryButton("  ← BACK  ");
        back.addActionListener(e -> dashboard.showPanel(DashboardFrame.CARD_HOME));
        p.add(back);
        return p;
    }

    private void loadStatement() {
        receiptPanel.removeAll();

        Account acc  = controller.getCurrentAccount();
        User    user = controller.getCurrentUser();

        // ── Receipt header ──
        addCenteredLabel("══════════════════════════════", ATMTheme.DIVIDER, ATMTheme.FONT_MONO);
        addCenteredLabel("S E C U R E   A T M", ATMTheme.ACCENT, new Font("Consolas", Font.BOLD, 14));
        addCenteredLabel("MINI ACCOUNT STATEMENT", ATMTheme.TEXT_WHITE, new Font("Consolas", Font.BOLD, 12));
        addCenteredLabel("══════════════════════════════", ATMTheme.DIVIDER, ATMTheme.FONT_MONO);
        addSpacer(4);

        addReceiptRow("Name     :", user != null ? user.getFullName() : "N/A");
        addReceiptRow("Account  :", FormatUtil.maskAccountNumber(acc.getAccountNumber()));
        addReceiptRow("Type     :", acc.getAccountType().name() + " Account");
        addReceiptRow("Date     :", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        addSpacer(4);
        addCenteredLabel("──────────────────────────────", ATMTheme.DIVIDER, ATMTheme.FONT_MONO);
        addSpacer(2);

        // ── Column header ──
        addMonoRow("Date        Type           Amount       Balance", ATMTheme.ACCENT, true);
        addCenteredLabel("──────────────────────────────", ATMTheme.DIVIDER, ATMTheme.FONT_MONO);

        // ── Transactions ──
        try {
            List<Transaction> txns = controller.getMiniStatement();
            if (txns.isEmpty()) {
                addSpacer(8);
                addCenteredLabel("No transactions found.", ATMTheme.TEXT_MUTED, ATMTheme.FONT_MONO);
                addSpacer(8);
            } else {
                for (Transaction txn : txns) {
                    String date = txn.getFormattedDate().length() >= 11
                            ? txn.getFormattedDate().substring(0, 11) : txn.getFormattedDate();
                    String type = txn.getTransactionType().getDisplayName();
                    if (type.length() > 12) type = type.substring(0, 12);
                    String line = String.format("%-12s %-14s %+10.2f  %12.2f",
                            date, type,
                            txn.getTransactionType().getSign().equals("+")
                                    ? txn.getAmount() : txn.getAmount().negate(),
                            txn.getBalanceAfter());
                    Color col = txn.getSignedAmountString().startsWith("+")
                            ? ATMTheme.GREEN : ATMTheme.RED;
                    addMonoRow(line, col, false);
                }
            }
        } catch (Exception ex) {
            addMonoRow("Error: " + ex.getMessage(), ATMTheme.RED, false);
        }

        addCenteredLabel("──────────────────────────────", ATMTheme.DIVIDER, ATMTheme.FONT_MONO);
        addSpacer(6);

        // ── Balance ──
        JPanel balRow = new JPanel(new BorderLayout());
        balRow.setOpaque(false);
        balRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel balTitle = new JLabel("CURRENT BALANCE:");
        balTitle.setFont(new Font("Consolas", Font.BOLD, 13));
        balTitle.setForeground(ATMTheme.TEXT_WHITE);
        balRow.add(balTitle, BorderLayout.WEST);

        JLabel balAmt = new JLabel(FormatUtil.formatCurrency(acc.getBalance()));
        balAmt.setFont(new Font("Consolas", Font.BOLD, 14));
        balAmt.setForeground(ATMTheme.ACCENT);
        balRow.add(balAmt, BorderLayout.EAST);
        receiptPanel.add(balRow);

        addSpacer(6);
        addCenteredLabel("══════════════════════════════", ATMTheme.DIVIDER, ATMTheme.FONT_MONO);
        addCenteredLabel("Thank you for banking with SecureATM", ATMTheme.TEXT_DIM, ATMTheme.FONT_SMALL);
        addCenteredLabel("Oasis Infobyte Banking System", ATMTheme.TEXT_DIM, ATMTheme.FONT_SMALL);
        addCenteredLabel("══════════════════════════════", ATMTheme.DIVIDER, ATMTheme.FONT_MONO);

        receiptPanel.revalidate();
        receiptPanel.repaint();
    }

    private void addCenteredLabel(String text, Color color, Font font) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        receiptPanel.add(l);
    }

    private void addReceiptRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel k = new JLabel(key);
        k.setFont(ATMTheme.FONT_MONO);
        k.setForeground(ATMTheme.TEXT_MUTED);
        row.add(k, BorderLayout.WEST);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Consolas", Font.BOLD, 12));
        v.setForeground(ATMTheme.TEXT_WHITE);
        row.add(v, BorderLayout.EAST);
        receiptPanel.add(row);
    }

    private void addMonoRow(String text, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(bold ? new Font("Consolas", Font.BOLD, 12) : ATMTheme.FONT_MONO);
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        receiptPanel.add(l);
    }

    private void addSpacer(int height) {
        receiptPanel.add(Box.createVerticalStrut(height));
    }

    private void exportReceipt() {
        Account acc  = controller.getCurrentAccount();
        User    user = controller.getCurrentUser();
        List<Transaction> txns = controller.getMiniStatement();

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("                 S E C U R E   A T M                 \n");
        sb.append("                MINI ACCOUNT STATEMENT               \n");
        sb.append("====================================================\n");
        sb.append("Name     : ").append(user != null ? user.getFullName() : "N/A").append("\n");
        sb.append("Account  : ").append(FormatUtil.maskAccountNumber(acc.getAccountNumber())).append("\n");
        sb.append("Type     : ").append(acc.getAccountType().name()).append(" Account\n");
        sb.append("Date     : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"))).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-12s %-14s %12s %12s\n", "Date", "Type", "Amount (Rs)", "Balance (Rs)"));
        sb.append("----------------------------------------------------\n");
        for (Transaction txn : txns) {
            String date = txn.getFormattedDate().length() >= 11 ? txn.getFormattedDate().substring(0, 11) : txn.getFormattedDate();
            String type = txn.getTransactionType().getDisplayName();
            if (type.length() > 12) type = type.substring(0, 12);
            sb.append(String.format("%-12s %-14s %+12.2f %12.2f\n",
                    date, type,
                    txn.getTransactionType().getSign().equals("+") ? txn.getAmount() : txn.getAmount().negate(),
                    txn.getBalanceAfter()));
        }
        sb.append("----------------------------------------------------\n");
        sb.append("CURRENT BALANCE: ").append(FormatUtil.formatCurrency(acc.getBalance())).append("\n");
        sb.append("====================================================\n");
        sb.append("         Thank you for banking with SecureATM       \n");
        sb.append("====================================================\n");

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("ATM_Receipt_" + acc.getAccountNumber() + ".txt"));
        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = chooser.getSelectedFile();
            try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                writer.write(sb.toString());
                JOptionPane.showMessageDialog(this,
                        "✓ Receipt saved successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Receipt Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save receipt: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
