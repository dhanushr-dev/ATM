package com.oasisinfobyte.atm.ui;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Account Registration Dialog — Allows new users to open a bank account.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class RegisterDialog extends JDialog {

    private final ATMController controller;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField pinField;
    private JTextField depositField;
    private JComboBox<Account.AccountType> typeCombo;
    private JLabel statusLabel;
    private JButton createBtn;

    public RegisterDialog(Frame owner, ATMController controller) {
        super(owner, "SecureATM — Open New Account", true);
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setSize(440, 560);
        setResizable(false);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(ATMTheme.BG_DEEP);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JLabel headerLbl = new JLabel("🏦 Open New Bank Account", SwingConstants.CENTER);
        headerLbl.setFont(ATMTheme.FONT_TITLE);
        headerLbl.setForeground(ATMTheme.ACCENT);
        headerLbl.setBorder(new EmptyBorder(0, 0, 16, 0));
        root.add(headerLbl, BorderLayout.NORTH);

        // Form
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0; g.gridx = 0;
        g.insets = new Insets(4, 0, 4, 0);

        // Full Name
        g.gridy = 0; card.add(ATMTheme.createLabel("FULL NAME"), g);
        g.gridy = 1; nameField = ATMTheme.createTextField(); card.add(nameField, g);

        // Email
        g.gridy = 2; card.add(ATMTheme.createLabel("EMAIL ADDRESS"), g);
        g.gridy = 3; emailField = ATMTheme.createTextField(); card.add(emailField, g);

        // Phone
        g.gridy = 4; card.add(ATMTheme.createLabel("MOBILE PHONE (10 DIGITS)"), g);
        g.gridy = 5; phoneField = ATMTheme.createTextField(); card.add(phoneField, g);

        // Account Type & PIN Row
        g.gridy = 6;
        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);

        JPanel pType = new JPanel(new BorderLayout(0, 4));
        pType.setOpaque(false);
        pType.add(ATMTheme.createLabel("ACCOUNT TYPE"), BorderLayout.NORTH);
        typeCombo = new JComboBox<>(Account.AccountType.values());
        typeCombo.setFont(ATMTheme.FONT_BODY);
        typeCombo.setBackground(ATMTheme.BG_INPUT);
        typeCombo.setForeground(ATMTheme.TEXT_WHITE);
        typeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBackground(isSelected ? ATMTheme.BG_NAV_HOVER : ATMTheme.BG_INPUT);
                l.setForeground(isSelected ? ATMTheme.ACCENT : ATMTheme.TEXT_WHITE);
                l.setFont(ATMTheme.FONT_BODY);
                l.setBorder(new EmptyBorder(4, 8, 4, 8));
                return l;
            }
        });
        pType.add(typeCombo, BorderLayout.CENTER);

        JPanel pPin = new JPanel(new BorderLayout(0, 4));
        pPin.setOpaque(false);
        pPin.add(ATMTheme.createLabel("4-DIGIT PIN"), BorderLayout.NORTH);
        pinField = ATMTheme.createPasswordField();
        pPin.add(pinField, BorderLayout.CENTER);

        row1.add(pType); row1.add(pPin);
        card.add(row1, g);

        // Initial Deposit
        g.gridy = 7; g.insets = new Insets(8, 0, 4, 0);
        card.add(ATMTheme.createLabel("INITIAL DEPOSIT (MIN ₹500)"), g);
        g.gridy = 8;
        depositField = ATMTheme.createTextField();
        depositField.setText("1000");
        card.add(depositField, g);

        // Status Label
        g.gridy = 9; g.insets = new Insets(8, 0, 4, 0);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ATMTheme.FONT_SMALL);
        statusLabel.setForeground(ATMTheme.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel, g);

        // Buttons
        g.gridy = 10; g.insets = new Insets(12, 0, 0, 0);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton cancelBtn = ATMTheme.createSecondaryButton("CANCEL");
        cancelBtn.addActionListener(e -> dispose());

        createBtn = ATMTheme.createPrimaryButton("CREATE ACCOUNT");
        createBtn.addActionListener(e -> performRegister());

        btns.add(cancelBtn); btns.add(createBtn);
        card.add(btns, g);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void performRegister() {
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pin   = new String(pinField.getPassword()).trim();
        String depStr = depositField.getText().trim();
        Account.AccountType type = (Account.AccountType) typeCombo.getSelectedItem();

        statusLabel.setText(" ");

        try {
            BigDecimal deposit = new BigDecimal(depStr);
            Account account = controller.registerAccount(name, email, phone, pin, deposit, type);

            JOptionPane.showMessageDialog(this,
                    "<html><b>✓ Account Created Successfully!</b><br><br>" +
                    "Your New 16-Digit Account Number:<br>" +
                    "<font color='#00C6FF' size='5'><b>" + account.getAccountNumber() + "</b></font><br><br>" +
                    "Please save this account number and use your PIN to login.</html>",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (NumberFormatException nfe) {
            statusLabel.setText("Invalid deposit amount.");
        } catch (Exception ex) {
            statusLabel.setText(ex.getMessage());
        }
    }
}
