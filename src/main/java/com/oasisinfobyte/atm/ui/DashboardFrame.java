package com.oasisinfobyte.atm.ui;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.User;
import com.oasisinfobyte.atm.ui.panels.*;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;
import com.oasisinfobyte.atm.utility.FormatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main dashboard — sidebar navigation + CardLayout content area.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class DashboardFrame extends JFrame {

    public static final String CARD_HOME       = "HOME";
    public static final String CARD_DEPOSIT    = "DEPOSIT";
    public static final String CARD_WITHDRAW   = "WITHDRAW";
    public static final String CARD_TRANSFER   = "TRANSFER";
    public static final String CARD_BALANCE    = "BALANCE";
    public static final String CARD_HISTORY    = "HISTORY";
    public static final String CARD_MINI       = "MINI";
    public static final String CARD_CHANGE_PIN = "CHANGE_PIN";

    private final ATMController controller;

    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JLabel     sideBalanceLabel;
    private JLabel     sideAccLabel;
    private JLabel     sideNameLabel;
    private JLabel     sideTimeLabel;
    private JButton    activeNavBtn;

    // Nav button background tracking
    private static final Color NAV_NORMAL = ATMTheme.BG_SIDEBAR;
    private static final Color NAV_ACTIVE = ATMTheme.BG_NAV_HOVER;

    public DashboardFrame(ATMController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("SecureATM — Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(980, 660);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(ATMTheme.BG_DEEP);
        root.add(buildSidebar(),     BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
        setContentPane(root);

        // Start live clock in sidebar
        startClock();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ATMTheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));

        // Right border line
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ATMTheme.DIVIDER));

        sidebar.add(buildSidebarHeader(),  BorderLayout.NORTH);
        sidebar.add(buildSidebarNav(),     BorderLayout.CENTER);
        sidebar.add(buildSidebarFooter(),  BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildSidebarHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(ATMTheme.BG_SIDEBAR);
        p.setBorder(new EmptyBorder(20, 16, 16, 16));

        // Bank brand
        JLabel brand = new JLabel("🏧  SecureATM");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        brand.setForeground(ATMTheme.ACCENT);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        p.add(brand);

        // Accent divider
        p.add(Box.createVerticalStrut(12));
        JPanel line = new JPanel();
        line.setBackground(ATMTheme.ACCENT);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.add(line);
        p.add(Box.createVerticalStrut(14));

        // User avatar circle
        Account acc  = controller.getCurrentAccount();
        User    user = controller.getCurrentUser();
        String  name = user != null ? user.getFullName() : "Account Holder";

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ATMTheme.ACCENT_DARK);
                g2.fillOval(0, 0, 40, 40);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2.setColor(ATMTheme.TEXT_WHITE);
                String initials = name.length() >= 2
                        ? String.valueOf(name.charAt(0)).toUpperCase()
                        : "?";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (40 - fm.stringWidth(initials)) / 2,
                        (40 - fm.getHeight()) / 2 + fm.getAscent());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
        };
        avatar.setAlignmentX(LEFT_ALIGNMENT);
        p.add(avatar);
        p.add(Box.createVerticalStrut(8));

        sideNameLabel = new JLabel(name);
        sideNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sideNameLabel.setForeground(ATMTheme.TEXT_WHITE);
        sideNameLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sideNameLabel);
        p.add(Box.createVerticalStrut(3));

        sideAccLabel = new JLabel(FormatUtil.maskAccountNumber(acc.getAccountNumber()));
        sideAccLabel.setFont(ATMTheme.FONT_MONO);
        sideAccLabel.setForeground(ATMTheme.TEXT_MUTED);
        sideAccLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sideAccLabel);
        p.add(Box.createVerticalStrut(14));

        // Balance block
        JPanel balanceBlock = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 50, 90, 120));
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }
        };
        balanceBlock.setLayout(new BoxLayout(balanceBlock, BoxLayout.Y_AXIS));
        balanceBlock.setOpaque(false);
        balanceBlock.setBorder(new EmptyBorder(10, 12, 10, 12));
        balanceBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel balTitle = new JLabel("Available Balance");
        balTitle.setFont(ATMTheme.FONT_SMALL);
        balTitle.setForeground(ATMTheme.TEXT_DIM);
        balTitle.setAlignmentX(LEFT_ALIGNMENT);
        balanceBlock.add(balTitle);
        balanceBlock.add(Box.createVerticalStrut(4));

        sideBalanceLabel = new JLabel(FormatUtil.formatCurrency(acc.getBalance()));
        sideBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        sideBalanceLabel.setForeground(ATMTheme.ACCENT);
        sideBalanceLabel.setAlignmentX(LEFT_ALIGNMENT);
        balanceBlock.add(sideBalanceLabel);

        balanceBlock.setAlignmentX(LEFT_ALIGNMENT);
        p.add(balanceBlock);

        return p;
    }

    private JPanel buildSidebarNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(ATMTheme.BG_SIDEBAR);
        nav.setBorder(new EmptyBorder(8, 0, 8, 0));

        Object[][] items = {
            {"🏠", "Home",               CARD_HOME},
            {"💰", "Deposit Money",       CARD_DEPOSIT},
            {"💸", "Withdraw Money",      CARD_WITHDRAW},
            {"↔",  "Transfer Money",      CARD_TRANSFER},
            {"💳", "Balance Inquiry",     CARD_BALANCE},
            {"📋", "Transaction History", CARD_HISTORY},
            {"📄", "Mini Statement",      CARD_MINI},
            {"🔑", "Change PIN",          CARD_CHANGE_PIN},
        };

        for (Object[] item : items) {
            JButton btn = buildNavButton(
                    (String) item[0], (String) item[1], (String) item[2]);
            nav.add(btn);
        }
        return nav;
    }

    private JButton buildNavButton(String icon, String label, String card) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (getBackground().equals(NAV_ACTIVE)) {
                    // Left accent bar
                    g2.setColor(ATMTheme.ACCENT);
                    g2.fillRect(0, 6, 3, getHeight() - 12);
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(ATMTheme.FONT_BODY);
        btn.setForeground(ATMTheme.TEXT_MUTED);
        btn.setBackground(NAV_NORMAL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setBackground(new Color(0, 40, 80, 180));
                    btn.setForeground(ATMTheme.TEXT_WHITE);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeNavBtn) {
                    btn.setBackground(NAV_NORMAL);
                    btn.setForeground(ATMTheme.TEXT_MUTED);
                }
            }
        });

        btn.addActionListener(e -> {
            setActiveNav(btn);
            showPanel(card);
        });
        return btn;
    }

    private void setActiveNav(JButton btn) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(NAV_NORMAL);
            activeNavBtn.setForeground(ATMTheme.TEXT_MUTED);
        }
        activeNavBtn = btn;
        btn.setBackground(NAV_ACTIVE);
        btn.setForeground(ATMTheme.ACCENT);
    }

    private JPanel buildSidebarFooter() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(ATMTheme.BG_SIDEBAR);
        p.setBorder(new EmptyBorder(8, 10, 20, 10));

        // Divider
        JPanel div = new JPanel();
        div.setBackground(ATMTheme.DIVIDER);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div.setAlignmentX(LEFT_ALIGNMENT);
        p.add(div);
        p.add(Box.createVerticalStrut(10));

        // Live clock
        sideTimeLabel = new JLabel(currentTime());
        sideTimeLabel.setFont(ATMTheme.FONT_SMALL);
        sideTimeLabel.setForeground(ATMTheme.TEXT_DIM);
        sideTimeLabel.setAlignmentX(LEFT_ALIGNMENT);
        sideTimeLabel.setBorder(new EmptyBorder(0, 6, 6, 0));
        p.add(sideTimeLabel);

        JButton logoutBtn = buildFooterBtn("⇦  Logout", ATMTheme.TEXT_MUTED, false);
        logoutBtn.addActionListener(e -> confirmLogout());
        p.add(logoutBtn);
        p.add(Box.createVerticalStrut(4));

        JButton exitBtn = buildFooterBtn("✕  Exit ATM", ATMTheme.RED, true);
        exitBtn.addActionListener(e -> confirmExit());
        p.add(exitBtn);
        return p;
    }

    private JButton buildFooterBtn(String text, Color color, boolean danger) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(color);
        btn.setBackground(ATMTheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(danger ? ATMTheme.RED : ATMTheme.BORDER_SUBTLE, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(danger ? new Color(80, 0, 0) : ATMTheme.BG_NAV_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(ATMTheme.BG_SIDEBAR);
            }
        });
        return btn;
    }

    // ── Content area ──────────────────────────────────────────────────────────

    private JPanel buildContentArea() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(ATMTheme.BG_DEEP);

        contentPanel.add(new HomePanel(controller, this),          CARD_HOME);
        contentPanel.add(new DepositPanel(controller, this),       CARD_DEPOSIT);
        contentPanel.add(new WithdrawPanel(controller, this),      CARD_WITHDRAW);
        contentPanel.add(new TransferPanel(controller, this),      CARD_TRANSFER);
        contentPanel.add(new BalancePanel(controller, this),       CARD_BALANCE);
        contentPanel.add(new HistoryPanel(controller, this),       CARD_HISTORY);
        contentPanel.add(new MiniStatementPanel(controller, this), CARD_MINI);
        contentPanel.add(new ChangePinPanel(controller, this),     CARD_CHANGE_PIN);

        return contentPanel;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void showPanel(String card) {
        refreshBalanceDisplay();
        cardLayout.show(contentPanel, card);
    }

    public void refreshBalanceDisplay() {
        Account acc = controller.getCurrentAccount();
        if (acc != null) sideBalanceLabel.setText(FormatUtil.formatCurrency(acc.getBalance()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void startClock() {
        Timer t = new Timer(1000, e -> sideTimeLabel.setText(currentTime()));
        t.start();
    }

    private String currentTime() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM  HH:mm:ss"));
    }

    private void confirmLogout() {
        int c = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            controller.logout();
            new LoginFrame(controller).setVisible(true);
            dispose();
        }
    }

    private void confirmExit() {
        int c = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the ATM?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) { controller.logout(); System.exit(0); }
    }
}
