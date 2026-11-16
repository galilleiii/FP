//package lspu_hnn_ui;
//
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Cursor;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Insets;
//import java.awt.Rectangle;
//import java.awt.RenderingHints;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//
//import javax.swing.BorderFactory;
//import javax.swing.Icon;
//import javax.swing.JButton;
//import javax.swing.JComboBox;
//import javax.swing.DefaultListCellRenderer;
//import javax.swing.JComponent;
//import javax.swing.JLabel;
//import javax.swing.JList;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTable;
//import javax.swing.JTextField;
//import javax.swing.plaf.basic.BasicComboBoxUI;
//import javax.swing.table.DefaultTableCellRenderer;
//
///** Small helpers so every page looks the same. */
//public final class Ui {
//
//    private Ui() {
//    }
//
//    public enum Kind {PRIMARY, GHOST, DANGER}
//
//    public static Graphics2D aa(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g.create();
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        return g2;
//    }
//
//    // ---------------------------------------------------------------- buttons
//
//    private static final class Btn extends JButton {
//        private final Kind kind;
//        private boolean hover;
//
//        Btn(String text, Kind kind, boolean small) {
//            super(text);
//            this.kind = kind;
//            setContentAreaFilled(false);
//            setBorderPainted(false);
//            setFocusPainted(false);
//            setOpaque(false);
//            setForeground(Theme.TEXT);
//            setFont(Theme.font(Font.BOLD, small ? 11.5f : 12.5f));
//            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//            setBorder(small ? BorderFactory.createEmptyBorder(5, 11, 5, 11) : BorderFactory.createEmptyBorder(8, 16, 8, 16));
//            addMouseListener(new MouseAdapter() {
//                @Override
//                public void mouseEntered(MouseEvent e) {
//                    hover = true;
//                    repaint();
//                }
//
//                @Override
//                public void mouseExited(MouseEvent e) {
//                    hover = false;
//                    repaint();
//                }
//            });
//        }
//
//        @Override
//        public void setEnabled(boolean b) {
//            super.setEnabled(b);
//            setForeground(b ? Theme.TEXT : Theme.MUTED);
//        }
//
//        @Override
//        protected void paintComponent(Graphics g0) {
//            Graphics2D g = aa(g0);
//            Color base = switch (kind) {
//                case PRIMARY -> Theme.ACCENT_DARK;
//                case DANGER -> new Color(0x9F2D2D);
//                case GHOST -> new Color(0x1A2A47);
//            };
//            if (!isEnabled()) base = Theme.BORDER;
//            else if (hover) base = base.brighter();
//            g.setColor(base);
//            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
//            if (kind == Kind.GHOST) {
//                g.setColor(Theme.BORDER);
//                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
//            }
//            g.dispose();
//            super.paintComponent(g0);
//        }
//    }
//
//    public static JButton button(String text, Kind kind) {
//        return new Btn(text, kind, false);
//    }
//
//    public static JButton button(String text) {
//        return new Btn(text, Kind.GHOST, false);
//    }
//
//    /** Smaller button for crowded panels such as the dashboard. */
//    public static JButton small(String text) {
//        return new Btn(text, Kind.GHOST, true);
//    }
//
//    // ---------------------------------------------------------------- text
//
//    public static JLabel label(String text, int style, float size, Color color) {
//        JLabel l = new JLabel(text);
//        l.setFont(Theme.font(style, size));
//        l.setForeground(color);
//        return l;
//    }
//
//    public static JLabel muted(String text) {
//        return label(text, Font.PLAIN, 12f, Theme.MUTED);
//    }
//
//    public static JTextField field(int columns) {
//        JTextField f = new JTextField(columns);
//        f.setCaretColor(Theme.TEXT);
//        f.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
//        return f;
//    }
//
//    public static JComboBox<String> combo() {
//        JComboBox<String> c = new JComboBox<>();
//        style(c);
//        return c;
//    }
//
//    /** Dark flat drop-down (the default Nimbus one is light gray and clashes with the dark theme). */
//    public static <T> void style(JComboBox<T> c) {
//        c.setUI(new BasicComboBoxUI() {
//            @Override
//            protected JButton createArrowButton() {
//                JButton b = new JButton() {
//                    @Override
//                    protected void paintComponent(Graphics g0) {
//                        Graphics2D g = aa(g0);
//                        g.setColor(Theme.MUTED);
//                        int cx = getWidth() / 2, cy = getHeight() / 2;
//                        g.fillPolygon(new int[]{cx - 5, cx + 5, cx}, new int[]{cy - 2, cy - 2, cy + 3}, 3);
//                        g.dispose();
//                    }
//                };
//                b.setBorder(BorderFactory.createEmptyBorder());
//                b.setContentAreaFilled(false);
//                b.setFocusPainted(false);
//                b.setPreferredSize(new Dimension(24, 20));
//                return b;
//            }
//
//            @Override
//            public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean hasFocus) {
//                g.setColor(Theme.PANEL_ALT);
//                g.fillRect(r.x, r.y, r.width, r.height);
//            }
//        });
//        c.setOpaque(true);
//        c.setBackground(Theme.PANEL_ALT);
//        c.setForeground(Theme.TEXT);
//        c.setFont(Theme.font(Font.PLAIN, 12.5f));
//        c.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
//        c.setRenderer(new DefaultListCellRenderer() {
//            @Override
//            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
//                super.getListCellRendererComponent(list, value, index, sel, false);
//                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
//                setOpaque(true);
//                setForeground(sel && index >= 0 ? Color.WHITE : Theme.TEXT);
//                setBackground(sel && index >= 0 ? Theme.ACCENT_DARK : Theme.PANEL_ALT);
//                return this;
//            }
//        });
//    }
//
//    /** In a vertical BoxLayout every child must share one alignment, or they drift to different x positions. */
//    public static <T extends JComponent> T alignLeft(T box) {
//        for (Component c : box.getComponents()) {
//            if (c instanceof JComponent j) j.setAlignmentX(Component.LEFT_ALIGNMENT);
//        }
//        return box;
//    }
//
//    // ---------------------------------------------------------------- cards and layout
//
//    /** Rounded panel with a title, like the four panels on the dashboard mockup. */
//    public static final class Card extends JPanel {
//        public Card(String title, JComponent content) {
//            super(new BorderLayout(0, 10));
//            setOpaque(false);
//            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
//            if (title != null) {
//                JLabel t = label(title, Font.BOLD, 14f, Theme.TEXT);
//                add(t, BorderLayout.NORTH);
//            }
//            content.setOpaque(false);
//            add(content, BorderLayout.CENTER);
//        }
//
//        @Override
//        protected void paintComponent(Graphics g0) {
//            Graphics2D g = aa(g0);
//            g.setColor(Theme.PANEL);
//            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
//            g.setColor(Theme.BORDER);
//            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
//            g.dispose();
//            super.paintComponent(g0);
//        }
//    }
//
//    public static JPanel page() {
//        JPanel p = new JPanel(new BorderLayout(16, 16));
//        p.setBackground(Theme.BG);
//        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
//        return p;
//    }
//
//    public static JPanel row(int gap, Component... items) {
//        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
//        p.setOpaque(false);
//        p.setBorder(BorderFactory.createEmptyBorder(0, -gap, 0, 0));   // FlowLayout adds a leading gap; cancel it
//        for (Component c : items) p.add(c);
//        return p;
//    }
//
//    /** Label + field rows in two columns. */
//    public static final class Form extends JPanel {
//        private int row;
//
//        public Form() {
//            super(new GridBagLayout());
//            setOpaque(false);
//        }
//
//        public Form row(String label, JComponent c) {
//            GridBagConstraints g = new GridBagConstraints();
//            g.gridx = 0;
//            g.gridy = row;
//            g.anchor = GridBagConstraints.WEST;
//            g.insets = new Insets(4, 0, 4, 10);
//            add(muted(label), g);
//            g.gridx = 1;
//            g.weightx = 1;
//            g.fill = GridBagConstraints.HORIZONTAL;
//            g.insets = new Insets(4, 0, 4, 0);
//            add(c, g);
//            row++;
//            return this;
//        }
//
//        public Form full(JComponent c) {
//            GridBagConstraints g = new GridBagConstraints();
//            g.gridx = 0;
//            g.gridy = row;
//            g.gridwidth = 2;
//            g.weightx = 1;
//            g.fill = GridBagConstraints.HORIZONTAL;
//            g.insets = new Insets(4, 0, 4, 0);
//            add(c, g);
//            row++;
//            return this;
//        }
//    }
//
//    public static Icon dot(Color c) {
//        return new Icon() {
//            @Override
//            public void paintIcon(Component comp, Graphics g0, int x, int y) {
//                Graphics2D g = aa(g0);
//                g.setColor(c);
//                g.fillOval(x, y, 11, 11);
//                g.dispose();
//            }
//
//            @Override
//            public int getIconWidth() {
//                return 11;
//            }
//
//            @Override
//            public int getIconHeight() {
//                return 11;
//            }
//        };
//    }
//
//    public static JPanel legend(String[] names, Color[] colors) {
//        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
//        p.setOpaque(false);
//        p.setBorder(BorderFactory.createEmptyBorder(0, -14, 0, 0));
//        for (int i = 0; i < names.length; i++) {
//            JLabel l = label(names[i], Font.PLAIN, 12f, Theme.MUTED);
//            l.setIcon(dot(colors[i]));
//            l.setIconTextGap(6);
//            p.add(l);
//        }
//        return p;
//    }
//
//    // ---------------------------------------------------------------- tables
//
//    public static void styleTable(JTable t) {
//        t.setRowHeight(30);
//        t.setShowGrid(false);
//        t.setIntercellSpacing(new Dimension(0, 0));
//        t.setFillsViewportHeight(true);
//        t.setFont(Theme.font(Font.PLAIN, 13f));
//        t.setForeground(Theme.TEXT);
//        t.setBackground(Theme.PANEL_ALT);
//        t.setSelectionBackground(Theme.ACCENT_DARK);
//        t.setSelectionForeground(Color.WHITE);
//        t.setFocusable(true);
//        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
//                super.getTableCellRendererComponent(tb, v, sel, false, row, col);
//                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
//                setOpaque(true);
//                setForeground(sel ? Color.WHITE : Theme.TEXT);
//                setBackground(sel ? Theme.ACCENT_DARK : (row % 2 == 0 ? Theme.PANEL_ALT : Theme.PANEL));
//                return this;
//            }
//        };
//        t.setDefaultRenderer(Object.class, cell);
//        t.getTableHeader().setReorderingAllowed(false);
//        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
//                super.getTableCellRendererComponent(tb, v, false, false, row, col);
//                setBorder(BorderFactory.createCompoundBorder(
//                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
//                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
//                setOpaque(true);
//                setBackground(Theme.SIDEBAR);
//                setForeground(Theme.MUTED);
//                setFont(Theme.font(Font.BOLD, 12f));
//                return this;
//            }
//        });
//        t.getTableHeader().setPreferredSize(new Dimension(100, 32));
//    }
//
//    public static JScrollPane scroll(Component c) {
//        JScrollPane sp = new JScrollPane(c);
//        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
//        sp.getViewport().setBackground(Theme.PANEL_ALT);
//        sp.getVerticalScrollBar().setUnitIncrement(16);
//        sp.getHorizontalScrollBar().setUnitIncrement(16);
//        return sp;
//    }
//}


package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;

/** Small helpers so every page looks the same. */
public final class Ui {

    private Ui() {
    }

    public enum Kind {PRIMARY, GHOST, DANGER}

    public static Graphics2D aa(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    // ---------------------------------------------------------------- buttons

    private static final class Btn extends JButton {
        private final Kind kind;
        private boolean hover;

        Btn(String text, Kind kind, boolean small) {
            super(text);
            this.kind = kind;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(Theme.TEXT);
            setFont(Theme.font(Font.BOLD, small ? 11.5f : 12.5f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(small ? BorderFactory.createEmptyBorder(5, 11, 5, 11) : BorderFactory.createEmptyBorder(8, 16, 8, 16));
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

        @Override
        public void setEnabled(boolean b) {
            super.setEnabled(b);
            setForeground(b ? Theme.TEXT : Theme.MUTED);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            Color base = switch (kind) {
                case PRIMARY -> Theme.ACCENT_DARK;
                case DANGER -> new Color(0x9F2D2D);
                case GHOST -> new Color(0x3A1F5C);
            };
            if (!isEnabled()) base = Theme.BORDER;
            else if (hover) base = base.brighter();
            g.setColor(base);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            if (kind == Kind.GHOST) {
                g.setColor(Theme.BORDER);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    public static JButton button(String text, Kind kind) {
        return new Btn(text, kind, false);
    }

    public static JButton button(String text) {
        return new Btn(text, Kind.GHOST, false);
    }

    /** Smaller button for crowded panels such as the dashboard. */
    public static JButton small(String text) {
        return new Btn(text, Kind.GHOST, true);
    }

    // ---------------------------------------------------------------- text

    public static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.font(style, size));
        l.setForeground(color);
        return l;
    }

    public static JLabel muted(String text) {
        return label(text, Font.PLAIN, 12f, Theme.MUTED);
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        f.setCaretColor(Theme.TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    public static JComboBox<String> combo() {
        JComboBox<String> c = new JComboBox<>();
        style(c);
        return c;
    }

    /** Dark flat drop-down (the default Nimbus one is light gray and clashes with the dark theme). */
    public static <T> void style(JComboBox<T> c) {
        c.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g0) {
                        Graphics2D g = aa(g0);
                        g.setColor(Theme.MUTED);
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g.fillPolygon(new int[]{cx - 5, cx + 5, cx}, new int[]{cy - 2, cy - 2, cy + 3}, 3);
                        g.dispose();
                    }
                };
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setPreferredSize(new Dimension(24, 20));
                return b;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle r, boolean hasFocus) {
                g.setColor(Theme.PANEL_ALT);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
        c.setOpaque(true);
        c.setBackground(Theme.PANEL_ALT);
        c.setForeground(Theme.TEXT);
        c.setFont(Theme.font(Font.PLAIN, 12.5f));
        c.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, false);
                setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                setOpaque(true);
                setForeground(sel && index >= 0 ? Color.WHITE : Theme.TEXT);
                setBackground(sel && index >= 0 ? Theme.ACCENT_DARK : Theme.PANEL_ALT);
                return this;
            }
        });
    }

    /** In a vertical BoxLayout every child must share one alignment, or they drift to different x positions. */
    public static <T extends JComponent> T alignLeft(T box) {
        for (Component c : box.getComponents()) {
            if (c instanceof JComponent j) j.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        return box;
    }

    // ---------------------------------------------------------------- cards and layout

    /** Rounded translucent panel with a title. The background image shows through it. */
    public static final class Card extends JPanel {
        public Card(String title, JComponent content) {
            super(new BorderLayout(0, 10));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            if (title != null) {
                JLabel t = label(title, Font.BOLD, 14f, Theme.TEXT);
                add(t, BorderLayout.NORTH);
            }
            content.setOpaque(false);
            add(content, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = aa(g0);
            g.setColor(Theme.CARD);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g.setColor(Theme.CARD_EDGE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Transparent page container: the BackgroundPanel behind it shows through. */
    public static JPanel page() {
        JPanel p = new JPanel(new BorderLayout(16, 16));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));
        return p;
    }

    public static JPanel row(int gap, Component... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, -gap, 0, 0));   // FlowLayout adds a leading gap; cancel it
        for (Component c : items) p.add(c);
        return p;
    }

    /** Label + field rows in two columns. */
    public static final class Form extends JPanel {
        private int row;

        public Form() {
            super(new GridBagLayout());
            setOpaque(false);
        }

        public Form row(String label, JComponent c) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = row;
            g.anchor = GridBagConstraints.WEST;
            g.insets = new Insets(4, 0, 4, 10);
            add(muted(label), g);
            g.gridx = 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(4, 0, 4, 0);
            add(c, g);
            row++;
            return this;
        }

        public Form full(JComponent c) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = row;
            g.gridwidth = 2;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(4, 0, 4, 0);
            add(c, g);
            row++;
            return this;
        }
    }

    public static Icon dot(Color c) {
        return new Icon() {
            @Override
            public void paintIcon(Component comp, Graphics g0, int x, int y) {
                Graphics2D g = aa(g0);
                g.setColor(c);
                g.fillOval(x, y, 11, 11);
                g.dispose();
            }

            @Override
            public int getIconWidth() {
                return 11;
            }

            @Override
            public int getIconHeight() {
                return 11;
            }
        };
    }

    public static JPanel legend(String[] names, Color[] colors) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, -14, 0, 0));
        for (int i = 0; i < names.length; i++) {
            JLabel l = label(names[i], Font.PLAIN, 12f, Theme.MUTED);
            l.setIcon(dot(colors[i]));
            l.setIconTextGap(6);
            p.add(l);
        }
        return p;
    }

    // ---------------------------------------------------------------- tables

    public static void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setFont(Theme.font(Font.PLAIN, 13f));
        t.setForeground(Theme.TEXT);
        t.setBackground(Theme.PANEL_ALT);
        t.setSelectionBackground(Theme.ACCENT_DARK);
        t.setSelectionForeground(Color.WHITE);
        t.setFocusable(true);
        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, sel, false, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setOpaque(true);
                setForeground(sel ? Color.WHITE : Theme.TEXT);
                setBackground(sel ? Theme.ACCENT_DARK : (row % 2 == 0 ? Theme.PANEL_ALT : Theme.PANEL));
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, cell);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, false, false, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                setOpaque(true);
                setBackground(Theme.SIDEBAR);
                setForeground(Theme.MUTED);
                setFont(Theme.font(Font.BOLD, 12f));
                return this;
            }
        });
        t.getTableHeader().setPreferredSize(new Dimension(100, 32));
    }

    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.getViewport().setBackground(Theme.PANEL_ALT);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        return sp;
    }
}