package com.oasisinfobyte.atm.ui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Centralised UI theme — Premium Dark Banking Edition.
 * Provides all colours, fonts, borders, and component factories.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public final class ATMTheme {

    // ── Colour Palette ────────────────────────────────────────────────────────
    public static final Color BG_DEEP       = new Color(8,   16,  36);
    public static final Color BG_CARD       = new Color(14,  26,  52);
    public static final Color BG_INPUT      = new Color(18,  34,  65);
    public static final Color BG_SIDEBAR    = new Color(10,  20,  42);
    public static final Color BG_NAV_HOVER  = new Color(0,   30,  65);

    public static final Color ACCENT        = new Color(0,   198, 255);
    public static final Color ACCENT_DARK   = new Color(0,   140, 200);
    public static final Color ACCENT_GLOW   = new Color(0,   198, 255, 50);

    public static final Color TEXT_WHITE    = new Color(230, 242, 255);
    public static final Color TEXT_MUTED    = new Color(130, 165, 210);
    public static final Color TEXT_DIM      = new Color(70,  100, 150);

    public static final Color GREEN         = new Color(0,   220, 110);
    public static final Color RED           = new Color(255,  70,  70);
    public static final Color AMBER         = new Color(255, 195,   0);

    public static final Color BORDER_SUBTLE = new Color(25,  50,  95);
    public static final Color DIVIDER       = new Color(20,  40,  80);

    // Legacy aliases kept for existing panel code
    public static final Color PRIMARY_DARK   = BG_DEEP;
    public static final Color PRIMARY_ACCENT = ACCENT;
    public static final Color PRIMARY_LIGHT  = new Color(72, 202, 228);
    public static final Color TEXT_PRIMARY   = TEXT_WHITE;
    public static final Color TEXT_SECONDARY = TEXT_MUTED;
    public static final Color SURFACE        = BG_CARD;
    public static final Color INPUT_BG       = BG_INPUT;
    public static final Color SUCCESS        = GREEN;
    public static final Color ERROR          = RED;
    public static final Color WARNING        = AMBER;

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_HERO     = new Font("Segoe UI", Font.BOLD,  30);
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font FONT_AMOUNT   = new Font("Segoe UI", Font.BOLD,  30);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Dimensions ────────────────────────────────────────────────────────────
    public static final int BTN_H   = 40;
    public static final int INPUT_H = 38;
    public static final int PAD_SM  = 8;
    public static final int PAD_MD  = 16;
    public static final int PAD_LG  = 24;
    public static final int RADIUS  = 10;

    /** Private constructor — utility class. */
    private ATMTheme() {}

    // ── Borders ───────────────────────────────────────────────────────────────

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(PAD_LG, PAD_LG, PAD_LG, PAD_LG)
        );
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        );
    }

    public static Border inputBorderFocused() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        );
    }

    public static Border emptyBorder(int t, int l, int b, int r) {
        return BorderFactory.createEmptyBorder(t, l, b, r);
    }

    // ── Component Factories ───────────────────────────────────────────────────

    /** Primary (solid cyan) rounded button. */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new RoundButton(text, ACCENT, BG_DEEP);
        btn.setFont(FONT_BUTTON);
        btn.setPreferredSize(new Dimension(120, BTN_H));
        return btn;
    }

    /** Secondary (outline) rounded button. */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new RoundButton(text, BG_CARD, ACCENT);
        btn.setFont(FONT_BUTTON);
        btn.setPreferredSize(new Dimension(120, BTN_H));
        ((RoundButton) btn).setOutline(true);
        return btn;
    }

    /** Danger (red) rounded button. */
    public static JButton createDangerButton(String text) {
        JButton btn = new RoundButton(text, RED, Color.WHITE);
        btn.setFont(FONT_BUTTON);
        btn.setPreferredSize(new Dimension(120, BTN_H));
        return btn;
    }

    /** Styled text field with focus-border animation. */
    public static JTextField createTextField() {
        JTextField f = new JTextField();
        f.setFont(FONT_INPUT);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(ACCENT);
        f.setBorder(inputBorder());
        f.setPreferredSize(new Dimension(200, INPUT_H));
        addFocusBorderEffect(f);
        return f;
    }

    /** Styled password field with focus-border animation. */
    public static JPasswordField createPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(FONT_INPUT);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(ACCENT);
        f.setBorder(inputBorder());
        f.setPreferredSize(new Dimension(200, INPUT_H));
        addFocusBorderEffect(f);
        return f;
    }

    public static JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static JLabel createTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(TEXT_WHITE);
        return l;
    }

    public static JPanel createCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(cardBorder());
        return p;
    }

    // ── Global defaults ───────────────────────────────────────────────────────

    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background",          BG_DEEP);
        UIManager.put("Label.foreground",          TEXT_WHITE);
        UIManager.put("TextField.background",      BG_INPUT);
        UIManager.put("TextField.foreground",      TEXT_WHITE);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("PasswordField.background",  BG_INPUT);
        UIManager.put("PasswordField.foreground",  TEXT_WHITE);
        UIManager.put("Button.background",         ACCENT);
        UIManager.put("Button.foreground",         BG_DEEP);
        UIManager.put("ScrollPane.background",     BG_DEEP);
        UIManager.put("Viewport.background",       BG_CARD);
        UIManager.put("Table.background",          BG_CARD);
        UIManager.put("Table.foreground",          TEXT_WHITE);
        UIManager.put("Table.gridColor",           DIVIDER);
        UIManager.put("Table.selectionBackground", BG_DEEP);
        UIManager.put("Table.selectionForeground", ACCENT);
        UIManager.put("TableHeader.background",    ACCENT);
        UIManager.put("TableHeader.foreground",    BG_DEEP);
        UIManager.put("OptionPane.background",     BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_WHITE);
        UIManager.put("SplitPane.background",      BG_DEEP);
        UIManager.put("SplitPaneDivider.background", BG_DEEP);
        UIManager.put("ScrollBar.background",      BG_DEEP);
        UIManager.put("ScrollBar.thumb",           BORDER_SUBTLE);
        UIManager.put("ScrollBar.track",           BG_DEEP);
        UIManager.put("TabbedPane.background",     BG_CARD);
        UIManager.put("TabbedPane.foreground",     TEXT_WHITE);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private static void addFocusBorderEffect(JComponent c) {
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                c.setBorder(inputBorderFocused());
            }
            @Override public void focusLost(FocusEvent e) {
                c.setBorder(inputBorder());
            }
        });
    }

    // ── Inner: Custom Rounded Button ─────────────────────────────────────────

    public static class RoundButton extends JButton {
        private Color bgColor;
        private Color fgColor;
        private boolean outline = false;

        public RoundButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.fgColor = fg;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    bgColor = outline ? ACCENT          : ACCENT_DARK;
                    fgColor = outline ? BG_DEEP         : TEXT_WHITE;
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    bgColor = outline ? BG_CARD         : ACCENT;
                    fgColor = outline ? ACCENT          : BG_DEEP;
                    repaint();
                }
                @Override public void mousePressed(MouseEvent e) {
                    bgColor = ACCENT_DARK;
                    repaint();
                }
                @Override public void mouseReleased(MouseEvent e) {
                    bgColor = outline ? BG_CARD : ACCENT;
                    fgColor = outline ? ACCENT  : BG_DEEP;
                    repaint();
                }
            });
        }

        public void setOutline(boolean outline) {
            this.outline = outline;
            this.bgColor = outline ? BG_CARD : ACCENT;
            this.fgColor = outline ? ACCENT  : BG_DEEP;
            setForeground(fgColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (outline) {
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, RADIUS * 2, RADIUS * 2));
                g2.setColor(ACCENT);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, w - 1.5f, h - 1.5f, RADIUS * 2, RADIUS * 2));
            } else {
                // Subtle gradient fill
                GradientPaint gp = new GradientPaint(0, 0, bgColor.brighter(),
                        0, h, bgColor);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, RADIUS * 2, RADIUS * 2));
            }
            // Text
            g2.setFont(getFont());
            g2.setColor(fgColor);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(Math.max(d.width + 20, 110), BTN_H);
        }
    }
}
