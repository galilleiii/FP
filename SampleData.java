package lspu_hnn_core;


/**
 * Starting data. The events come from LSPU's published history (Wikipedia, "Laguna State Polytechnic
 * University"); an admin can edit them. The distances are sample values for the demo, not surveyed roads.
 * Events are inserted in this order so the tree starts with 8 nodes and 4 levels.
 */
final class SampleData {

    private SampleData() {
    }

    static void fillEvents(BST bst) {
        bst.clear();
        Object[][] rows = {
                {1984, "Became Laguna State Polytechnic College (BP Blg. 482)"},
                {1957, "Became Baybay National Agricultural and Vocational School (RA 1807)"},
                {2000, "San Pablo City campus turned over; three-campus integration formalized"},
                {1952, "Baybay Provincial High School opened in Siniloan"},
                {1971, "Became Baybay National College of Agriculture and Technology"},
                {1993, "Named a Provincial Technical Institute of Agriculture in Region IV"},
                {2007, "Became Laguna State Polytechnic University (RA 9402)"},
                {1960, "Sta. Cruz and San Pablo campuses began operations"},
        };
        for (Object[] r : rows) bst.insert(new Event((Integer) r[0], (String) r[1]));
    }

    /** Same connections as the adjacency matrix in the project mockup. */
    static void fillGraph(Graph g) {
        g.clear();
        g.addNode("Siniloan", 0.12, 0.30);
        g.addNode("Sta. Cruz", 0.62, 0.16);
        g.addNode("Los Baños", 0.36, 0.78);
        g.addNode("San Pablo", 0.88, 0.70);
        g.setEdge("Siniloan", "Sta. Cruz", 24);
        g.setEdge("Sta. Cruz", "Los Baños", 18);
        g.setEdge("Sta. Cruz", "San Pablo", 36);
        g.setEdge("Los Baños", "San Pablo", 28);
    }
}

