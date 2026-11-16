package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;

import lspu_hnn_core.AppModel;

/** Module C: animated BFS and DFS between two campuses, with the queue/stack and a comparison table. */
public class RoutesPage extends JPanel {

    private final AppModel model;
    private final GraphCanvas canvas;
    private final FrontierPanel frontier = new FrontierPanel(false);
    private final RouteRunner runner;
    private final ComparisonPanel comparison;
    private final JComboBox<String> start = Ui.combo(), end = Ui.combo();
    private final JSlider speed = new JSlider(150, 1500, 650);
    private final JTextArea log = new JTextArea();

    public RoutesPage(AppModel model) {
        super(new BorderLayout(16, 16));
        this.model = model;
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        canvas = new GraphCanvas(model, false);
        runner = new RouteRunner(model, canvas, frontier, this::println, () -> 1700 - speed.getValue());
        comparison = new ComparisonPanel(model, false);

        JPanel top = new JPanel(new BorderLayout(16, 16));
        top.setOpaque(false);

        JPanel canvasBox = new JPanel(new BorderLayout(0, 8));
        canvasBox.setOpaque(false);
        canvasBox.add(canvas, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.setOpaque(false);
        south.add(frontier, BorderLayout.NORTH);
        south.add(Ui.legend(new String[]{"Current", "Visited", "In queue / stack", "Final path"},
                new Color[]{Theme.YELLOW, Theme.PASSED, Theme.ORANGE, Theme.GREEN}), BorderLayout.SOUTH);
        canvasBox.add(south, BorderLayout.SOUTH);
        top.add(new Ui.Card("Route Animation", canvasBox), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 16));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(350, 100));
        right.add(new Ui.Card("Route Controls", controls()), BorderLayout.NORTH);
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(Theme.font(Font.PLAIN, 12.5f));
        log.setBackground(Theme.PANEL_ALT);
        log.setForeground(Theme.TEXT);
        log.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        right.add(new Ui.Card("Step Log", Ui.scroll(log)), BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.CENTER);

        Ui.Card cmp = new Ui.Card("BFS vs DFS Comparison", comparison);
        cmp.setPreferredSize(new Dimension(100, 372));
        add(cmp, BorderLayout.SOUTH);

        model.addListener(this::refresh);
        refresh();
        println("Pick two campuses, then run BFS and DFS to compare them.");
    }

    private JPanel controls() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JButton bfs = Ui.button("Run BFS", Ui.Kind.PRIMARY);
        JButton dfs = Ui.button("Run DFS", Ui.Kind.PRIMARY);
        JButton reset = Ui.button("Reset");
        bfs.addActionListener(e -> runner.run(true, (String) start.getSelectedItem(), (String) end.getSelectedItem(), comparison::set));
        dfs.addActionListener(e -> runner.run(false, (String) start.getSelectedItem(), (String) end.getSelectedItem(), comparison::set));
        reset.addActionListener(e -> {
            runner.reset();
            println("Reset.");
        });
        box.add(new Ui.Form().row("Start", start).row("End", end));
        box.add(Ui.row(8, bfs, dfs, reset));
        JLabel sp = Ui.label("Animation speed", Font.BOLD, 12f, Theme.ACCENT);
        sp.setBorder(BorderFactory.createEmptyBorder(12, 0, 2, 0));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        box.add(sp);
        speed.setOpaque(false);
        box.add(speed);
        JPanel sl = new JPanel(new BorderLayout());
        sl.setOpaque(false);
        sl.add(Ui.muted("Slow"), BorderLayout.WEST);
        sl.add(Ui.muted("Fast"), BorderLayout.EAST);
        box.add(sl);
        return Ui.alignLeft(box);
    }

    private void refresh() {
        runner.reset();
        fill(start, 0);
        fill(end, model.graph.nodes().size() - 1);
    }

    private void fill(JComboBox<String> c, int defaultIndex) {
        Object sel = c.getSelectedItem();
        c.removeAllItems();
        for (String n : model.graph.nodes()) c.addItem(n);
        if (sel != null && model.graph.hasNode((String) sel)) c.setSelectedItem(sel);
        else if (c.getItemCount() > 0) c.setSelectedIndex(Math.max(0, Math.min(defaultIndex, c.getItemCount() - 1)));
    }

    private void println(String s) {
        log.append(s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }
}

