package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lspu_hnn_core.AppModel;

/** Module A: build, search, delete and traverse the heritage events BST. */
public class BstPage extends JPanel {

    private final AppModel model;
    private final TreePanel tree;
    private final BstController controller;
    private final JTextArea log = new JTextArea();
    private final JSlider speed = new JSlider(150, 1500, 700);
    private final JTextField year = Ui.field(8), title = Ui.field(18), query = Ui.field(12), delYear = Ui.field(8);
    private final JLabel stats = Ui.muted(" ");

    public BstPage(AppModel model) {
        super(new BorderLayout(16, 16));
        this.model = model;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        tree = new TreePanel(model, false);
        controller = new BstController(model, tree, this::println, () -> 1600 - speed.getValue());
        tree.onNodeClick(k -> {
            tree.setSelected(k);
            delYear.setText(String.valueOf(k));
            year.setText(String.valueOf(k));
        });

        // canvas
        JPanel canvasBox = new JPanel(new BorderLayout(0, 8));
        canvasBox.setOpaque(false);
        canvasBox.add(Ui.scroll(wrap(tree)), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(Ui.legend(
                new String[]{"Default", "Visiting", "Found / output", "Delete target", "Successor"},
                new Color[]{Theme.NODE, Theme.YELLOW, Theme.GREEN, Theme.RED, Theme.ORANGE}), BorderLayout.WEST);
        south.add(stats, BorderLayout.EAST);
        canvasBox.add(south, BorderLayout.SOUTH);
        add(new Ui.Card("Heritage Events – Binary Search Tree", canvasBox), BorderLayout.CENTER);

        // right column
        JPanel right = new JPanel(new BorderLayout(0, 16));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(350, 100));
        right.add(new Ui.Card("Operations", operations()), BorderLayout.NORTH);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(Theme.font(Font.PLAIN, 12.5f));
        log.setBackground(Theme.PANEL_ALT);
        log.setForeground(Theme.TEXT);
        log.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        right.add(new Ui.Card("Log", Ui.scroll(log)), BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        model.addListener(this::updateStats);
        updateStats();
        println("Click a node to select it. Hover a node to read its title.");
    }

    private static JPanel wrap(TreePanel t) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PANEL_ALT);
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    private JPanel operations() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        if (model.isAdmin()) {
            box.add(sectionLabel("Insert event"));
            JButton insert = Ui.button("Insert", Ui.Kind.PRIMARY);
            insert.addActionListener(e -> doInsert());
            box.add(new Ui.Form().row("Year", year).row("Title", title).full(insert));
            title.addActionListener(e -> doInsert());
        } else {
            JLabel note = Ui.muted("<html>View-only account. Ask an admin to add or remove events.</html>");
            note.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            box.add(note);
        }

        box.add(sectionLabel("Search (year or keyword)"));
        JButton search = Ui.button("Search", Ui.Kind.PRIMARY);
        search.addActionListener(e -> controller.search(query.getText()));
        query.addActionListener(e -> controller.search(query.getText()));
        box.add(new Ui.Form().row("Find", query).full(search));

        if (model.isAdmin()) {
            box.add(sectionLabel("Delete event"));
            JButton del = Ui.button("Delete", Ui.Kind.DANGER);
            del.addActionListener(e -> doDelete());
            box.add(new Ui.Form().row("Year", delYear).full(del));
        }

        box.add(sectionLabel("Traversals"));
        JPanel trav = new JPanel(new GridLayout(1, 3, 6, 0));
        trav.setOpaque(false);
        JButton pre = Ui.button("Pre-order"), in = Ui.button("In-order"), post = Ui.button("Post-order");
        pre.addActionListener(e -> controller.traverse("Pre-order", model.bst.preorder()));
        in.addActionListener(e -> controller.traverse("In-order", model.bst.inorder()));
        post.addActionListener(e -> controller.traverse("Post-order", model.bst.postorder()));
        trav.add(pre);
        trav.add(in);
        trav.add(post);
        box.add(trav);

        box.add(sectionLabel("Animation speed"));
        speed.setOpaque(false);
        speed.setInverted(false);
        box.add(speed);
        JPanel sl = new JPanel(new BorderLayout());
        sl.setOpaque(false);
        sl.add(Ui.muted("Slow"), BorderLayout.WEST);
        sl.add(Ui.muted("Fast"), BorderLayout.EAST);
        box.add(sl);

        JButton reset = Ui.button("Reset colors");
        reset.addActionListener(e -> {
            controller.reset();
            println("Reset.");
        });
        JPanel rp = new JPanel(new BorderLayout());
        rp.setOpaque(false);
        rp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        rp.add(reset, BorderLayout.CENTER);
        box.add(rp);
        return Ui.alignLeft(box);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = Ui.label(text, Font.BOLD, 12f, Theme.ACCENT);
        l.setBorder(BorderFactory.createEmptyBorder(10, 0, 2, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // ---------------------------------------------------------------- actions

    private void doInsert() {
        int y;
        try {
            y = Integer.parseInt(year.getText().trim());
        } catch (NumberFormatException ex) {
            println("Year must be a whole number, like 1957.");
            return;
        }
        String t = title.getText().trim();
        if (t.isEmpty()) {
            println("Type a title for the event.");
            return;
        }
        controller.insert(y, t);
        title.setText("");
    }

    private void doDelete() {
        int y;
        try {
            y = Integer.parseInt(delYear.getText().trim());
        } catch (NumberFormatException ex) {
            println("Type the year to delete (or click its node).");
            return;
        }
        if (controller.busy()) {
            println("Wait for the current animation to finish (or press Reset).");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete the event for " + y + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            controller.delete(y);
            tree.setSelected(null);
        }
    }

    private void println(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void updateStats() {
        stats.setText(model.bst.size() + " events · " + model.bst.height() + " levels · "
                + (model.bst.isBalanced() ? "balanced" : "not balanced"));
    }
}
