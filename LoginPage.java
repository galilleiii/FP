//package lspu_hnn_ui;
//
//import javax.swing.ImageIcon;
//import java.awt.Image;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.FontMetrics;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Insets;
//import java.util.Arrays;
//
//import javax.swing.BorderFactory;
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComponent;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//import javax.swing.JPasswordField;
//import javax.swing.JTextField;
//import javax.swing.SwingConstants;
//import javax.swing.Timer;
//
//import lspu_hnn_core.Auth;
//import lspu_hnn_core.AppModel;
//
///** First screen: username and password. The saved role decides which dashboard opens. */
//public class LoginPage extends JPanel {
//
//    private final AppModel model;
//    private final Runnable onSuccess;
//    private final JTextField username = Ui.field(20);
//    private final JPasswordField password = new JPasswordField(20);
//    private final JCheckBox show = new JCheckBox("Show password");
//    private final JLabel error = Ui.label(" ", Font.PLAIN, 12.5f, Theme.RED);
//    private final JButton login = Ui.button("Login", Ui.Kind.PRIMARY);
//    private final Timer lockTimer;
//    private final char defaultEcho;
//
//    public LoginPage(AppModel model, Runnable onSuccess) {
//        super(new GridBagLayout());
//        this.model = model;
//        this.onSuccess = onSuccess;
//        setBackground(Theme.BG);
//
//        password.setCaretColor(Theme.TEXT);
//        password.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(Theme.BORDER), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
//        defaultEcho = password.getEchoChar();
//        show.setOpaque(false);
//        show.setForeground(Theme.MUTED);
//        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : defaultEcho));
//
//        username.addActionListener(e -> password.requestFocusInWindow());
//        password.addActionListener(e -> attempt());
//        login.addActionListener(e -> attempt());
//        lockTimer = new Timer(500, e -> updateLock());
//
//        add(card());
//    }
//
//    private JComponent card() {
//    	
//        JPanel card = new JPanel() {
//            @Override
//            protected void paintComponent(Graphics g0) {
//                Graphics2D g = Ui.aa(g0);
//                g.setColor(Theme.PANEL);
//                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
//                g.setColor(Theme.BORDER);
//                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
//                g.dispose();
//            }
//        };
//        card.setOpaque(false);
//        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
//        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 26, 40));
//        card.setPreferredSize(new Dimension(440, 540));
//
//        JPanel logo = new JPanel() {
//            @Override
//            protected void paintComponent(Graphics g0) {
//                Graphics2D g = Ui.aa(g0);
//                int d = 72, x = (getWidth() - d) / 2;
//                g.setColor(Theme.ACCENT_DARK);
//                g.fillOval(x, 2, d, d);
//                g.setColor(Theme.ACCENT);
//                g.setStroke(new java.awt.BasicStroke(3f));
//                g.drawOval(x, 2, d, d);
//                g.setColor(Color.WHITE);
//                g.setFont(Theme.font(Font.BOLD, 20f));
//                FontMetrics fm = g.getFontMetrics();
//                g.drawString("LSPU", (getWidth() - fm.stringWidth("LSPU")) / 2, 2 + d / 2 + (fm.getAscent() - fm.getDescent()) / 2);
//                g.dispose();
//            }
//        };
//        logo.setOpaque(false);
//        logo.setAlignmentX(LEFT_ALIGNMENT);
//        logo.setPreferredSize(new Dimension(100, 80));
//        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
//        card.add(logo);
//
//        card.add(center(Ui.label("LSPU Heritage & Campus Navigator", Font.BOLD, 17f, Theme.TEXT)));
//        card.add(vgap(4));
//        card.add(center(Ui.muted("Interactive HNN · Laguna State Polytechnic University")));
//        card.add(vgap(26));
//
//        card.add(left(Ui.label("Username", Font.BOLD, 12f, Theme.MUTED)));
//        card.add(vgap(4));
//        card.add(stretch(username));
//        card.add(vgap(14));
//        card.add(left(Ui.label("Password", Font.BOLD, 12f, Theme.MUTED)));
//        card.add(vgap(4));
//        card.add(stretch(password));
//        card.add(vgap(6));
//        card.add(left(show));
//        card.add(vgap(8));
//
//        error.setHorizontalAlignment(SwingConstants.CENTER);
//        error.setPreferredSize(new Dimension(300, 36));
//        error.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
//        card.add(stretch(error));
//        card.add(vgap(8));
//        login.setAlignmentX(LEFT_ALIGNMENT);
//        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
//        card.add(login);
//        card.add(vgap(22));
//        card.add(center(Ui.muted("ITEC 104 · Final Project")));
//        return card;
//    }
//
//    private static JComponent center(JLabel c) {
//        c.setHorizontalAlignment(SwingConstants.CENTER);
//        c.setAlignmentX(LEFT_ALIGNMENT);
//        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
//        return c;
//    }
//
//    private static JComponent left(JComponent c) {
//        c.setAlignmentX(LEFT_ALIGNMENT);
//        JPanel p = new JPanel(new BorderLayout());
//        p.setOpaque(false);
//        p.setAlignmentX(LEFT_ALIGNMENT);
//        p.add(c, BorderLayout.WEST);
//        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 2));
//        return p;
//    }
//
//    private static JComponent stretch(JComponent c) {
//        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(c.getPreferredSize().height, 34)));
//        c.setAlignmentX(LEFT_ALIGNMENT);
//        return c;
//    }
//
//    private static JComponent vgap(int h) {
//        return (JComponent) javax.swing.Box.createVerticalStrut(h);
//    }
//
//    // ---------------------------------------------------------------- behavior
//
//    /** Called by the shell every time the login screen is shown. */
//    public void reset() {
//        lockTimer.stop();
//        username.setText("");
//        password.setText("");
//        show.setSelected(false);
//        password.setEchoChar(defaultEcho);
//        error.setText(" ");
//        login.setEnabled(true);
//        username.requestFocusInWindow();
//    }
//
//    private void attempt() {
//        if (!login.isEnabled()) return;
//        String user = username.getText().trim();
//        char[] pw = password.getPassword();
//        try {
//            if (user.isEmpty() || pw.length == 0) {
//                error.setText("Enter your username and password");
//                return;
//            }
//            model.login(user, pw);
//            error.setText(" ");
//            onSuccess.run();
//        } catch (Auth.AuthException ex) {
//            password.setText("");
//            error.setText("<html><div style='text-align:center'>" + ex.getMessage() + "</div></html>");
//            if (ex.getMessage().startsWith("Too many")) {
//                login.setEnabled(false);
//                lockTimer.start();
//            } else {
//                password.requestFocusInWindow();
//            }
//        } finally {
//            Arrays.fill(pw, '\0');
//        }
//    }
//
//    private void updateLock() {
//        long left = model.auth.lockRemainingMs(username.getText());
//        if (left <= 0) {
//            lockTimer.stop();
//            login.setEnabled(true);
//            error.setText(" ");
//        } else {
//            error.setText("Too many attempts. Try again in " + ((left + 999) / 1000) + " seconds.");
//        }
//    }
//}
//
//


package lspu_hnn_ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Auth;
import lspu_hnn_core.Role;

/**
 * Sign in and Sign up on one screen, drawn over src/images/Student.png.
 * The shell keeps using it the same way: new LoginPage(model, onSuccess) and reset().
 */
public class LoginPage extends BackgroundPanel {

    private static final String SIGN_IN = "in";
    private static final String SIGN_UP = "up";
    private static final int HERO_MIN_WIDTH = 1000;      // below this the big text on the left is hidden
    private static final int CARD_WIDTH = 400;
    private static final String[] CAMPUSES = {"Siniloan", "Sta. Cruz", "Los Ba\u00f1os", "San Pablo"};

    private final AppModel model;
    private final Runnable onSuccess;

    private final CardLayout cards = new CardLayout();
    private final CardLayout heroes = new CardLayout();
    private final JPanel cardHolder = new JPanel(cards);
    private final JPanel heroHolder = new JPanel(heroes);

    // sign in
    private final AuthUi.Pill studentPill = new AuthUi.Pill("STUDENT", true);
    private final AuthUi.Pill adminPill = new AuthUi.Pill("ADMINISTRATOR", true);
    private final AuthUi.Field idField = new AuthUi.Field("you@lspu.edu.ph");
    private final AuthUi.Password pwField = new AuthUi.Password("Enter your password");
    private final JLabel signInMsg = new JLabel(" ");
    private final AuthUi.Pill signInBtn = new AuthUi.Pill("SIGN IN", false);
    private final Timer lockTimer;
    private Role role = Role.USER;                       // Student is the default tab

    // sign up
    private final AuthUi.Field suName = new AuthUi.Field("Your full name");
    private final AuthUi.Field suStudentId = new AuthUi.Field("Student ID");
    private final AuthUi.Field suEmail = new AuthUi.Field("you@lspu.edu.ph");
    private final AuthUi.Combo suCampus = new AuthUi.Combo(CAMPUSES);
    private final AuthUi.Field suLogin = new AuthUi.Field("you@lspu.edu.ph");
    private final AuthUi.Password suPw = new AuthUi.Password("Enter your password");
    private final AuthUi.Password suConfirm = new AuthUi.Password("Enter your password");
    private final JLabel signUpMsg = new JLabel(" ");
    private final AuthUi.Pill createBtn = new AuthUi.Pill("CREATE ACCOUNT", false);

    public LoginPage(AppModel model, Runnable onSuccess) {
        super(new GridBagLayout());
        this.model = model;
        this.onSuccess = onSuccess;

        for (JLabel m : new JLabel[]{signInMsg, signUpMsg}) {
            m.setFont(AuthUi.font(Font.PLAIN, 10.5f));
            m.setHorizontalAlignment(SwingConstants.CENTER);
        }

        cardHolder.setOpaque(false);
        cardHolder.add(centered(card(signInForm())), SIGN_IN);
        cardHolder.add(centered(card(signUpForm())), SIGN_UP);

        heroHolder.setOpaque(false);
        heroHolder.add(heroWrap(hero("Explore LSPU\u2019s history<br>and campus network.",
                "Build binary search trees of heritage events and trace routes<br>"
                        + "across Siniloan, Sta. Cruz, Los Ba\u00f1os and San Pablo.")), SIGN_IN);
        heroHolder.add(heroWrap(hero("Create your explorer<br>account.",
                "Save routes, bookmark heritage events and compare BFS<br>and DFS paths between campuses.")), SIGN_UP);

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.weightx = 1;
        left.weighty = 1;
        left.fill = GridBagConstraints.BOTH;
        left.insets = new Insets(0, 60, 0, 10);
        add(heroHolder, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.weighty = 1;
        right.fill = GridBagConstraints.BOTH;
        right.insets = new Insets(10, 20, 10, 90);
        add(cardHolder, right);

        lockTimer = new Timer(500, e -> updateLock());
        wireActions();
        setActiveRole(Role.USER);
    }

    // ---------------------------------------------------------------- building blocks

    private static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JComponent card(JComponent form) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = Ui.aa(g0);
                g.setColor(AuthUi.CARD);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g.setColor(AuthUi.CARD_EDGE);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
                g.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));
        card.add(form, BorderLayout.CENTER);
        form.setPreferredSize(new Dimension(CARD_WIDTH - 56, form.getPreferredSize().height));
        return card;
    }

    /** The card keeps its own height; the CardLayout would otherwise stretch both cards to the taller one. */
    private static JComponent centered(JComponent c) {
        JPanel w = new JPanel(new GridBagLayout());
        w.setOpaque(false);
        w.add(c);
        return w;
    }

    private static JComponent heroWrap(JComponent hero) {
        JPanel w = new JPanel(new GridBagLayout());
        w.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        w.add(hero, c);
        return w;
    }

    private static JComponent hero(String titleHtml, String body) {
        JPanel p = column();
        JLabel title = AuthUi.label("<html>" + titleHtml + "</html>", Font.BOLD, 40f, Color.WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(8));
        JLabel sub = AuthUi.label("<html>" + body + "</html>",
                Font.PLAIN, 12.5f, new Color(255, 255, 255, 215));
        sub.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sub);
        return p;
    }

    private static JComponent left(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.add(c, BorderLayout.WEST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height + 2));
        return p;
    }

    private static JComponent stretch(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private static JComponent centerRow(JComponent... items) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        for (JComponent i : items) p.add(i);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private static JComponent gap(int h) {
        return (JComponent) Box.createVerticalStrut(h);
    }

    private static JComponent fieldLabel(String text) {
        JLabel l = AuthUi.label(text, Font.BOLD, 10.5f, AuthUi.LABEL);
        l.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        return left(l);
    }

    private static JComponent messageSlot(JLabel msg) {
        msg.setPreferredSize(new Dimension(300, 26));
        return stretch(msg);
    }

    private void addField(JPanel form, String label, JComponent input) {
        form.add(fieldLabel(label));
        form.add(gap(3));
        form.add(stretch(input));
        form.add(gap(7));
    }

    private JComponent signInForm() {
        JPanel f = column();
        f.add(left(new AuthUi.PixelTitle("Sign in")));
        f.add(gap(4));
        f.add(left(AuthUi.label("Welcome back. Choose your role to continue.", Font.PLAIN, 10.5f, AuthUi.LABEL)));
        f.add(gap(10));

        studentPill.asToggle();
        adminPill.asToggle();
        JPanel roles = new JPanel(new BorderLayout());
        roles.setOpaque(false);
        roles.setAlignmentX(LEFT_ALIGNMENT);
        roles.add(studentPill, BorderLayout.WEST);
        roles.add(adminPill, BorderLayout.EAST);
        roles.setMaximumSize(new Dimension(Integer.MAX_VALUE, roles.getPreferredSize().height));
        f.add(roles);
        f.add(gap(12));

        addField(f, "ID or Email", idField);
        f.add(fieldLabel("Password"));
        f.add(gap(3));
        f.add(stretch(pwField));
        f.add(gap(5));

        JPanel forgot = new JPanel(new BorderLayout());
        forgot.setOpaque(false);
        forgot.add(AuthUi.link("Forget password?", 10f, this::forgotPassword), BorderLayout.EAST);
        f.add(stretch(forgot));
        f.add(messageSlot(signInMsg));
        f.add(gap(2));
        f.add(centerRow(signInBtn));
        f.add(gap(10));
        f.add(centerRow(AuthUi.label("New here?", Font.PLAIN, 11.5f, Color.WHITE),
                AuthUi.link("Create an account", 11.5f, () -> setView(SIGN_UP))));
        return f;
    }

    private JComponent signUpForm() {
        JPanel f = column();
        f.add(left(new AuthUi.PixelTitle("Sign up")));
        f.add(gap(4));
        f.add(left(AuthUi.label("It takes less than a minute.", Font.PLAIN, 10.5f, AuthUi.LABEL)));
        f.add(gap(10));
        addField(f, "Full name", suName);
        addField(f, "Student ID", suStudentId);
        addField(f, "Institutional email", suEmail);
        addField(f, "Host campus", suCampus);
        addField(f, "Student ID or Email", suLogin);
        addField(f, "Password", suPw);
        addField(f, "Confirm Password", suConfirm);
        f.add(messageSlot(signUpMsg));
        f.add(gap(2));
        f.add(centerRow(createBtn));
        f.add(gap(8));
        f.add(centerRow(AuthUi.label("Already registered?", Font.PLAIN, 11.5f, Color.WHITE),
                AuthUi.link("Sign in", 11.5f, () -> setView(SIGN_IN))));
        return f;
    }

    /** Hide the big text on narrow windows so it never collides with the card. */
    @Override
    public void doLayout() {
        heroHolder.setVisible(getWidth() >= HERO_MIN_WIDTH);
        super.doLayout();
    }

    // ---------------------------------------------------------------- behavior

    private void wireActions() {
        studentPill.addActionListener(e -> setActiveRole(Role.USER));
        adminPill.addActionListener(e -> setActiveRole(Role.ADMIN));
        idField.addActionListener(e -> pwField.requestFocusInWindow());
        pwField.addActionListener(e -> attempt());
        signInBtn.addActionListener(e -> attempt());

        suName.addActionListener(e -> suStudentId.requestFocusInWindow());
        suStudentId.addActionListener(e -> suEmail.requestFocusInWindow());
        suEmail.addActionListener(e -> suLogin.requestFocusInWindow());
        suLogin.addActionListener(e -> suPw.requestFocusInWindow());
        suPw.addActionListener(e -> suConfirm.requestFocusInWindow());
        suConfirm.addActionListener(e -> createAccount());
        createBtn.addActionListener(e -> createAccount());
    }

    private void setActiveRole(Role r) {
        role = r;
        studentPill.setActive(r == Role.USER);
        adminPill.setActive(r == Role.ADMIN);
        showMsg(signInMsg, "", Theme.RED);
    }

    private void setView(String view) {
        cards.show(cardHolder, view);
        heroes.show(heroHolder, view);
        showMsg(signInMsg, "", Theme.RED);
        showMsg(signUpMsg, "", Theme.RED);
        (view.equals(SIGN_IN) ? idField : suName).requestFocusInWindow();
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static void showMsg(JLabel l, String text, Color color) {
        l.setForeground(color);
        l.setText(text.isEmpty() ? " " : "<html><div style='text-align:center;width:300px'>" + esc(text) + "</div></html>");
    }

    /** Called by the shell every time the login screen is shown. */
    public void reset() {
        lockTimer.stop();
        for (AuthUi.Field f : new AuthUi.Field[]{idField, suName, suStudentId, suEmail, suLogin}) f.setText("");
        for (AuthUi.Password p : new AuthUi.Password[]{pwField, suPw, suConfirm}) p.setText("");
        suCampus.setSelectedIndex(0);
        signInBtn.setEnabled(true);
        setActiveRole(Role.USER);
        setView(SIGN_IN);
    }

    private void forgotPassword() {
        JOptionPane.showMessageDialog(this,
                "Passwords are reset by an administrator.\nAsk your admin to open Manage Users and reset it for you.",
                "Forget password?", JOptionPane.INFORMATION_MESSAGE);
    }

    private void attempt() {
        if (!signInBtn.isEnabled()) return;
        String typed = idField.getText().trim();
        char[] pw = pwField.getPassword();
        try {
            if (typed.isEmpty() || pw.length == 0) {
                showMsg(signInMsg, "Enter your ID or email and your password", Theme.RED);
                return;
            }
            model.login(model.resolveLoginId(typed), pw);
            if (model.currentUser().role() != role) {              // wrong tab for this account
                model.logout();
                pwField.setText("");
                showMsg(signInMsg, "This is not " + (role == Role.ADMIN ? "an Administrator" : "a Student")
                        + " account. Pick the other role.", Theme.RED);
                return;
            }
            showMsg(signInMsg, "", Theme.RED);
            onSuccess.run();
        } catch (Auth.AuthException ex) {
            pwField.setText("");
            showMsg(signInMsg, ex.getMessage(), Theme.RED);
            if (ex.getMessage().startsWith("Too many")) {
                signInBtn.setEnabled(false);
                lockTimer.start();
            } else {
                pwField.requestFocusInWindow();
            }
        } finally {
            Arrays.fill(pw, '\0');
        }
    }

    private void updateLock() {
        long left = model.auth.lockRemainingMs(model.resolveLoginId(idField.getText()));
        if (left <= 0) {
            lockTimer.stop();
            signInBtn.setEnabled(true);
            showMsg(signInMsg, "", Theme.RED);
        } else {
            showMsg(signInMsg, "Too many attempts. Try again in " + ((left + 999) / 1000) + " seconds.", Theme.RED);
        }
    }

    private void createAccount() {
        char[] pw = suPw.getPassword();
        char[] confirm = suConfirm.getPassword();
        try {
            if (!Arrays.equals(pw, confirm)) {
                showMsg(signUpMsg, "Passwords do not match", Theme.RED);
                return;
            }
            String loginId = suLogin.getText().trim();
            model.register(suName.getText(), suStudentId.getText(), suEmail.getText(),
                    (String) suCampus.getSelectedItem(), loginId, pw);
            for (AuthUi.Field f : new AuthUi.Field[]{suName, suStudentId, suEmail, suLogin}) f.setText("");
            suPw.setText("");
            suConfirm.setText("");
            setView(SIGN_IN);
            setActiveRole(Role.USER);
            idField.setText(loginId);
            showMsg(signInMsg, "Account created. Sign in to continue.", Theme.GREEN);
            pwField.requestFocusInWindow();
        } catch (IllegalArgumentException ex) {
            showMsg(signUpMsg, ex.getMessage(), Theme.RED);
        } finally {
            Arrays.fill(pw, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
}