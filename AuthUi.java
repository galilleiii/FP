package lspu_hnn_ui;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.plaf.basic.BasicTextFieldUI;

/** Look of the Sign in / Sign up screens: pixel heading, rounded dark inputs, plum pill buttons, purple links. */
final class AuthUi {

    private AuthUi() {
    }

    static final Color CARD = new Color(20, 14, 24, 228);
    static final Color CARD_EDGE = new Color(255, 255, 255, 28);
    static final Color FIELD = new Color(62, 58, 66);
    static final Color FIELD_TEXT = new Color(238, 234, 244);
    static final Color HINT = new Color(150, 146, 158);
    static final Color PLUM = new Color(0x5C1A63);
    static final Color PLUM_HOT = new Color(0x7E2C8A);
    static final Color PLUM_DIM = new Color(0x3F1446);
    static final Color LINK = new Color(0xB84DFF);
    static final Color LABEL = new Color(0xF2EEF6);

    // ---------------------------------------------------------------- fonts

    private static final Set<String> FAMILIES = new HashSet<>(
            java.util.Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

    /** Geometric sans like the mockup (Futura) if installed; otherwise the nearest common font. */
    private static final String SANS = pick("Futura", "Futura PT", "Jost", "Century Gothic", "Segoe UI");

    private static String pick(String... names) {
        for (String n : names) if (FAMILIES.contains(n)) return n;
        return Font.SANS_SERIF;
    }

    static Font font(int style, float size) {
        return new Font(SANS, style, 12).deriveFont(style, size);
    }

    static Font tracked(Font f, float tracking) {
        Map<TextAttribute, Object> m = new HashMap<>();
        m.put(TextAttribute.TRACKING, tracking);
        return f.deriveFont(m);
    }

    // ---------------------------------------------------------------- labels and links

    static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    static JLabel link(String text, float size, Runnable action) {
        JLabel l = new JLabel("<html><u>" + text + "</u></html>");
        l.setFont(font(Font.PLAIN, size));
        l.setForeground(LINK);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                l.setForeground(LINK.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                l.setForeground(LINK);
            }
        });
        return l;
    }

    // ---------------------------------------------------------------- inputs

    private static void styleInput(JTextField f) {
        f.setOpaque(false);
        f.setForeground(FIELD_TEXT);
        f.setCaretColor(FIELD_TEXT);
        f.setSelectionColor(PLUM_HOT);
        f.setSelectedTextColor(Color.WHITE);
        f.setFont(font(Font.PLAIN, 12.5f));
        f.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        f.setPreferredSize(new Dimension(100, 30));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    }

    private static void paintInput(JTextField f, Graphics g0) {
        Graphics2D g = Ui.aa(g0);
        g.setColor(FIELD);
        g.fillRoundRect(0, 0, f.getWidth(), f.getHeight(), f.getHeight(), f.getHeight());
        if (f.isFocusOwner()) {
            g.setColor(new Color(LINK.getRed(), LINK.getGreen(), LINK.getBlue(), 200));
            g.setStroke(new BasicStroke(1.4f));
            g.drawRoundRect(0, 0, f.getWidth() - 1, f.getHeight() - 1, f.getHeight(), f.getHeight());
        }
        g.dispose();
    }

    private static void paintHint(JTextField f, Graphics g0, String hint) {
        if (!f.getText().isEmpty() || hint == null) return;
        Graphics2D g = Ui.aa(g0);
        g.setFont(f.getFont());
        g.setColor(HINT);
        Insets in = f.getInsets();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(hint, in.left, (f.getHeight() - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
    }

    /** Rounded dark text field with a hint. BasicTextFieldUI avoids Nimbus drawing its own square box. */
    static final class Field extends JTextField {
        private final String hint;

        Field(String hint) {
            this.hint = hint;
            styleInput(this);
        }

        @Override
        public void updateUI() {
            setUI(new BasicTextFieldUI());
        }

        @Override
        protected void paintComponent(Graphics g0) {
            paintInput(this, g0);
            super.paintComponent(g0);
            paintHint(this, g0, hint);
        }
    }

    static final class Password extends JPasswordField {
        private final String hint;

        Password(String hint) {
            this.hint = hint;
            styleInput(this);
        }

        @Override
        public void updateUI() {
            setUI(new BasicPasswordFieldUI());
        }

        @Override
        protected void paintComponent(Graphics g0) {
            paintInput(this, g0);
            super.paintComponent(g0);
            if (getPassword().length == 0) paintHint(this, g0, hint);
        }
    }

    /** Rounded dark drop-down in the same style as the fields. */
    static final class Combo extends JComboBox<String> {
        Combo(String... items) {
            super(items);
            setOpaque(false);
            setFont(font(Font.PLAIN, 12.5f));
            setForeground(FIELD_TEXT);
            setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 4));
            setPreferredSize(new Dimension(100, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton b = new JButton() {
                        @Override
                        protected void paintComponent(Graphics g0) {
                            Graphics2D g = Ui.aa(g0);
                            g.setColor(HINT);
                            int cx = getWidth() / 2, cy = getHeight() / 2;
                            g.fillPolygon(new int[]{cx - 4, cx + 4, cx}, new int[]{cy - 2, cy - 2, cy + 3}, 3);
                            g.dispose();
                        }
                    };
                    b.setBorder(BorderFactory.createEmptyBorder());
                    b.setContentAreaFilled(false);
                    b.setFocusPainted(false);
                    b.setPreferredSize(new Dimension(26, 20));
                    return b;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean hasFocus) {
                    // the rounded background is painted by the combo box itself
                }
            });
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object v, int index, boolean sel, boolean foc) {
                    super.getListCellRendererComponent(list, v, index, sel, false);
                    setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
                    setFont(font(Font.PLAIN, 12.5f));
                    boolean row = index >= 0;
                    setOpaque(row);                                    // the value shown in the box stays transparent
                    setForeground(FIELD_TEXT);
                    setBackground(row ? (sel ? PLUM : new Color(38, 30, 46)) : FIELD);
                    return this;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Ui.aa(g0);
            g.setColor(FIELD);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ---------------------------------------------------------------- buttons

    /** Plum pill button. Also used as the Student / Administrator toggle (setActive). */
    static final class Pill extends JButton {
        private boolean hover;
        private boolean active = true;
        private boolean toggle;

        Pill(String text, boolean small) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(tracked(font(Font.BOLD, small ? 9.5f : 10.5f), 0.08f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(small ? 4 : 6, small ? 18 : 24, small ? 4 : 6, small ? 18 : 24));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void asToggle() {
            toggle = true;
        }

        void setActive(boolean a) {
            active = a;
            setForeground(a || !toggle ? Color.WHITE : new Color(255, 255, 255, 170));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Ui.aa(g0);
            int w = getWidth() - 1, h = getHeight() - 1;
            if (isEnabled()) {
                g.setColor(new Color(0, 0, 0, 80));                 // soft shadow under the pill
                g.fillRoundRect(1, 3, w - 1, h - 2, h, h);
            }
            Color base = !isEnabled() ? PLUM_DIM : (toggle && !active ? PLUM_DIM : (hover ? PLUM_HOT : PLUM));
            g.setColor(base);
            g.fillRoundRect(0, 0, w, h - 2, h, h);
            if (toggle && active) {
                g.setColor(new Color(LINK.getRed(), LINK.getGreen(), LINK.getBlue(), 230));
                g.setStroke(new BasicStroke(1.3f));
                g.drawRoundRect(0, 0, w, h - 2, h, h);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    // ---------------------------------------------------------------- pixel heading

    private static final Map<Character, String[]> GLYPHS = new HashMap<>();

    private static void glyph(char c, String... rows) {
        GLYPHS.put(c, rows);
    }

    static {
        glyph('S', ".####", "#....", "#....", ".###.", "....#", "....#", "####.", ".....", ".....");
        glyph('i', "#", ".", "#", "#", "#", "#", "#", ".", ".");
        glyph('g', ".....", ".....", ".####", "#...#", "#...#", "#...#", ".####", "....#", ".###.");
        glyph('n', ".....", ".....", "####.", "#...#", "#...#", "#...#", "#...#", ".....", ".....");
        glyph('u', ".....", ".....", "#...#", "#...#", "#...#", "#..##", ".##.#", ".....", ".....");
        glyph('p', ".....", ".....", "####.", "#...#", "#...#", "#...#", "####.", "#....", "#....");
        glyph(' ', "...", "...", "...", "...", "...", "...", "...", "...", "...");
    }

    /**
     * Pixel-art heading with a pink/cyan glitch edge, drawn from bitmaps so no font file is needed.
     * Only the letters in "Sign in" and "Sign up" are defined; add more with glyph(...) above.
     */
    static final class PixelTitle extends JComponent {
        private final String text;
        private static final int PX = 4;

        PixelTitle(String text) {
            this.text = text;
            int cols = 0;
            for (char c : text.toCharArray()) {
                String[] g = GLYPHS.getOrDefault(c, GLYPHS.get(' '));
                cols += g[0].length() + 1;
            }
            setPreferredSize(new Dimension(cols * PX + 6, 9 * PX + 4));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 9 * PX + 4));
            setOpaque(false);
        }

        private void draw(Graphics2D g, int ox, int oy, Color color) {
            g.setColor(color);
            int x = ox;
            for (char c : text.toCharArray()) {
                String[] rows = GLYPHS.getOrDefault(c, GLYPHS.get(' '));
                for (int r = 0; r < rows.length; r++) {
                    for (int k = 0; k < rows[r].length(); k++) {
                        if (rows[r].charAt(k) == '#') g.fillRect(x + k * PX, oy + r * PX, PX, PX);
                    }
                }
                x += (rows[0].length() + 1) * PX;
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();      // no antialiasing: keeps the pixels crisp
            draw(g, 5, 3, new Color(255, 77, 219, 170));  // pink edge
            draw(g, 1, 2, new Color(77, 232, 255, 170));  // cyan edge
            draw(g, 3, 2, Color.WHITE);
            g.dispose();
        }
    }
}