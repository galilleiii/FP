package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import lspu_hnn_core.ActivityLog;
import lspu_hnn_core.AppModel;

/** Admin only: every login, logout and data change, newest first, with a text filter. */
public class LogPage extends JPanel implements Refreshable {

    private final AppModel model;
    private final DefaultTableModel rows = new DefaultTableModel(new String[]{"Time", "User", "Action", "Detail"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(rows);
    private final JTextField filter = Ui.field(18);
    private final JLabel count = Ui.muted(" ");

    public LogPage(AppModel model) {
        super(new BorderLayout(16, 16));
        this.model = model;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JTable table = new JTable(rows);
        Ui.styleTable(table);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(500);

        filter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                apply();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                apply();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                apply();
            }
        });
        JButton reload = Ui.button("Refresh");
        reload.addActionListener(e -> refresh());

        JPanel top = Ui.row(10, Ui.muted("Filter"), filter, reload);
        JPanel box = new JPanel(new BorderLayout(0, 10));
        box.setOpaque(false);
        box.add(top, BorderLayout.NORTH);
        box.add(Ui.scroll(table), BorderLayout.CENTER);
        box.add(count, BorderLayout.SOUTH);
        add(new Ui.Card("Activity Log", box), BorderLayout.CENTER);
        refresh();
    }

    private void apply() {
        String t = filter.getText().trim();
        sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(t)));
        count.setText(sorter.getViewRowCount() + " of " + rows.getRowCount() + " entries shown");
    }

    @Override
    public void refresh() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ActivityLog.Entry> all = model.log.entries();
        rows.setRowCount(0);
        for (int i = all.size() - 1; i >= 0; i--) {
            ActivityLog.Entry e = all.get(i);
            rows.addRow(new Object[]{fmt.format(new Date(e.time())), e.user(), e.action(), e.detail()});
        }
        apply();
    }
}
