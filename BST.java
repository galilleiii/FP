package lspu_hnn_core;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lspu_hnn_core.Step.Kind;

/**
 * Binary Search Tree of historical events, keyed by year.
 * No GUI code here. The plan* methods return the steps the GUI animates,
 * without changing the tree. The real change is done by insert / delete.
 */
public class BST {

    public static final class Node {
        public Event event;
        public Node left, right;

        Node(Event e) {
            event = e;
        }

        public int key() {
            return event.year();
        }
    }

    private Node root;
    private int size;

    public Node root() {
        return root;
    }

    public int size() {
        return size;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    // ---------------------------------------------------------------- insert / find

    /** Adds the event. Returns false if the year already exists. */
    public boolean insert(Event e) {
        if (root == null) {
            root = new Node(e);
            size++;
            return true;
        }
        Node cur = root;
        while (true) {
            if (e.year() == cur.key()) return false;
            if (e.year() < cur.key()) {
                if (cur.left == null) {
                    cur.left = new Node(e);
                    size++;
                    return true;
                }
                cur = cur.left;
            } else {
                if (cur.right == null) {
                    cur.right = new Node(e);
                    size++;
                    return true;
                }
                cur = cur.right;
            }
        }
    }

    public Node find(int year) {
        Node cur = root;
        while (cur != null && cur.key() != year) {
            cur = year < cur.key() ? cur.left : cur.right;
        }
        return cur;
    }

    // ---------------------------------------------------------------- delete

    /** Removes the year. Handles 0, 1 and 2 children. Returns false if not found. */
    public boolean delete(int year) {
        int before = size;
        root = delete(root, year);
        return size < before;
    }

    private Node delete(Node n, int year) {
        if (n == null) return null;
        if (year < n.key()) {
            n.left = delete(n.left, year);
        } else if (year > n.key()) {
            n.right = delete(n.right, year);
        } else {
            size--;
            if (n.left == null) return n.right;   // 0 or 1 child
            if (n.right == null) return n.left;   // 1 child
            Node succ = n.right;                  // 2 children: in-order successor
            while (succ.left != null) succ = succ.left;
            n.event = succ.event;
            size++;                               // the recursive call below removes the successor and counts it
            n.right = delete(n.right, succ.key());
        }
        return n;
    }

    // ---------------------------------------------------------------- animation plans

    public List<Step> planInsert(int year) {
        List<Step> s = new ArrayList<>();
        if (root == null) {
            s.add(new Step(Kind.INFO, -1, "The tree is empty, so " + year + " becomes the root"));
            return s;
        }
        Node cur = root;
        while (cur != null) {
            if (year == cur.key()) {
                s.add(new Step(Kind.INFO, -1, "Year " + year + " already exists"));
                return s;
            }
            boolean goLeft = year < cur.key();
            s.add(new Step(Kind.VISIT, cur.key(), "Compare " + year + (goLeft ? " < " : " > ") + cur.key()
                    + ": go " + (goLeft ? "left" : "right")));
            Node next = goLeft ? cur.left : cur.right;
            if (next == null) {
                s.add(new Step(Kind.INFO, -1, "Empty spot found: " + year + " becomes the "
                        + (goLeft ? "left" : "right") + " child of " + cur.key()));
            }
            cur = next;
        }
        return s;
    }

    public List<Step> planSearch(int year) {
        List<Step> s = new ArrayList<>();
        Node cur = root;
        while (cur != null) {
            if (year == cur.key()) {
                s.add(new Step(Kind.FOUND, cur.key(), "Found " + year + ": " + cur.event.title()));
                return s;
            }
            boolean goLeft = year < cur.key();
            s.add(new Step(Kind.VISIT, cur.key(), "Compare " + year + (goLeft ? " < " : " > ") + cur.key()
                    + ": go " + (goLeft ? "left" : "right")));
            cur = goLeft ? cur.left : cur.right;
        }
        s.add(new Step(Kind.NOT_FOUND, -1, "Year " + year + " is not in the tree"));
        return s;
    }

    /** Checks every node in in-order and marks titles that contain the keyword. */
    public List<Step> planKeywordSearch(String keyword) {
        List<Step> s = new ArrayList<>();
        String kw = keyword.toLowerCase(Locale.ROOT);
        int matches = 0;
        for (Event e : inorder()) {
            if (e.title().toLowerCase(Locale.ROOT).contains(kw)) {
                matches++;
                s.add(new Step(Kind.FOUND, e.year(), "Match at " + e.year() + ": " + e.title()));
            } else {
                s.add(new Step(Kind.VISIT, e.year(), "Check " + e.year() + ": no match"));
            }
        }
        s.add(new Step(matches > 0 ? Kind.INFO : Kind.NOT_FOUND, -1,
                matches > 0 ? matches + " match(es) for \"" + keyword + "\"" : "No title contains \"" + keyword + "\""));
        return s;
    }

    public List<Step> planDelete(int year) {
        List<Step> s = new ArrayList<>();
        Node cur = root;
        while (cur != null && cur.key() != year) {
            boolean goLeft = year < cur.key();
            s.add(new Step(Kind.VISIT, cur.key(), "Compare " + year + (goLeft ? " < " : " > ") + cur.key()
                    + ": go " + (goLeft ? "left" : "right")));
            cur = goLeft ? cur.left : cur.right;
        }
        if (cur == null) {
            s.add(new Step(Kind.NOT_FOUND, -1, "Year " + year + " is not in the tree"));
            return s;
        }
        int kids = (cur.left != null ? 1 : 0) + (cur.right != null ? 1 : 0);
        if (kids == 0) {
            s.add(new Step(Kind.TARGET, year, "Found " + year + ": no children, so the leaf is simply removed"));
        } else if (kids == 1) {
            Node child = cur.left != null ? cur.left : cur.right;
            s.add(new Step(Kind.TARGET, year, "Found " + year + ": one child (" + child.key()
                    + "), so the child takes its place"));
        } else {
            s.add(new Step(Kind.TARGET, year, "Found " + year + ": two children, so find its in-order successor "
                    + "(smallest year in the right subtree)"));
            Node succ = cur.right;
            while (succ.left != null) {
                s.add(new Step(Kind.VISIT, succ.key(), "Go to " + succ.key() + ", then keep going left"));
                succ = succ.left;
            }
            s.add(new Step(Kind.SUCCESSOR, succ.key(), "Successor is " + succ.key() + ": copy it into the node, "
                    + "then remove its old spot"));
        }
        return s;
    }

    // ---------------------------------------------------------------- traversals

    public List<Event> preorder() {
        List<Event> out = new ArrayList<>();
        pre(root, out);
        return out;
    }

    public List<Event> inorder() {
        List<Event> out = new ArrayList<>();
        in(root, out);
        return out;
    }

    public List<Event> postorder() {
        List<Event> out = new ArrayList<>();
        post(root, out);
        return out;
    }

    private void pre(Node n, List<Event> out) {
        if (n == null) return;
        out.add(n.event);
        pre(n.left, out);
        pre(n.right, out);
    }

    private void in(Node n, List<Event> out) {
        if (n == null) return;
        in(n.left, out);
        out.add(n.event);
        in(n.right, out);
    }

    private void post(Node n, List<Event> out) {
        if (n == null) return;
        post(n.left, out);
        post(n.right, out);
        out.add(n.event);
    }

    // ---------------------------------------------------------------- shape

    /** Number of levels (an empty tree is 0, a single node is 1). */
    public int height() {
        return height(root);
    }

    private int height(Node n) {
        return n == null ? 0 : 1 + Math.max(height(n.left), height(n.right));
    }

    /** True if, at every node, the left and right heights differ by at most 1. */
    public boolean isBalanced() {
        return balancedHeight(root) >= 0;
    }

    private int balancedHeight(Node n) {
        if (n == null) return 0;
        int l = balancedHeight(n.left);
        if (l < 0) return -1;
        int r = balancedHeight(n.right);
        if (r < 0 || Math.abs(l - r) > 1) return -1;
        return 1 + Math.max(l, r);
    }
}
