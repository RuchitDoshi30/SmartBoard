package com.smartboard.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Objects;

public final class UiKit {
    // Palette
    public static final Color BG = new Color(248, 249, 250);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(23, 23, 23);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color PRIMARY = new Color(59, 130, 246);
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color WARNING = new Color(217, 119, 6);

    private UiKit() {}

    public static Font loadFont(Component c) {
        try {
            Font poppins = Font.createFont(Font.TRUETYPE_FONT,
                    Objects.requireNonNull(c.getClass().getResourceAsStream("/resource/Poppins-Regular.ttf")))
                .deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(poppins);
            return poppins;
        } catch (Exception e) {
            return new Font("Segoe UI", Font.PLAIN, 14);
        }
    }

    // Header
    public static JPanel header(Font font, String title, JComponent... right) {
        RoundedPanel panel = new RoundedPanel(16);
        panel.setLayout(new BorderLayout(12, 0));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel t = new JLabel(title);
        t.setFont(font.deriveFont(Font.BOLD, 20f));
        t.setForeground(TEXT);
        panel.add(t, BorderLayout.WEST);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        r.setOpaque(false);
        if (right != null) for (JComponent c : right) r.add(c);
        panel.add(r, BorderLayout.EAST);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));
        wrap.add(panel);
        return wrap;
    }

    // Components
    public static class RoundedPanel extends JPanel {
        private final int radius;
        public RoundedPanel(int radius) { this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0,20));
            g2.fillRoundRect(3,5,getWidth()-6,getHeight()-6,radius,radius);
            g2.setColor(getBackground());
            g2.fillRoundRect(0,0,getWidth()-6,getHeight()-8,radius,radius);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

    public static class GhostButton extends JButton {
        public GhostButton(String text) { super(text); setup(); }
        private void setup() {
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover()) {
                g2.setColor(new Color(0,0,0,20));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class PrimaryButton extends JButton {
        public PrimaryButton(String text) { super(text); setup(); }
        private void setup() {
            setFocusPainted(false);
            setForeground(Color.WHITE);
            setBackground(PRIMARY);
            setBorder(BorderFactory.createEmptyBorder(10,18,10,18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class ModernTextField extends JTextField {
        private String placeholder = "";
        public ModernTextField() { setOpaque(false); setBorder(BorderFactory.createEmptyBorder(10,14,10,14)); }
        public ModernTextField setPlaceholder(String ph) { this.placeholder = ph; return this; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            g2.setColor(BORDER);
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner() && !placeholder.isEmpty()) {
                g2.setColor(MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, 14, (getHeight()+fm.getAscent()-fm.getDescent())/2 - 1);
            }
            g2.dispose();
        }
        @Override public boolean isOpaque() { return false; }
    }

    public static class ModernTextArea extends JTextArea {
        public ModernTextArea(int rows, int cols) { super(rows, cols); setOpaque(false); setBorder(BorderFactory.createEmptyBorder(10,14,10,14)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            g2.setColor(BORDER);
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
            super.paintComponent(g);
            g2.dispose();
        }
        @Override public boolean isOpaque() { return false; }
    }

    public static class PillLabel extends JLabel {
        private final Color bg;
        public PillLabel(String text, Color bg, Color fg, int padH) {
            super(text);
            this.bg = bg; setForeground(fg);
            setBorder(BorderFactory.createEmptyBorder(6, padH, 6, padH));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override public boolean isOpaque() { return false; }
    }

    public static void styleTable(JTable table, Font font) {
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setFont(font.deriveFont(Font.PLAIN, 13f));
        table.setBackground(CARD);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(243,244,246));
        header.setForeground(TEXT);
        header.setFont(font.deriveFont(Font.BOLD, 13f));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // striped rows + hover
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setForeground(TEXT);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD : new Color(249, 250, 251));
                }
                return c;
            }
        });
    }
}
