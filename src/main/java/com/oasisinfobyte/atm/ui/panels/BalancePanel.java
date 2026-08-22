package com.oasisinfobyte.atm.ui.panels;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.User;
import com.oasisinfobyte.atm.ui.DashboardFrame;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;
import com.oasisinfobyte.atm.utility.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Balance Inquiry panel.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class BalancePanel extends JPanel {

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    private JLabel balanceLabel;
    private JLabel timeLabel;

    public BalancePanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(DepositPanel.buildPageHeader("💳", "Balance Inquiry", "Real-time account balance"), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.weightx = 1; gc.weighty = 1;
        wrap.add(buildCard(), gc);
        return wrap;
    }

    private JPanel buildCard() {
        JPanel card = PanelUtil.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0; g.gridx = 0;
        g.insets = new Insets(6, 0, 6, 0);

        Account acc  = controller.getCurrentAccount();
        User    user = controller.getCurrentUser();

        // ── Big balance display ──
        g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);
        JLabel balTitle = new JLabel("AVAILABLE BALANCE");
        balTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        balTitle.setForeground(ATMTheme.TEXT_DIM);
        card.add(balTitle, g);

        g.gridy = 1; g.insets = new Insets(0, 0, 20, 0);
        balanceLabel = new JLabel(FormatUtil.formatCurrency(acc.getBalance()));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        balanceLabel.setForeground(ATMTheme.ACCENT);
        card.add(balanceLabel, g);

        // ── Divider ──
        g.gridy = 2; g.insets = new Insets(0, 0, 16, 0);
        card.add(PanelUtil.divider(), g);

        // ── Info grid ──
        g.gridy = 3; g.insets = new Insets(6, 0, 6, 0);
        card.add(buildInfoGrid(acc, user), g);

        // ── Timestamp ──
        g.gridy = 4; g.insets = new Insets(16, 0, 4, 0);
        timeLabel = new JLabel("Checked: " + now());
        timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        timeLabel.setForeground(ATMTheme.TEXT_DIM);
        card.add(timeLabel, g);

        // ── Buttons ──
        g.gridy = 5; g.insets = new Insets(16, 0, 0, 0);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);

        JButton refreshBtn = ATMTheme.createPrimaryButton("  🔄 REFRESH  ");
        JButton backBtn    = ATMTheme.createSecondaryButton("  ← BACK  ");

        refreshBtn.addActionListener(e -> refresh());
        backBtn.addActionListener(e    -> dashboard.showPanel(DashboardFrame.CARD_HOME));

        btns.add(refreshBtn); btns.add(backBtn);
        card.add(btns, g);

        return card;
    }

    private JPanel buildInfoGrid(Account acc, User user) {
        JPanel p = new JPanel(new GridLayout(2, 2, 20, 10));
        p.setOpaque(false);

        addInfoCell(p, "Account Holder", user != null ? user.getFullName() : "N/A");
        addInfoCell(p, "Account Number", FormatUtil.maskAccountNumber(acc.getAccountNumber()));
        addInfoCell(p, "Account Type",   acc.getAccountType().name() + " Account");
        addInfoCell(p, "Status",         acc.getStatus().name());
        return p;
    }

    private void addInfoCell(JPanel parent, String label, String value) {
        JPanel cell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 30, 65, 140));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            }
        };
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);
        cell.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lbl.setForeground(ATMTheme.TEXT_DIM);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        cell.add(lbl);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        val.setForeground("ACTIVE".equals(value) ? ATMTheme.GREEN : ATMTheme.TEXT_WHITE);
        val.setAlignmentX(LEFT_ALIGNMENT);
        cell.add(val);

        parent.add(cell);
    }

    private void refresh() {
        try {
            BigDecimal balance = controller.getBalance();
            balanceLabel.setText(FormatUtil.formatCurrency(balance));
            timeLabel.setText("Checked: " + now());
            dashboard.refreshBalanceDisplay();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy  HH:mm:ss"));
    }
}
