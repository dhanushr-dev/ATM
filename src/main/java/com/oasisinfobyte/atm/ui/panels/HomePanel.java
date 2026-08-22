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
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Home panel — welcome card + quick-action grid.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class HomePanel extends JPanel {

    private final ATMController  controller;
    private final DashboardFrame dashboard;

    public HomePanel(ATMController controller, DashboardFrame dashboard) {
        this.controller = controller;
        this.dashboard  = dashboard;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ATMTheme.BG_DEEP);
        setBorder(new EmptyBorder(28, 28, 28, 28));
        add(buildTopRow(),      BorderLayout.NORTH);
        add(buildQuickGrid(),   BorderLayout.CENTER);
    }

    private JPanel buildTopRow() {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        row.add(buildWelcomeCard(), BorderLayout.CENTER);
        row.add(buildAccountCard(), BorderLayout.EAST);
        return row;
    }

    private JPanel buildWelcomeCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        Account acc  = controller.getCurrentAccount();
        User    user = controller.getCurrentUser();
        String  name = user != null ? user.getFullName() : "Valued Customer";

        JLabel greet = new JLabel("Welcome back,");
        greet.setFont(ATMTheme.FONT_BODY);
        greet.setForeground(ATMTheme.TEXT_MUTED);
        greet.setAlignmentX(LEFT_ALIGNMENT);
        card.add(greet);

        JLabel nameLabel = new JLabel(name + " 👋");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        nameLabel.setForeground(ATMTheme.TEXT_WHITE);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(nameLabel);

        card.add(Box.createVerticalStrut(16));

        JLabel balTitle = new JLabel("Available Balance");
        balTitle.setFont(ATMTheme.FONT_SMALL);
        balTitle.setForeground(ATMTheme.TEXT_DIM);
        balTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(balTitle);

        JLabel balAmt = new JLabel(FormatUtil.formatCurrency(acc.getBalance()));
        balAmt.setFont(ATMTheme.FONT_AMOUNT);
        balAmt.setForeground(ATMTheme.ACCENT);
        balAmt.setAlignmentX(LEFT_ALIGNMENT);
        card.add(balAmt);

        card.add(Box.createVerticalStrut(10));

        JLabel dateLabel = new JLabel(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        dateLabel.setFont(ATMTheme.FONT_SMALL);
        dateLabel.setForeground(ATMTheme.TEXT_DIM);
        dateLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(dateLabel);
        return card;
    }

    private JPanel buildAccountCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(200, 0));

        Account acc = controller.getCurrentAccount();

        addInfo(card, "Account No.",  FormatUtil.maskAccountNumber(acc.getAccountNumber()));
        card.add(Box.createVerticalStrut(12));
        addInfo(card, "Account Type", acc.getAccountType().name());
        card.add(Box.createVerticalStrut(12));
        addInfo(card, "Status",       "● " + acc.getStatus().name());

        if (acc.getLastLogin() != null) {
            card.add(Box.createVerticalStrut(12));
            addInfo(card, "Last Login",
                    acc.getLastLogin().format(DateTimeFormatter.ofPattern("dd MMM HH:mm")));
        }
        return card;
    }

    private void addInfo(JPanel parent, String title, String value) {
        JLabel t = new JLabel(title.toUpperCase());
        t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        t.setForeground(ATMTheme.TEXT_DIM);
        t.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(t);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(value.startsWith("●") ? ATMTheme.GREEN : ATMTheme.TEXT_WHITE);
        v.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(v);
    }

    private JPanel buildQuickGrid() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JLabel sectionTitle = new JLabel("Quick Actions");
        sectionTitle.setFont(ATMTheme.FONT_SUBTITLE);
        sectionTitle.setForeground(ATMTheme.TEXT_MUTED);
        sectionTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
        grid.setOpaque(false);

        Object[][] actions = {
            {"💰", "Deposit",          ATMTheme.GREEN,       DashboardFrame.CARD_DEPOSIT},
            {"💸", "Withdraw",         ATMTheme.RED,         DashboardFrame.CARD_WITHDRAW},
            {"↔",  "Transfer",         ATMTheme.ACCENT,      DashboardFrame.CARD_TRANSFER},
            {"💳", "Balance",          new Color(180,100,255), DashboardFrame.CARD_BALANCE},
            {"📋", "History",          ATMTheme.AMBER,       DashboardFrame.CARD_HISTORY},
            {"📄", "Mini Stmt",        new Color(100,200,150), DashboardFrame.CARD_MINI},
            {"🔑", "Change PIN",       new Color(255,150,50), DashboardFrame.CARD_CHANGE_PIN},
            {"🔒", "Logout",           ATMTheme.TEXT_MUTED,  null},
        };

        for (Object[] a : actions) {
            grid.add(buildActionCard(
                    (String) a[0], (String) a[1], (Color) a[2], (String) a[3]));
        }
        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildActionCard(String icon, String label, Color accent, String card) {
        JPanel p = new JPanel(new GridBagLayout()) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (card != null) dashboard.showPanel(card);
                        else {
                            int c = JOptionPane.showConfirmDialog(HomePanel.this,
                                    "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
                            if (c == JOptionPane.YES_OPTION) {
                                controller.logout();
                                new com.oasisinfobyte.atm.ui.LoginFrame(controller).setVisible(true);
                                SwingUtilities.getWindowAncestor(HomePanel.this).dispose();
                            }
                        }
                    }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hovered ? new Color(
                        Math.min(ATMTheme.BG_CARD.getRed()   + 15, 255),
                        Math.min(ATMTheme.BG_CARD.getGreen() + 15, 255),
                        Math.min(ATMTheme.BG_CARD.getBlue()  + 25, 255))
                        : ATMTheme.BG_CARD;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                // Bottom accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, getHeight() - 3, getWidth(), 3, 3, 3);
                // Border
                g2.setColor(hovered ? accent : ATMTheme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(hovered ? 1.5f : 0.8f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 12, 12));
            }
        };
        p.setOpaque(false);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel icnLbl = new JLabel(icon);
        icnLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        p.add(icnLbl, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(6, 0, 0, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(ATMTheme.TEXT_WHITE);
        p.add(lbl, gbc);
        return p;
    }

    private JPanel createCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ATMTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(ATMTheme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 14, 14));
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));
        return p;
    }
}
