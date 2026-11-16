package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.User;

/**
 * The whole window content: the login screen, and after login the sidebar + header + pages.
 * What is built depends on the role (admins get three extra pages and extra buttons on the shared ones).
 */
public class AppShell extends JPanel {

    private static final String LOGIN = "login", MAIN = "main";

    private final AppModel model;
    private final Path dataDir;
    private final CardLayout cards = new CardLayout();
    private final LoginPage login;
    private JPanel main;
    private JPanel mainHolder = new JPanel(new BorderLayout());

    private final Map<String, JComponent> pages = new LinkedHashMap<>();
    private final List<NavButton> navButtons = new ArrayList<>();
    private final JLabel pageTitle = Ui.label(" ", Font.BOLD, 20f, Theme.TEXT);
    private CardLayout pageCards;
    private JPanel pageHolder;

    public AppShell(AppModel model, Path dataDir) {
        super();
        this.model = model;
        this.dataDir = dataDir;
        setLayout(cards);
        setBackground(Theme.BG);
        login = new LoginPage(model, this::showMain);
        add(login, LOGIN);
        add(mainHolder, MAIN);
        cards.show(this, LOGIN);
    }

    public void focusLogin() {
        login.reset();
    }

    /** Names of the pages the current account can open (used by the screenshot tool). */
    public List<String> pageNames() {
        return new ArrayList<>(pages.keySet());
    }

    // ---------------------------------------------------------------- after login

    public void showMain() {
        mainHolder.removeAll();
        pages.clear();
        navButtons.clear();
        boolean admin = model.isAdmin();

        pages.put("Dashboard", new DashboardPage(model));
        pages.put("Heritage Events BST", new BstPage(model));
        pages.put("Campus Network Map", new MapPage(model));
        pages.put("Graph Representations", new RepresentationsPage(model));
        pages.put("Traversal Comparison", new RoutesPage(model));
        pages.put("Historical Timeline", new TimeLinePage(model));
        if (admin) {
            pages.put("Manage Users", new UsersPage(model));
            pages.put("Activity Log", new LogPage(model));
            pages.put("Data & Settings", new SettingsPage(model, dataDir));
        }

        pageCards = new CardLayout();
        pageHolder = new JPanel(pageCards);
        pageHolder.setBackground(Theme.BG);
        for (Map.Entry<String, JComponent> e : pages.entrySet()) pageHolder.add(e.getValue(), e.getKey());

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Theme.BG);
        right.add(header(), BorderLayout.NORTH);
        right.add(pageHolder, BorderLayout.CENTER);

        main = new JPanel(new BorderLayout());
        main.add(sidebar(admin), BorderLayout.WEST);
        main.add(right, BorderLayout.CENTER);
        mainHolder.add(main, BorderLayout.CENTER);
        mainHolder.revalidate();

        open("Dashboard");
        cards.show(this, MAIN);
    }

    private void open(String name) {
        pageCards.show(pageHolder, name);
        pageTitle.setText(name);
        for (NavButton b : navButtons) b.setActive(b.name.equals(name));
        JComponent p = pages.get(name);
        if (p instanceof Refreshable r) r.refresh();
    }

    private void logout() {
        model.logout();
        mainHolder.removeAll();
        pages.clear();
        navButtons.clear();
        cards.show(this, LOGIN);
        login.reset();
    }

    // ---------------------------------------------------------------- header

    private JPanel header() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(Theme.BG);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER), BorderFactory.createEmptyBorder(14, 22, 14, 22)));
        h.add(pageTitle, BorderLayout.WEST);

        User u = model.currentUser();
        String who = u.role().label() + " · " + u.displayName();
        JLabel chip = new JLabel(who) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = Ui.aa(g0);
                g.setColor(model.isAdmin() ? new Color(0x4A2F7A) : new Color(0x1A3D63));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
                super.paintComponent(g0);
            }
        };
        chip.setFont(Theme.font(Font.BOLD, 12.5f));
        chip.setForeground(Color.WHITE);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        chip.setOpaque(false);
        JLabel date = Ui.muted(LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH)));
        JPanel r = new JPanel();
        r.setOpaque(false);
        r.setLayout(new BoxLayout(r, BoxLayout.X_AXIS));
        r.add(date);
        r.add(javax.swing.Box.createHorizontalStrut(14));
        r.add(chip);
        h.add(r, BorderLayout.EAST);
        return h;
    }

    // ---------------------------------------------------------------- sidebar

    private JPanel sidebar(boolean admin) {
        JPanel s = new JPanel();
        s.setBackground(Theme.SIDEBAR);
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        s.setPreferredSize(new Dimension(236, 100));

        JLabel brand = Ui.label("LSPU-HNN", Font.BOLD, 19f, Theme.TEXT);
        JLabel sub = Ui.label("Heritage & Campus Navigator", Font.PLAIN, 11f, Theme.MUTED);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(22, 20, 18, 6));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        top.add(brand);
        top.add(javax.swing.Box.createVerticalStrut(2));
        top.add(sub);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        s.add(top);

        s.add(section("EXPLORE"));
        for (String n : new String[]{"Dashboard", "Heritage Events BST", "Campus Network Map", "Graph Representations",
                "Traversal Comparison", "Historical Timeline"}) {
            s.add(nav(n));
        }
        if (admin) {
            s.add(section("ADMIN"));
            for (String n : new String[]{"Manage Users", "Activity Log", "Data & Settings"}) s.add(nav(n));
        }
        s.add(javax.swing.Box.createVerticalGlue());

        JButton out = Ui.button("Log out");
        out.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Log out of LSPU-HNN?", "Log out", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) logout();
        });
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 16, 18, 16));
        bottom.add(out, BorderLayout.CENTER);
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        s.add(bottom);
        return s;
    }

    private JComponent section(String text) {
        JLabel l = Ui.label(text, Font.BOLD, 11f, Theme.MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(14, 22, 6, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return l;
    }

    private NavButton nav(String name) {
        NavButton b = new NavButton(name);
        b.addActionListener(e -> open(name));
        navButtons.add(b);
        return b;
    }

    private static final class NavButton extends JButton {
        final String name;
        private boolean selected, hover;

        NavButton(String name) {
            super(name);
            this.name = name;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalAlignment(LEFT);
            setFont(Theme.font(Font.PLAIN, 13.5f));
            setForeground(Theme.MUTED);
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
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

        void setActive(boolean s) {
            selected = s;
            setForeground(s ? Color.WHITE : Theme.MUTED);
            setFont(Theme.font(s ? Font.BOLD : Font.PLAIN, 13.5f));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = Ui.aa(g0);
            if (selected || hover) {
                g.setColor(selected ? Theme.ACCENT_DARK : new Color(0x162540));
                g.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 12, 12);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }
}

