package lspu_hnn_ui;


import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lspu_hnn_core.AppModel;

/** Module B, dual view: the adjacency matrix and the adjacency list update live next to the graph. */
public class RepresentationsPage extends JPanel {

    public RepresentationsPage(AppModel model) {
        super(new BorderLayout(16, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 16));
        left.setOpaque(false);

        JPanel matrix = new JPanel(new BorderLayout(0, 6));
        matrix.setOpaque(false);
        JLabel hint = Ui.muted(model.graph.isDirected() ? "Row = from, column = to. INF = no direct connection."
                : "Undirected: the matrix is symmetric. INF = no direct connection.");
        model.addListener(() -> hint.setText(model.graph.isDirected()
                ? "Directed: row = from, column = to. INF = no direct connection."
                : "Undirected: the matrix is symmetric. INF = no direct connection."));
        matrix.add(hint, BorderLayout.NORTH);
        matrix.add(Ui.scroll(new MatrixPanel(model, false)), BorderLayout.CENTER);
        left.add(new Ui.Card("Adjacency Matrix", matrix));

        JPanel list = new JPanel(new BorderLayout(0, 6));
        list.setOpaque(false);
        list.add(Ui.muted("Each campus points to its neighbors as [campus · km]. The last pointer is null."), BorderLayout.NORTH);
        list.add(Ui.scroll(new AdjListPanel(model, false)), BorderLayout.CENTER);
        left.add(new Ui.Card("Adjacency List", list));
        add(left, BorderLayout.CENTER);

        GraphCanvas canvas = new GraphCanvas(model, false);
        JPanel cbox = new JPanel(new BorderLayout());
        cbox.setOpaque(false);
        cbox.add(canvas, BorderLayout.CENTER);
        Ui.Card card = new Ui.Card("Graph (live)", cbox);
        card.setPreferredSize(new java.awt.Dimension(430, 100));
        add(card, BorderLayout.EAST);
    }
}

