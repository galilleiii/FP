package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lspu_hnn_core.AppModel;

/** Module B: the campus network. Admins can add waypoints, edit distances and switch directed / undirected. */
public class MapPage extends JPanel {

    private final AppModel model;
    private final GraphCanvas canvas;
    private final JComboBox<String> from = Ui.combo(), to = Ui.combo(), remove = Ui.combo();
    private final JTextField km = Ui.field(6);
    private final JTextField newCampus = Ui.field(14);
    private final JCheckBox directed = new JCheckBox("Directed edges (one way)");
    private final JLabel status = Ui.muted(" ");
    private boolean refreshing;

    public MapPage(AppModel model) {
        super(new BorderLayout(16, 16));
        this.model = model;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        canvas = new GraphCanvas(model, false);
        JPanel box = new JPanel(new BorderLayout(0, 8));
        box.setOpaque(false);
        box.add(canvas, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(Ui.muted("Drag a campus to move it. Numbers on the lines are distances in km."), BorderLayout.WEST);
        south.add(status, BorderLayout.EAST);
        box.add(south, BorderLayout.SOUTH);
        add(new Ui.Card("Campus Network Map", box), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(350, 100));
        right.add(new Ui.Card("Connectivity Controls", controls()), BorderLayout.NORTH);
        add(right, BorderLayout.EAST);

        model.addListener(this::refresh);
        refresh();
    }

    private JPanel controls() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        if (!model.isAdmin()) {
            JLabel note = Ui.muted("<html>View-only account. Admins can add waypoints, change distances and switch "
                    + "between directed and undirected edges.</html>");
            box.add(note);
            return box;
        }

        box.add(section("Add campus / waypoint"));
        JButton add = Ui.button("Add campus", Ui.Kind.PRIMARY);
        add.addActionListener(e -> run(() -> {
            String name = newCampus.getText().trim();
            if (model.addCampus(name)) {
                status.setText("Added " + name + ". Now connect it below.");
                newCampus.setText("");
            } else {
                status.setText("That campus already exists.");
            }
        }));
        newCampus.addActionListener(e -> add.doClick());
        box.add(new Ui.Form().row("Name", newCampus).full(add));

        box.add(section("Connect / change distance"));
        JButton set = Ui.button("Add / update connection", Ui.Kind.PRIMARY);
        km.setText("20");
        set.addActionListener(e -> run(() -> {
            int dist;
            try {
                dist = Integer.parseInt(km.getText().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Distance must be a whole number of km, like 24");
            }
            model.setEdge((String) from.getSelectedItem(), (String) to.getSelectedItem(), dist);
            status.setText("Saved connection.");
        }));
        JButton del = Ui.button("Remove connection", Ui.Kind.DANGER);
        del.addActionListener(e -> run(() -> {
            boolean ok = model.removeEdge((String) from.getSelectedItem(), (String) to.getSelectedItem());
            status.setText(ok ? "Connection removed." : "Those campuses are not connected.");
        }));
        box.add(new Ui.Form().row("From", from).row("To", to).row("Distance (km)", km).full(set).full(del));

        box.add(section("Edge type"));
        directed.setOpaque(false);
        directed.setForeground(Theme.TEXT);
        directed.addActionListener(e -> {
            if (!refreshing) run(() -> model.setDirected(directed.isSelected()));
        });
        box.add(directed);
        JLabel hint = Ui.muted("<html>Off: every connection works both ways.<br>On: a connection only goes from \"From\" to \"To\".</html>");
        box.add(hint);

        box.add(section("Remove campus"));
        JButton rm = Ui.button("Remove campus", Ui.Kind.DANGER);
        rm.addActionListener(e -> {
            String name = (String) remove.getSelectedItem();
            if (name == null) return;
            int ok = JOptionPane.showConfirmDialog(this, "Remove " + name + " and all its connections?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) run(() -> {
                model.removeCampus(name);
                status.setText("Removed " + name + ".");
            });
        });
        box.add(new Ui.Form().row("Campus", remove).full(rm));
        return Ui.alignLeft(box);
    }

    private JLabel section(String text) {
        JLabel l = Ui.label(text, Font.BOLD, 12f, Theme.ACCENT);
        l.setBorder(BorderFactory.createEmptyBorder(12, 0, 2, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private void run(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException ex) {
            status.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot do that", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refresh() {
        refreshing = true;
        fill(from);
        fill(to);
        fill(remove);
        if (to.getItemCount() > 1 && to.getSelectedIndex() == from.getSelectedIndex()) to.setSelectedIndex(1);
        directed.setSelected(model.graph.isDirected());
        refreshing = false;
    }

    private void fill(JComboBox<String> c) {
        Object sel = c.getSelectedItem();
        c.removeAllItems();
        for (String n : model.graph.nodes()) c.addItem(n);
        if (sel != null) c.setSelectedItem(sel);
    }
}


