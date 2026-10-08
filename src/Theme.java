import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public final class Theme {
    private Theme() {}

    public static final Color NAVY = new Color(0x1F, 0x3A, 0x5F);
    public static final Color NAVY_DARK = new Color(0x16, 0x2B, 0x47);
    public static final Color INK = new Color(0x1B, 0x1F, 0x24);
    public static final Color MUTED = new Color(0x6B, 0x72, 0x80);
    public static final Color LINE = new Color(0xE3, 0xE6, 0xEB);
    public static final Color BG = new Color(0xF6, 0xF7, 0xF9);
    public static final Color CARD = Color.WHITE;
    public static final Color ACCENT_SOFT = new Color(0xE8, 0xEF, 0xF9);
    public static final Color SPOT = new Color(0x0E, 0x7C, 0x6B);
    public static final Color SPOT_SOFT = new Color(0xDD, 0xF3, 0xEE);
    public static final Color REVIEW = new Color(0x8A, 0x4B, 0x0F);
    public static final Color REVIEW_SOFT = new Color(0xFB, 0xEE, 0xDC);
    public static final Color DANGER = new Color(0xB4, 0x23, 0x18);
    public static final Color DANGER_SOFT = new Color(0xFB, 0xE9, 0xE7);
    public static final Color OK = new Color(0x1B, 0x7F, 0x3B);
    public static final Color OK_SOFT = new Color(0xE3, 0xF5, 0xE9);

    private static final String FAMILY = pickFont();
    public static Font font(int size) { return new Font(FAMILY, Font.PLAIN, size); }
    public static Font bold(int size) { return new Font(FAMILY, Font.BOLD, size); }

    private static String pickFont() {
        String[] wanted = {"Inter", "Segoe UI", "SF Pro Text", "Helvetica Neue", "DejaVu Sans", "Arial"};
        String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String w : wanted) {
            for (String f : installed) {
                if (f.equals(w)) return w;
            }
        }
        return Font.SANS_SERIF;
    }

    public static void install() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("Panel.background", BG);
        UIManager.put("Label.font", font(13));
        UIManager.put("TextField.font", font(13));
        UIManager.put("TextArea.font", font(13));
        UIManager.put("ComboBox.font", font(13));
        UIManager.put("Button.font", font(13));
        UIManager.put("OptionPane.messageFont", font(13));
        UIManager.put("OptionPane.buttonFont", font(13));
        UIManager.put("TabbedPane.font", font(13));
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ComboBox.background", CARD);
        UIManager.put("TextField.caretForeground", INK);
    }


    public static JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text); l.setFont(font(size)); l.setForeground(color); return l;
    }
    public static JLabel heading(String text, int size) {
        JLabel l = new JLabel(text); l.setFont(bold(size)); l.setForeground(INK); return l;
    }
    public static JLabel muted(String text) { return label(text, 12, MUTED); }


    public static JComponent pill(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        l.setFont(bold(11)); l.setForeground(fg); l.setOpaque(false);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }


    public static JTextField field(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !placeholder.isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(MUTED); g2.setFont(getFont());
                    g2.drawString(placeholder, getInsets().left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        styleInput(f);
        return f;
    }
    public static JPasswordField password() { JPasswordField f = new JPasswordField(); styleInput(f); return f; }
    public static void styleInput(JComponent c) {
        c.setFont(font(13)); c.setBackground(CARD); c.setForeground(INK);
        c.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(LINE, 8), new EmptyBorder(8, 12, 8, 12)));
        c.setPreferredSize(new Dimension(240, 38));
    }
    public static JTextArea area(int rows) {
        JTextArea a = new JTextArea(rows, 20); a.setLineWrap(true); a.setWrapStyleWord(true); styleInput(a);
        a.setPreferredSize(null); return a;
    }


    public static JButton primary(String text) { return button(text, Color.WHITE, NAVY, NAVY_DARK); }
    public static JButton secondary(String text) { return button(text, NAVY, ACCENT_SOFT, new Color(0xD7, 0xE2, 0xF3)); }
    public static JButton ghost(String text) { return button(text, INK, CARD, BG); }
    public static JButton danger(String text) { return button(text, DANGER, DANGER_SOFT, new Color(0xF6, 0xD5, 0xD1)); }
    public static JButton success(String text) { return button(text, Color.WHITE, OK, new Color(0x15, 0x66, 0x2F)); }

    public static JButton button(String text, Color fg, Color bg, Color hover) {
        JButton b = new JButton(text) {
            private boolean over;
            { addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (over ? hover : bg) : LINE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (bg == CARD) { g2.setColor(LINE); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(bold(13)); b.setForeground(fg); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(9, 18, 9, 18)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(false);
        return b;
    }


    public static JPanel card() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(LINE); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(18, 20, 18, 20));
        return p;
    }

   
    public static class Notice extends JLabel {
        public Notice() { setFont(font(12)); setBorder(new EmptyBorder(8, 12, 8, 12)); setOpaque(false); setVisible(false); }
        public void error(String msg) { show(msg, DANGER, DANGER_SOFT); }
        public void success(String msg) { show(msg, OK, OK_SOFT); }
        public void clear() { setVisible(false); }
        private void show(String msg, Color fg, Color bg) {
            setText("<html>" + msg + "</html>"); setForeground(fg); putClientProperty("bg", bg); setVisible(true); revalidate(); repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            Color bg = (Color) getClientProperty("bg");
            if (bg != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    public static class RoundBorder implements Border {
        private final Color color; private final int radius;
        public RoundBorder(Color color, int radius) { this.color = color; this.radius = radius; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius); g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    public static JPanel vbox() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); return p; }
    public static JPanel hbox() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)) {
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
        };
        p.setOpaque(false); return p;
    }
    /** Flat combo box that matches the text fields. */
    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(CARD); g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(MUTED); g2.setStroke(new BasicStroke(1.6f));
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g2.drawLine(cx - 4, cy - 2, cx, cy + 2); g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                        g2.dispose();
                    }
                };
                b.setBorder(null); b.setContentAreaFilled(false); b.setFocusPainted(false); return b;
            }
        });
        c.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean sel, boolean foc) {
                JLabel x = (JLabel) super.getListCellRendererComponent(l, v, i, sel, foc);
                x.setBorder(new EmptyBorder(6, 8, 6, 8)); x.setFont(font(13));
                x.setBackground(sel ? ACCENT_SOFT : CARD); x.setForeground(INK); return x;
            }
        });
        styleInput(c); c.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(LINE, 8), new EmptyBorder(2, 6, 2, 4)));
        return c;
    }
    /** Thin, quiet scrollbar. */
    public static class SlimScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(0xC9, 0xCF, 0xD8); trackColor = BG; }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor); g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6); g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { g.setColor(trackColor); g.fillRect(r.x, r.y, r.width, r.height); }
    }
    public static Component gap(int h) { return Box.createRigidArea(new Dimension(0, h)); }
    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c); sp.setBorder(null); sp.getViewport().setOpaque(false); sp.setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16); sp.getVerticalScrollBar().setUI(new SlimScrollBarUI()); sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); return sp;
    }
    public static JLabel formLabel(String t) { JLabel l = label(t, 12, MUTED); l.setFont(bold(12)); return l; }
}
