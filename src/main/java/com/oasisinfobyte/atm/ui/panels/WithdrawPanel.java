package com.oasisinfobyte.atm.ui.panels;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.ui.DashboardFrame;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;
import com.oasisinfobyte.atm.utility.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Withdraw Money panel.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class WithdrawPanel extends JPanel {

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    private JTextField amountField;
    private JLabel     statusLabel;
    private JLabel     resultLabel;

    public WithdrawPanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(DepositPanel.buildPageHeader("💸", "Withdraw Money", "Withdraw cash from your account"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel card = PanelUtil.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0; g.gridx = 0;
        g.insets = new Insets(0, 0, 6, 0);

        g.gridy = 0;
        card.add(PanelUtil.infoRow("Max per transaction: ₹50,000  •  Min: ₹1"), g);

        g.gridy = 1; g.insets = new Insets(14, 0, 5, 0);
        card.add(ATMTheme.createLabel("AMOUNT (₹)"), g);

        g.gridy = 2; g.insets = new Insets(0, 0, 8, 0);
        amountField = ATMTheme.createTextField();
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountField.setPreferredSize(new Dimension(400, 46));
        card.add(amountField, g);

        g.gridy = 3; g.insets = new Insets(0, 0, 16, 0);
        card.add(buildQuickAmounts(), g);

        g.gridy = 4; g.insets = new Insets(0, 0, 4, 0);
        statusLabel = PanelUtil.statusLabel();
        card.add(statusLabel, g);

        g.gridy = 5;
        resultLabel = PanelUtil.resultLabel();
        card.add(resultLabel, g);

        g.gridy = 6; g.insets = new Insets(16, 0, 0, 0);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);

        JButton withdrawBtn = ATMTheme.createPrimaryButton("  WITHDRAW  ");
        JButton clearBtn    = ATMTheme.createSecondaryButton("  CLEAR  ");
        JButton backBtn     = ATMTheme.createSecondaryButton("  ← BACK  ");

        withdrawBtn.addActionListener(e -> performWithdraw());
        clearBtn.addActionListener(e    -> clearForm());
        backBtn.addActionListener(e     -> dashboard.showPanel(DashboardFrame.CARD_HOME));

        btns.add(withdrawBtn); btns.add(clearBtn); btns.add(backBtn);
        card.add(btns, g);

        return PanelUtil.centerCard(card);
    }

    private JPanel buildQuickAmounts() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.add(ATMTheme.createLabel("Quick: "));
        for (String amt : new String[]{"500","1000","2000","5000","10000","20000","50000"}) {
            ATMTheme.RoundButton b = new ATMTheme.RoundButton("₹" + amt, ATMTheme.BG_INPUT, ATMTheme.ACCENT);
            b.setOutline(true);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            b.setPreferredSize(new Dimension(72, 30));
            b.addActionListener(e -> amountField.setText(amt));
            p.add(b);
        }
        return p;
    }

    private void performWithdraw() {
        String amt = amountField.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm withdrawal of  ₹" + amt + " ?\n\nPlease collect your cash.",
                "Confirm Withdrawal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Transaction txn = controller.withdraw(amt);
            resultLabel.setText("✓  Withdrawal successful!   New Balance: "
                    + FormatUtil.formatCurrency(controller.getCurrentAccount().getBalance())
                    + "   Ref: " + txn.getReferenceNumber());
            resultLabel.setForeground(ATMTheme.GREEN);
            statusLabel.setText(" ");
            amountField.setText("");
            dashboard.refreshBalanceDisplay();
        } catch (Exception ex) {
            statusLabel.setText("✗  " + ex.getMessage());
            statusLabel.setForeground(ATMTheme.RED);
            resultLabel.setText(" ");
        }
    }

    private void clearForm() {
        amountField.setText("");
        statusLabel.setText(" ");
        resultLabel.setText(" ");
    }
}
