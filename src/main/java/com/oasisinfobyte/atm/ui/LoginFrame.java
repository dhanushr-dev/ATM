package com.oasisinfobyte.atm.ui;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.ui.theme.ATMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Premium Login Screen — ATM Interface.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public class LoginFrame extends JFrame {

    private final ATMController controller;

    private JTextField    accountField;
    private JPasswordField pinField;
    // Initialized here so they are NEVER null, even before buildCard() runs
    private JLabel        statusLabel  = new JLabel(" ");
    private JButton       loginBtn     = new JButton("LOGIN");

    public LoginFrame(ATMController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("SecureATM — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(460, 640);
        setLocationRelativeTo(null);

        // Deep-dark background panel
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(5, 10, 25), 0, getHeight(), ATMTheme.BG_DEEP);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCard(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(36, 0, 20, 0));

        // Logo circle with ATM icon
        JLabel logo = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Glow ring
                g2.setColor(ATMTheme.ACCENT_GLOW);
                g2.fillOval(4, 4, 56, 56);
                // Circle
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(0, 100, 160), 0, getHeight(), new Color(0, 60, 110));
                g2.setPaint(gp);
                g2.fillOval(8, 8, 48, 48);
                // Icon
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
                g2.setColor(ATMTheme.ACCENT);
                FontMetrics fm = g2.getFontMetrics();
                String icon = "🏧";
                int tx = (64 - fm.stringWidth(icon)) / 2;
                int ty = (64 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, tx, ty);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(64, 64); }
        };
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(logo);
        p.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("SecureATM");
        title.setFont(ATMTheme.FONT_HERO);
        title.setForeground(ATMTheme.TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(6));

        JLabel sub = new JLabel("Oasis Infobyte Banking System");
        sub.setFont(ATMTheme.FONT_BODY);
        sub.setForeground(ATMTheme.TEXT_DIM);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(sub);
        return p;
    }

    // ── Card ─────────────────────────────────────────────────────────────────

    private JPanel buildCard() {
        // Rounded card panel
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ATMTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                // Top accent line
                g2.setColor(ATMTheme.ACCENT);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(40, 2, getWidth() - 40, 2);
                // Subtle border
                g2.setColor(ATMTheme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 20, 20));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(28, 32, 28, 32));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridx   = 0;
        g.insets  = new Insets(5, 0, 5, 0);

        // Account number
        g.gridy = 0;
        JLabel accLbl = ATMTheme.createLabel("ACCOUNT NUMBER");
        card.add(accLbl, g);

        g.gridy = 1;
        accountField = ATMTheme.createTextField();
        accountField.setToolTipText("Enter your 16-digit account number");
        accountField.setDocument(new NumericDocument(16));
        accountField.setPreferredSize(new Dimension(360, ATMTheme.INPUT_H));
        card.add(accountField, g);

        // PIN
        g.gridy = 2; g.insets = new Insets(14, 0, 5, 0);
        card.add(ATMTheme.createLabel("PIN"), g);

        g.gridy = 3; g.insets = new Insets(5, 0, 5, 0);
        JPanel pinRow = new JPanel(new BorderLayout(6, 0));
        pinRow.setOpaque(false);

        pinField = ATMTheme.createPasswordField();
        pinField.setToolTipText("Enter your 4-digit PIN");
        pinField.setDocument(new NumericDocument(4));
        pinField.setPreferredSize(new Dimension(310, ATMTheme.INPUT_H));

        JButton togglePinBtn = ATMTheme.createSecondaryButton("👁");
        togglePinBtn.setToolTipText("Show/Hide PIN");
        togglePinBtn.setPreferredSize(new Dimension(44, ATMTheme.INPUT_H));
        togglePinBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        togglePinBtn.addActionListener(e -> {
            if (pinField.getEchoChar() == (char) 0) {
                pinField.setEchoChar('•');
                togglePinBtn.setText("👁");
            } else {
                pinField.setEchoChar((char) 0);
                togglePinBtn.setText("🙈");
            }
        });

        pinRow.add(pinField, BorderLayout.CENTER);
        pinRow.add(togglePinBtn, BorderLayout.EAST);
        card.add(pinRow, g);

        // PIN dot indicators
        g.gridy = 4; g.insets = new Insets(4, 0, 4, 0);
        JPanel dotsPanel = buildPinDots();
        card.add(dotsPanel, g);

        // Status label
        g.gridy = 5; g.insets = new Insets(8, 0, 2, 0);
        statusLabel = new JLabel(" ");
        statusLabel.setFont(ATMTheme.FONT_BODY);
        statusLabel.setForeground(ATMTheme.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel, g);

        // Login button
        g.gridy = 6; g.insets = new Insets(10, 0, 6, 0);
        loginBtn = ATMTheme.createPrimaryButton("LOGIN");
        loginBtn.setPreferredSize(new Dimension(360, 44));
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.addActionListener(e -> performLogin());
        card.add(loginBtn, g);

        // Clear button
        g.gridy = 7; g.insets = new Insets(4, 0, 4, 0);
        JButton clearBtn = ATMTheme.createSecondaryButton("CLEAR");
        clearBtn.setPreferredSize(new Dimension(360, 38));
        clearBtn.addActionListener(e -> clearForm());
        card.add(clearBtn, g);

        // Open New Account button
        g.gridy = 8; g.insets = new Insets(6, 0, 0, 0);
        JButton regBtn = ATMTheme.createSecondaryButton("➕  Open New Account");
        regBtn.setPreferredSize(new Dimension(360, 36));
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        regBtn.setForeground(ATMTheme.ACCENT);
        regBtn.addActionListener(e -> new RegisterDialog(this, controller).setVisible(true));
        card.add(regBtn, g);

        // Enter key on PIN triggers login
        pinField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });
        // Tab on account number moves to PIN
        accountField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) pinField.requestFocus();
            }
        });

        // Wrapper for horizontal centering with side padding
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints wg = new GridBagConstraints();
        wg.fill    = GridBagConstraints.HORIZONTAL;
        wg.weightx = 1.0;
        wg.weighty = 1.0;
        wg.anchor  = GridBagConstraints.NORTH;
        wg.insets  = new Insets(0, 24, 0, 24);
        wrapper.add(card, wg);
        return wrapper;
    }

    private JPanel buildPinDots() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        p.setOpaque(false);

        JLabel[] dots = new JLabel[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            dots[i] = new JLabel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int len = pinField.getPassword().length;
                    if (idx < len) {
                        g2.setColor(ATMTheme.ACCENT);
                        g2.fillOval(2, 2, 11, 11);
                    } else {
                        g2.setColor(ATMTheme.BORDER_SUBTLE);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawOval(2, 2, 11, 11);
                    }
                }
                @Override public Dimension getPreferredSize() { return new Dimension(15, 15); }
            };
            p.add(dots[i]);
        }
        // Repaint dots on PIN field change
        pinField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { repaintDots(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { repaintDots(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { repaintDots(); }
            private void repaintDots() { for (JLabel d : dots) d.repaint(); }
        });
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 0, 14, 0));
        JLabel lbl = new JLabel("🔒  Secured  •  © 2024 Oasis Infobyte");
        lbl.setFont(ATMTheme.FONT_SMALL);
        lbl.setForeground(ATMTheme.TEXT_DIM);
        p.add(lbl);
        return p;
    }

    // ── Events ────────────────────────────────────────────────────────────────

    private void performLogin() {
        String acct = accountField.getText().trim();
        String pin  = new String(pinField.getPassword()).trim();

        if (acct.isEmpty() || pin.isEmpty()) {
            showError("Please enter your Account Number and PIN.");
            return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Authenticating…");

        SwingWorker<Account, Void> worker = new SwingWorker<>() {
            @Override protected Account doInBackground() {
                return controller.login(acct, pin);
            }
            @Override protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("LOGIN");
                try {
                    Account account = get();
                    if (account == null) {
                        showError("Authentication failed. Please try again.");
                        return;
                    }
                    // Success
                    clearForm();
                    new DashboardFrame(controller).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    showError(cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void clearForm() {
        accountField.setText("");
        pinField.setText("");
        statusLabel.setText(" ");
        accountField.requestFocus();
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(ATMTheme.RED);
        pinField.setText("");
        pinField.requestFocus();
        // Shake animation
        shakeWindow();
    }

    /** Quick horizontal shake to signal a failed login attempt. */
    private void shakeWindow() {
        final int[] dx = {0, 8, -8, 6, -6, 4, -4, 0};
        final int[] idx = {0};
        Point orig = getLocation();
        Timer t = new Timer(35, null);
        t.addActionListener(e -> {
            if (idx[0] >= dx.length) {
                setLocation(orig);
                t.stop();
                return;
            }
            setLocation(orig.x + dx[idx[0]++], orig.y);
        });
        t.start();
    }

    // ── Inner: Numeric-only document ─────────────────────────────────────────

    private static class NumericDocument extends PlainDocument {
        private final int max;
        NumericDocument(int max) { this.max = max; }

        @Override
        public void insertString(int off, String str, AttributeSet a) throws BadLocationException {
            if (str == null) return;
            String digits = str.replaceAll("[^0-9]", "");
            if ((getLength() + digits.length()) <= max) super.insertString(off, digits, a);
        }
    }
}
