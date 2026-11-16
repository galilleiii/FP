package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lspu_hnn_core.AppModel;

/**
 * The four-panel dashboard from the project mockup: heritage BST, campus map, graph representations,
 * and the BFS vs DFS comparison. Admins also get Add / Delete buttons on the tree panel.
 */
public class DashboardPage extends JPanel {

    private final AppModel model;
    private final TreePanel tree;
    private final BstController bst;
    private final GraphCanvas canvas;
    private final FrontierPanel frontier = new FrontierPanel(true);
    private final RouteRunner runner;
    private final ComparisonPanel comparison;
    private final JLabel treeStatus = Ui.muted(" "), mapStatus = Ui.muted(" ");
    private final JComboBox<String> origin = Ui.combo(), dest = Ui.combo();
    private final JTextField query = Ui.field(9);

    public DashboardPage(AppModel model) {
        super(new GridLayout(2, 2, 16, 16));
        this.model = model;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        tree = new TreePanel(model, true);
        bst = new BstController(model, tree, s -> treeStatus.setText(s.trim()), () -> 650);
        tree.onNodeClick(tree::setSelected);

        canvas = new GraphCanvas(model, true);
        runner = new RouteRunner(model, canvas, frontier, s -> mapStatus.setText(s.trim()), () -> 650);
        comparison = new ComparisonPanel(model, true);

        add(new Ui.Card("Heritage Events BST", treePanel()));
        add(new Ui.Card("Campus Network Map", mapPanel()));
        add(new Ui.Card("Graph Representations", reps()));
        add(new Ui.Card("BFS vs DFS Comparison", comparison));

        model.addListener(this::refreshCombos);
        refreshCombos();
    }

    // ---------------------------------------------------------------- top-left

    private JPanel treePanel() {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(false);
        box.add(tree, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));

        JButton search = Ui.small("Search");
        search.addActionListener(e -> bst.search(query.getText()));
        query.addActionListener(e -> bst.search(query.getText()));
        query.setToolTipText("Type a year (1957) or a keyword (university)");
        JPanel r1 = Ui.row(6, Ui.muted("Find"), query, search);
        if (model.isAdmin()) {
            JButton add = Ui.small("Add event");
            JButton del = Ui.small("Delete selected");
            add.addActionListener(e -> addEvent());
            del.addActionListener(e -> deleteSelected());
            r1.add(add);
            r1.add(del);
        }
        south.add(r1);

        JButton pre = Ui.small("Pre-order"), in = Ui.small("In-order"), post = Ui.small("Post-order");
        pre.addActionListener(e -> bst.traverse("Pre-order", model.bst.preorder()));
        in.addActionListener(e -> bst.traverse("In-order", model.bst.inorder()));
        post.addActionListener(e -> bst.traverse("Post-order", model.bst.postorder()));
        south.add(Ui.row(6, pre, in, post));
        south.add(treeStatus);
        box.add(Ui.alignLeft(south), BorderLayout.SOUTH);
        return box;
    }

    private void addEvent() {
        JTextField y = Ui.field(8), t = Ui.field(24);
        JPanel form = new Ui.Form().row("Year", y).row("Title", t);
        while (true) {
            int ok = JOptionPane.showConfirmDialog(this, form, "Add heritage event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;
            try {
                bst.insert(Integer.parseInt(y.getText().trim()), t.getText().trim());
                return;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Year must be a whole number, like 1957.", "Check the year", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void deleteSelected() {
        treeStatus.setText("Click a node in the tree first, then press Delete selected.");
        Integer k = selectedKey();
        if (k == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Delete the event for " + k + "?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            bst.delete(k);
            tree.setSelected(null);
        }
    }

    private Integer selectedKey() {
        return tree.selected();
    }

    // ---------------------------------------------------------------- top-right

    private JPanel mapPanel() {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(false);
        box.add(canvas, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        JButton bfs = Ui.small("Run BFS"), dfs = Ui.small("Run DFS"), reset = Ui.small("Reset");
        bfs.addActionListener(e -> run(true));
        dfs.addActionListener(e -> run(false));
        reset.addActionListener(e -> {
            runner.reset();
            mapStatus.setText(" ");
        });
        JPanel r1 = Ui.row(6, Ui.muted("From"), origin, Ui.muted("To"), dest);
        south.add(r1);
        south.add(Ui.row(6, bfs, dfs, reset));
        south.add(frontier);
        south.add(mapStatus);
        box.add(Ui.alignLeft(south), BorderLayout.SOUTH);
        return box;
    }

    private void run(boolean isBfs) {
        runner.run(isBfs, (String) origin.getSelectedItem(), (String) dest.getSelectedItem(), comparison::set);
    }

    private void refreshCombos() {
        runner.reset();
        fill(origin, 0);
        fill(dest, model.graph.nodes().size() - 1);
    }

    private void fill(JComboBox<String> c, int idx) {
        Object sel = c.getSelectedItem();
        c.removeAllItems();
        for (String n : model.graph.nodes()) c.addItem(n);
        if (sel != null && model.graph.hasNode((String) sel)) c.setSelectedItem(sel);
        else if (c.getItemCount() > 0) c.setSelectedIndex(Math.max(0, Math.min(idx, c.getItemCount() - 1)));
    }

    // ---------------------------------------------------------------- bottom-left

    private JPanel reps() {
        JPanel box = new JPanel(new GridLayout(2, 1, 0, 8));
        box.setOpaque(false);
        box.add(labelled("Adjacency matrix (row = from, INF = none)", new MatrixPanel(model, true)));
        box.add(labelled("Adjacency list", new AdjListPanel(model, true)));
        return box;
    }

    private static JPanel labelled(String title, javax.swing.JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(Ui.muted(title), BorderLayout.NORTH);
        p.add(Ui.scroll(c), BorderLayout.CENTER);
        return p;
    }
}
