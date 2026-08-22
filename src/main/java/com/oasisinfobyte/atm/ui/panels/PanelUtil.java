package com.oasisinfobyte.atm.ui.panels;

import com.oasisinfobyte.atm.ui.theme.ATMTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared panel-building utilities to keep all operation panels DRY.
 *
 * @author Oasis Infobyte ATM Project
 * @version 2.0.0
 */
public final class PanelUtil {

    private PanelUtil() {}

    /** Rounded card panel with dark surface and subtle border. */
    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ATMTheme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(ATMTheme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 16, 16));
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        return p;
    }

    /** Wraps a card in a top-left-anchored scroll panel. */
    public static JPanel centerCard(JPanel card) {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.weightx = 1.0;
        g.weighty = 1.0;
        outer.add(card, g);
        return outer;
    }

    /** Blue info banner row. */
    public static JPanel infoRow(String message) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 60, 120, 120));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
            }
        };
        p.setOpaque(false);
        JLabel l = new JLabel("ℹ  " + message);
        l.setFont(ATMTheme.FONT_SMALL);
        l.setForeground(ATMTheme.ACCENT);
        p.add(l);
        return p;
    }

    /** Red/neutral status label for error messages. */
    public static JLabel statusLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(ATMTheme.FONT_BODY);
        l.setForeground(ATMTheme.RED);
        return l;
    }

    /** Green result label for success messages. */
    public static JLabel resultLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(ATMTheme.FONT_BODY);
        l.setForeground(ATMTheme.GREEN);
        return l;
    }

    /** Horizontal divider line. */
    public static JPanel divider() {
        JPanel p = new JPanel();
        p.setBackground(ATMTheme.DIVIDER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return p;
    }
}
