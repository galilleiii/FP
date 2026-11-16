package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.file.Path;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import lspu_hnn_core.AppModel;

/** Admin only: CSV import / export of events and connections, and restoring the sample data. */
public class SettingsPage extends JPanel {

    private final AppModel model;
    private final Path dataDir;
    private final JLabel status = Ui.muted(" ");

    public SettingsPage(AppModel model, Path dataDir) {
        super(new BorderLayout(16, 16));
        this.model = model;
        this.dataDir = dataDir;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JPanel grid = new JPanel(new GridLayout(1, 2, 16, 16));
        grid.setOpaque(false);
        grid.add(new Ui.Card("Heritage events (CSV)", block(
                "Columns: year,title. Importing keeps existing events and adds only years that are not in the tree yet.",
                action("Export events", false, this::exportEvents),
                action("Import events", false, this::importEvents))));
        grid.add(new Ui.Card("Campus connections (CSV)", block(
                "Columns: from,to,km. Importing adds missing campuses and updates distances.",
                action("Export connections", false, this::exportEdges),
                action("Import connections", false, this::importEdges))));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(grid, BorderLayout.NORTH);

        JButton reset = Ui.button("Restore sample data", Ui.Kind.DANGER);
        reset.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "Replace all events and campuses with the sample data?\nAccounts and the activity log are not touched.",
                    "Restore sample data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                model.resetSampleData();
                status.setText("Sample data restored.");
            }
        });
        JPanel data = block("Saved files live in: " + dataDir.toAbsolutePath()
                + ". Every change is saved right away. Restoring sample data brings back the original 8 events and 4 campuses.", 900, reset);
        top.add(new Ui.Card("Data", data), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);
        add(status, BorderLayout.SOUTH);
    }

    private JPanel block(String text, JButton... buttons) {
        return block(text, 360, buttons);
    }

    private JPanel block(String text, int width, JButton... buttons) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel l = Ui.muted("<html><div style='width:" + width + "px'>" + text + "</div></html>");
        l.setAlignmentX(LEFT_ALIGNMENT);
        box.add(l);
        box.add(javax.swing.Box.createVerticalStrut(12));
        JPanel row = Ui.row(8, buttons);
        row.setAlignmentX(LEFT_ALIGNMENT);
        box.add(row);
        return box;
    }

    private interface FileJob {
        void run(Path p);
    }

    private JButton action(String label, boolean save, FileJob job) {
        JButton b = Ui.button(label);
        boolean exporting = label.startsWith("Export");
        b.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
            if (exporting) fc.setSelectedFile(new java.io.File(label.contains("events") ? "lspu_events.csv" : "lspu_connections.csv"));
            int r = exporting ? fc.showSaveDialog(this) : fc.showOpenDialog(this);
            if (r != JFileChooser.APPROVE_OPTION) return;
            Path p = fc.getSelectedFile().toPath();
            if (exporting && !p.getFileName().toString().toLowerCase().endsWith(".csv")) p = p.resolveSibling(p.getFileName() + ".csv");
            try {
                job.run(p);
            } catch (RuntimeException ex) {
                status.setText(ex.getMessage());
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not finish", JOptionPane.WARNING_MESSAGE);
            }
        });
        return b;
    }

    private void exportEvents(Path p) {
        model.exportEvents(p);
        status.setText("Exported events to " + p);
    }

    private void importEvents(Path p) {
        int[] r = model.importEvents(p);
        status.setText("Imported events: " + r[0] + " added, " + r[1] + " skipped (duplicate years or invalid rows).");
    }

    private void exportEdges(Path p) {
        model.exportEdges(p);
        status.setText("Exported connections to " + p);
    }

    private void importEdges(Path p) {
        status.setText("Imported " + model.importEdges(p) + " connections.");
    }
}

