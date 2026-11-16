package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Event;
import lspu_hnn_core.Sorter;

/** Module D: the historical timeline as a sorted table, with merge, quick and insertion sort. */
public class TimeLinePage extends JPanel implements Refreshable {

    private final AppModel model;
    private final DefaultTableModel events = new DefaultTableModel(new String[]{"Year", "Event"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel stats = new DefaultTableModel(
            new String[]{"", "Merge", "Quick", "Insertion"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JComboBox<Sorter.Algo> algo = new JComboBox<>(Sorter.Algo.values());
    private final JComboBox<String> by = Ui.combo(), order = Ui.combo();
    private final JLabel info = Ui.muted(" ");

    public TimeLinePage(AppModel model) {
        super(new BorderLayout(16, 16));
        this.model = model;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        by.addItem("Chronological (year)");
        by.addItem("Alphabetical (title)");
        order.addItem("Ascending");
        order.addItem("Descending");
        Ui.style(algo);

        JTable t = new JTable(events);
        Ui.styleTable(t);
        t.getColumnModel().getColumn(0).setMaxWidth(90);
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(Ui.scroll(t), BorderLayout.CENTER);
        left.add(info, BorderLayout.SOUTH);
        add(new Ui.Card("Historical Timeline", left), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 16));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(450, 100));
        right.add(new Ui.Card("Sort Controls", controls()), BorderLayout.NORTH);
        right.add(new Ui.Card("Algorithm Comparison", statsTable()), BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        stats.addRow(new Object[]{"Comparisons", "–", "–", "–"});
        stats.addRow(new Object[]{"Run time (avg)", "–", "–", "–"});
        stats.addRow(new Object[]{"Best case", Sorter.Algo.MERGE.best, Sorter.Algo.QUICK.best, Sorter.Algo.INSERTION.best});
        stats.addRow(new Object[]{"Average case", Sorter.Algo.MERGE.average, Sorter.Algo.QUICK.average, Sorter.Algo.INSERTION.average});
        stats.addRow(new Object[]{"Worst case", Sorter.Algo.MERGE.worst, Sorter.Algo.QUICK.worst, Sorter.Algo.INSERTION.worst});
        stats.addRow(new Object[]{"Extra space", Sorter.Algo.MERGE.space, Sorter.Algo.QUICK.space, Sorter.Algo.INSERTION.space});
        model.addListener(this::refresh);
        refresh();
    }

    private JPanel controls() {
        JPanel box = new JPanel(new BorderLayout(0, 8));
        box.setOpaque(false);
        JButton sort = Ui.button("Sort", Ui.Kind.PRIMARY);
        JButton all = Ui.button("Compare all 3");
        sort.addActionListener(e -> sortWith((Sorter.Algo) algo.getSelectedItem(), true));
        all.addActionListener(e -> {
            for (Sorter.Algo a : Sorter.Algo.values()) sortWith(a, a == algo.getSelectedItem());
            if (algo.getSelectedItem() == null) return;
        });
        Ui.Form f = new Ui.Form().row("Algorithm", algo).row("Sort by", by).row("Order", order)
                .full(Ui.row(8, sort, all));
        box.add(f, BorderLayout.NORTH);
        return box;
    }

    private JPanel statsTable() {
        JTable t = new JTable(stats);
        Ui.styleTable(t);
        t.setRowHeight(30);
        t.getColumnModel().getColumn(0).setPreferredWidth(104);
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.add(Ui.scroll(t), BorderLayout.CENTER);
        p.add(Ui.muted("<html>Comparisons are counted for the current events. Run time is the average of 500 runs.</html>"), BorderLayout.SOUTH);
        return p;
    }

    /** Sorts the current events; when show is true the table shows this result. */
    private void sortWith(Sorter.Algo a, boolean show) {
        List<Event> input = model.bst.preorder();   // not already sorted, so the sort has real work to do
        Sorter.Key key = by.getSelectedIndex() == 0 ? Sorter.Key.YEAR : Sorter.Key.TITLE;
        boolean asc = order.getSelectedIndex() == 0;
        Sorter.Result r = Sorter.sort(input, a, key, asc);
        int i = a.ordinal();
        stats.setValueAt(String.valueOf(r.comparisons), 0, i + 1);
        stats.setValueAt(String.format(Locale.ROOT, "%.1f µs", r.avgMicros), 1, i + 1);
        if (show) {
            show(r.sorted);
            info.setText(a.label + ": " + r.sorted.size() + " events sorted by " + (key == Sorter.Key.YEAR ? "year" : "title")
                    + ", " + (asc ? "ascending" : "descending") + ", " + r.comparisons + " comparisons.");
        }
    }

    private void show(List<Event> list) {
        events.setRowCount(0);
        for (Event e : list) events.addRow(new Object[]{e.year(), e.title()});
    }

    @Override
    public void refresh() {
        for (int i = 1; i <= 3; i++) {
            stats.setValueAt("–", 0, i);
            stats.setValueAt("–", 1, i);
        }
        show(model.bst.inorder());
        info.setText(model.bst.size() + " events, shown in chronological order. Choose an algorithm and press Sort.");
    }
}
