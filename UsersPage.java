package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Role;
import lspu_hnn_core.User;

/** Admin only: add accounts, reset passwords, enable / disable and delete. */
public class UsersPage extends JPanel implements Refreshable {

    private final AppModel model;
    private final DefaultTableModel rows = new DefaultTableModel(new String[]{"Username", "Name", "Role", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(rows);
    private final JLabel status = Ui.muted(" ");

    public UsersPage(AppModel model) {
        super(new BorderLayout(16, 16));
        this.model = model;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        Ui.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton add = Ui.button("Add user", Ui.Kind.PRIMARY);
        JButton reset = Ui.button("Reset password");
        JButton toggle = Ui.button("Enable / disable");
        JButton del = Ui.button("Delete user", Ui.Kind.DANGER);
        add.addActionListener(e -> addUser());
        reset.addActionListener(e -> withSelected(this::resetPassword));
        toggle.addActionListener(e -> withSelected(this::toggle));
        del.addActionListener(e -> withSelected(this::delete));

        JPanel box = new JPanel(new BorderLayout(0, 10));
        box.setOpaque(false);
        box.add(Ui.scroll(table), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(Ui.row(8, add, reset, toggle, del), BorderLayout.WEST);
        south.add(status, BorderLayout.EAST);
        box.add(south, BorderLayout.SOUTH);
        add(new Ui.Card("Manage Accounts", box), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        rows.setRowCount(0);
        for (User u : model.auth.users()) {
            rows.addRow(new Object[]{u.username(), u.displayName(), u.role().label(), u.active() ? "Active" : "Disabled"});
        }
    }

    private void withSelected(java.util.function.Consumer<String> action) {
        int r = table.getSelectedRow();
        if (r < 0) {
            status.setText("Select an account in the table first.");
            return;
        }
        action.accept((String) rows.getValueAt(r, 0));
    }

    private void run(Runnable r, String ok) {
        try {
            r.run();
            status.setText(ok);
        } catch (RuntimeException ex) {
            status.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot do that", JOptionPane.WARNING_MESSAGE);
        }
        refresh();
    }

    private void addUser() {
        JTextField user = Ui.field(14), name = Ui.field(14);
        JPasswordField pw = new JPasswordField(14), pw2 = new JPasswordField(14);
        JComboBox<String> role = Ui.combo();
        role.addItem("User");
        role.addItem("Admin");
        JPanel form = new Ui.Form().row("Username", user).row("Full name", name).row("Password", pw).row("Repeat password", pw2).row("Role", role);
        while (true) {
            int ok = JOptionPane.showConfirmDialog(this, form, "Add account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;
            if (!new String(pw.getPassword()).equals(new String(pw2.getPassword()))) {
                JOptionPane.showMessageDialog(this, "The two passwords do not match.", "Check the password", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            try {
                model.addUser(user.getText(), name.getText(), pw.getPassword(), role.getSelectedIndex() == 1 ? Role.ADMIN : Role.USER);
                status.setText("Added " + user.getText().trim() + ".");
                refresh();
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot add account", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void resetPassword(String username) {
        JPasswordField pw = new JPasswordField(14), pw2 = new JPasswordField(14);
        JPanel form = new Ui.Form().row("New password", pw).row("Repeat", pw2);
        while (true) {
            int ok = JOptionPane.showConfirmDialog(this, form, "Reset password for " + username, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;
            if (!new String(pw.getPassword()).equals(new String(pw2.getPassword()))) {
                JOptionPane.showMessageDialog(this, "The two passwords do not match.", "Check the password", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            try {
                model.resetPassword(username, pw.getPassword());
                status.setText("Password changed for " + username + ".");
                return;
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot change password", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void toggle(String username) {
        boolean active = model.auth.users().stream().filter(u -> u.username().equals(username)).findFirst().map(User::active).orElse(true);
        run(() -> model.setUserActive(username, !active), username + (active ? " disabled." : " enabled."));
    }

    private void delete(String username) {
        int ok = JOptionPane.showConfirmDialog(this, "Delete the account \"" + username + "\"?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) run(() -> model.deleteUser(username), "Deleted " + username + ".");
    }
}


