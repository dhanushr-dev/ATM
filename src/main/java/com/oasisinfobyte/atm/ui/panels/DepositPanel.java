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
 * Deposit Money panel.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class DepositPanel extends JPanel {

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    private JTextField amountField;
    private JLabel     statusLabel;
    private JLabel     resultLabel;

    public DepositPanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(buildPageHeader("💰", "Deposit Money", "Add funds to your account"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel card = PanelUtil.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0; g.gridx = 0;
        g.insets = new Insets(0, 0, 6, 0);

        // Info row
        g.gridy = 0;
        JPanel info = PanelUtil.infoRow("Max single deposit: ₹1,00,000  •  Min: ₹1");
        card.add(info, g);

        // Amount label + field
        g.gridy = 1; g.insets = new Insets(14, 0, 5, 0);
        card.add(ATMTheme.createLabel("AMOUNT (₹)"), g);

        g.gridy = 2; g.insets = new Insets(0, 0, 8, 0);
        amountField = ATMTheme.createTextField();
        amountField.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountField.setPreferredSize(new Dimension(400, 46));
        card.add(amountField, g);

        // Quick amounts
        g.gridy = 3; g.insets = new Insets(0, 0, 16, 0);
        card.add(buildQuickAmounts(new String[]{"500","1000","2000","5000","10000","20000"}), g);

        // Status / result
        g.gridy = 4; g.insets = new Insets(0, 0, 4, 0);
        statusLabel = PanelUtil.statusLabel();
        card.add(statusLabel, g);

        g.gridy = 5;
        resultLabel = PanelUtil.resultLabel();
        card.add(resultLabel, g);

        // Buttons
        g.gridy = 6; g.insets = new Insets(16, 0, 0, 0);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);

        JButton depositBtn = ATMTheme.createPrimaryButton("  DEPOSIT  ");
        JButton clearBtn   = ATMTheme.createSecondaryButton("  CLEAR  ");
        JButton backBtn    = ATMTheme.createSecondaryButton("  ← BACK  ");

        depositBtn.addActionListener(e -> performDeposit());
        clearBtn.addActionListener(e   -> clearForm());
        backBtn.addActionListener(e    -> dashboard.showPanel(DashboardFrame.CARD_HOME));

        btns.add(depositBtn); btns.add(clearBtn); btns.add(backBtn);
        card.add(btns, g);

        return PanelUtil.centerCard(card);
    }

    private JPanel buildQuickAmounts(String[] amounts) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel lbl = ATMTheme.createLabel("Quick: ");
        p.add(lbl);
        for (String amt : amounts) {
            ATMTheme.RoundButton b = new ATMTheme.RoundButton("₹" + amt, ATMTheme.BG_INPUT, ATMTheme.ACCENT);
            b.setOutline(true);
            b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            b.setPreferredSize(new Dimension(72, 30));
            b.addActionListener(e -> amountField.setText(amt));
            p.add(b);
        }
        return p;
    }

    private void performDeposit() {
        String amt = amountField.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm deposit of  ₹" + amt + " ?",
                "Confirm Deposit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Transaction txn = controller.deposit(amt);
            resultLabel.setText("✓  Deposit successful!   New Balance: "
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

    // ── Shared header ─────────────────────────────────────────────────────────

    static JPanel buildPageHeader(String icon, String title, String sub) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel icn = new JLabel(icon);
        icn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        left.add(icn);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(ATMTheme.FONT_TITLE);
        t.setForeground(ATMTheme.TEXT_WHITE);
        textCol.add(t);

        JLabel s = new JLabel(sub);
        s.setFont(ATMTheme.FONT_SMALL);
        s.setForeground(ATMTheme.TEXT_DIM);
        textCol.add(s);

        left.add(textCol);
        p.add(left, BorderLayout.WEST);

        // Accent line below
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0,0, ATMTheme.ACCENT, getWidth(), 0,
                        new Color(0,198,255,0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), 2);
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(0, 2));
        p.add(line, BorderLayout.SOUTH);
        return p;
    }
}
