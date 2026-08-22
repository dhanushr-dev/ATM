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
 * Transfer Money panel.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class TransferPanel extends JPanel {

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    private JTextField destField;
    private JTextField amountField;
    private JLabel     statusLabel;
    private JLabel     resultLabel;

    public TransferPanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(DepositPanel.buildPageHeader("↔", "Transfer Money", "Send funds to another account"), BorderLayout.NORTH);
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
        card.add(PanelUtil.infoRow("Transfers are immediate and irreversible  •  Max: ₹50,000"), g);

        g.gridy = 1; g.insets = new Insets(16, 0, 5, 0);
        card.add(ATMTheme.createLabel("DESTINATION ACCOUNT NUMBER (16 DIGITS)"), g);

        g.gridy = 2; g.insets = new Insets(0, 0, 8, 0);
        destField = ATMTheme.createTextField();
        destField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        destField.setPreferredSize(new Dimension(400, 42));
        destField.setToolTipText("Enter 16-digit account number");
        card.add(destField, g);

        g.gridy = 3; g.insets = new Insets(14, 0, 5, 0);
        card.add(ATMTheme.createLabel("AMOUNT (₹)"), g);

        g.gridy = 4; g.insets = new Insets(0, 0, 8, 0);
        amountField = ATMTheme.createTextField();
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountField.setPreferredSize(new Dimension(400, 46));
        card.add(amountField, g);

        g.gridy = 5; g.insets = new Insets(0, 0, 4, 0);
        statusLabel = PanelUtil.statusLabel();
        card.add(statusLabel, g);

        g.gridy = 6;
        resultLabel = PanelUtil.resultLabel();
        card.add(resultLabel, g);

        g.gridy = 7; g.insets = new Insets(16, 0, 0, 0);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);

        JButton transferBtn = ATMTheme.createPrimaryButton("  TRANSFER  ");
        JButton clearBtn    = ATMTheme.createSecondaryButton("  CLEAR  ");
        JButton backBtn     = ATMTheme.createSecondaryButton("  ← BACK  ");

        transferBtn.addActionListener(e -> performTransfer());
        clearBtn.addActionListener(e    -> clearForm());
        backBtn.addActionListener(e     -> dashboard.showPanel(DashboardFrame.CARD_HOME));

        btns.add(transferBtn); btns.add(clearBtn); btns.add(backBtn);
        card.add(btns, g);

        return PanelUtil.centerCard(card);
    }

    private void performTransfer() {
        String dest = destField.getText().trim();
        String amt  = amountField.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>Transfer  <b>₹" + amt + "</b>  to account  <b>" + dest + "</b> ?<br>"
                + "<font color='orange'>This action cannot be undone.</font></html>",
                "Confirm Transfer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Transaction txn = controller.transfer(dest, amt);
            resultLabel.setText("✓  Transfer successful!   New Balance: "
                    + FormatUtil.formatCurrency(controller.getCurrentAccount().getBalance())
                    + "   Ref: " + txn.getReferenceNumber());
            resultLabel.setForeground(ATMTheme.GREEN);
            statusLabel.setText(" ");
            destField.setText("");
            amountField.setText("");
            dashboard.refreshBalanceDisplay();
        } catch (Exception ex) {
            statusLabel.setText("✗  " + ex.getMessage());
            statusLabel.setForeground(ATMTheme.RED);
            resultLabel.setText(" ");
        }
    }

    private void clearForm() {
        destField.setText("");
        amountField.setText("");
        statusLabel.setText(" ");
        resultLabel.setText(" ");
    }
}
