package com.oasisinfobyte.atm.ui.panels;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.ui.DashboardFrame;
import com.oasisinfobyte.atm.ui.LoginFrame;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;

/**
 * Change PIN panel.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class ChangePinPanel extends JPanel {

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    private JPasswordField currentField;
    private JPasswordField newField;
    private JPasswordField confirmField;
    private JLabel         statusLabel;

    public ChangePinPanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(DepositPanel.buildPageHeader("🔑", "Change PIN", "Update your 4-digit security PIN"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel card = PanelUtil.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0; g.gridx = 0;
        g.insets = new Insets(0, 0, 6, 0);

        // Security notice
        g.gridy = 0;
        JPanel warn = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        warn.setOpaque(false);
        warn.setBackground(new Color(100, 50, 0, 100));
        JLabel warnLbl = new JLabel("<html><i>⚠  Your PIN must be 4 digits. Never share it with anyone.</i></html>");
        warnLbl.setFont(ATMTheme.FONT_SMALL);
        warnLbl.setForeground(ATMTheme.AMBER);
        warn.add(warnLbl);
        card.add(warn, g);

        // Current PIN
        g.gridy = 1; g.insets = new Insets(16, 0, 5, 0);
        card.add(ATMTheme.createLabel("CURRENT PIN"), g);

        g.gridy = 2; g.insets = new Insets(0, 0, 8, 0);
        currentField = ATMTheme.createPasswordField();
        currentField.setDocument(new PinDocument());
        currentField.setPreferredSize(new Dimension(250, 42));
        card.add(createPinRow(currentField), g);

        // New PIN
        g.gridy = 3; g.insets = new Insets(12, 0, 5, 0);
        card.add(ATMTheme.createLabel("NEW PIN (4 DIGITS)"), g);

        g.gridy = 4; g.insets = new Insets(0, 0, 8, 0);
        newField = ATMTheme.createPasswordField();
        newField.setDocument(new PinDocument());
        newField.setPreferredSize(new Dimension(250, 42));
        card.add(createPinRow(newField), g);

        // Confirm PIN
        g.gridy = 5; g.insets = new Insets(12, 0, 5, 0);
        card.add(ATMTheme.createLabel("CONFIRM NEW PIN"), g);

        g.gridy = 6; g.insets = new Insets(0, 0, 12, 0);
        confirmField = ATMTheme.createPasswordField();
        confirmField.setDocument(new PinDocument());
        confirmField.setPreferredSize(new Dimension(250, 42));
        card.add(createPinRow(confirmField), g);

        // Status
        g.gridy = 7; g.insets = new Insets(0, 0, 6, 0);
        statusLabel = PanelUtil.statusLabel();
        card.add(statusLabel, g);

        // Buttons
        g.gridy = 8; g.insets = new Insets(16, 0, 0, 0);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);

        JButton changeBtn = ATMTheme.createPrimaryButton("  CHANGE PIN  ");
        JButton clearBtn  = ATMTheme.createSecondaryButton("  CLEAR  ");
        JButton backBtn   = ATMTheme.createSecondaryButton("  ← BACK  ");

        changeBtn.addActionListener(e -> performChange());
        clearBtn.addActionListener(e  -> clearForm());
        backBtn.addActionListener(e   -> dashboard.showPanel(DashboardFrame.CARD_HOME));

        btns.add(changeBtn); btns.add(clearBtn); btns.add(backBtn);
        card.add(btns, g);

        return PanelUtil.centerCard(card);
    }

    private void performChange() {
        String curr = new String(currentField.getPassword());
        String newP = new String(newField.getPassword());
        String conf = new String(confirmField.getPassword());

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to change your PIN?\n\nYou will be logged out after this change.",
                "Confirm PIN Change", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            controller.changePin(curr, newP, conf);
            JOptionPane.showMessageDialog(this,
                    "✓  Your PIN has been changed successfully.\n\nPlease login again with your new PIN.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            controller.logout();
            new LoginFrame(controller).setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();

        } catch (Exception ex) {
            statusLabel.setText("✗  " + ex.getMessage());
            statusLabel.setForeground(ATMTheme.RED);
        }
    }

    private void clearForm() {
        currentField.setText("");
        newField.setText("");
        confirmField.setText("");
        statusLabel.setText(" ");
    }

    private JPanel createPinRow(JPasswordField field) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        JButton btn = ATMTheme.createSecondaryButton("👁");
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setPreferredSize(new Dimension(44, 42));
        btn.addActionListener(e -> {
            if (field.getEchoChar() == (char) 0) {
                field.setEchoChar('•');
                btn.setText("👁");
            } else {
                field.setEchoChar((char) 0);
                btn.setText("🙈");
            }
        });
        row.add(field, BorderLayout.CENTER);
        row.add(btn, BorderLayout.EAST);
        return row;
    }

    // ── PIN Document (4 digits max, numeric only) ────────────────────────────

    private static class PinDocument extends PlainDocument {
        @Override
        public void insertString(int off, String str, AttributeSet a) throws BadLocationException {
            if (str == null) return;
            String digits = str.replaceAll("[^0-9]", "");
            if ((getLength() + digits.length()) <= 4) super.insertString(off, digits, a);
        }
    }
}
