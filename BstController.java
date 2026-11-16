package lspu_hnn_ui;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import lspu_hnn_core.AppModel;
import lspu_hnn_core.Event;
import lspu_hnn_core.Step;

/**
 * Runs BST operations as animations on a TreePanel. Both the BST page and the dashboard use it.
 * The tree itself is only changed at the end of the animation, through AppModel (so admin checks apply).
 */
public final class BstController {

    private final AppModel model;
    private final TreePanel tree;
    private final Consumer<String> out;
    private final StepPlayer<Step> player;
    private final Set<Integer> finals = new HashSet<>();
    private Integer cur;
    private Color passed = Theme.PASSED;

    public BstController(AppModel model, TreePanel tree, Consumer<String> out, IntSupplier delayMs) {
        this.model = model;
        this.tree = tree;
        this.out = out;
        this.player = new StepPlayer<>(delayMs);
    }

    public boolean busy() {
        return player.running();
    }

    public void reset() {
        player.stop();
        begin(Theme.PASSED);
        tree.setSelected(null);
    }

    private void begin(Color passedColor) {
        finals.clear();
        cur = null;
        passed = passedColor;
        tree.clearMarks();
    }

    private boolean refuseIfBusy() {
        if (busy()) {
            out.accept("Wait for the current animation to finish (or press Reset).");
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- one animation step

    private void settlePrevious(int newKey) {
        if (cur != null && cur != newKey && !finals.contains(cur)) tree.mark(cur, passed);
    }

    private void apply(Step s) {
        switch (s.kind()) {
            case VISIT -> {
                settlePrevious(s.key());
                cur = s.key();
                tree.mark(cur, Theme.YELLOW);
            }
            case FOUND -> {
                settlePrevious(s.key());
                cur = s.key();
                finals.add(s.key());
                tree.mark(s.key(), Theme.GREEN);
            }
            case TARGET -> {
                settlePrevious(s.key());
                cur = s.key();
                finals.add(s.key());
                tree.mark(s.key(), Theme.RED);
            }
            case SUCCESSOR -> {
                settlePrevious(s.key());
                cur = s.key();
                finals.add(s.key());
                tree.mark(s.key(), Theme.ORANGE);
            }
            default -> {
            }
        }
        out.accept("  " + s.message());
    }

    private void settleLast() {
        if (cur != null && !finals.contains(cur)) tree.mark(cur, passed);
    }

    // ---------------------------------------------------------------- operations

    public void insert(int year, String title) {
        if (refuseIfBusy()) return;
        begin(Theme.PASSED);
        if (model.bst.find(year) != null) {
            out.accept("Cannot insert: " + year + " already exists. Each year can be used once.");
            tree.mark(year, Theme.RED);
            return;
        }
        out.accept("Insert " + year + " – " + title);
        player.play(model.bst.planInsert(year), this::apply, () -> {
            settleLast();
            try {
                model.insertEvent(year, title);
                finals.add(year);
                tree.mark(year, Theme.GREEN);
                out.accept("Inserted " + year);
            } catch (RuntimeException ex) {
                out.accept("Could not insert: " + ex.getMessage());
            }
        });
    }

    public void delete(int year) {
        if (refuseIfBusy()) return;
        begin(Theme.PASSED);
        List<Step> plan = model.bst.planDelete(year);
        if (model.bst.find(year) == null) {
            out.accept("Delete " + year);
            player.play(plan, this::apply, this::settleLast);
            return;
        }
        Integer successor = null;
        for (Step s : plan) if (s.kind() == Step.Kind.SUCCESSOR) successor = s.key();
        Integer succ = successor;
        out.accept("Delete " + year);
        player.play(plan, this::apply, () -> {
            try {
                model.deleteEvent(year);
                tree.clearMarks();
                finals.clear();
                cur = null;
                if (succ != null) {
                    tree.mark(succ, Theme.GREEN);
                    out.accept("Deleted " + year + ". The node now holds " + succ + ".");
                } else {
                    out.accept("Deleted " + year);
                }
            } catch (RuntimeException ex) {
                out.accept("Could not delete: " + ex.getMessage());
            }
        });
    }

    /** A number searches by year (follows the BST path). Anything else searches titles for the keyword. */
    public void search(String query) {
        if (refuseIfBusy()) return;
        query = query.trim();
        if (query.isEmpty()) {
            out.accept("Type a year or a keyword first.");
            return;
        }
        begin(Theme.PASSED);
        List<Step> plan;
        try {
            int year = Integer.parseInt(query);
            out.accept("Search for year " + year);
            plan = model.bst.planSearch(year);
        } catch (NumberFormatException ex) {
            out.accept("Search titles for \"" + query + "\"");
            plan = model.bst.planKeywordSearch(query);
        }
        player.play(plan, this::apply, this::settleLast);
    }

    public void traverse(String name, List<Event> order) {
        if (refuseIfBusy()) return;
        begin(Theme.GREEN);
        if (order.isEmpty()) {
            out.accept("The tree is empty.");
            return;
        }
        out.accept(name + " traversal");
        List<Step> steps = new ArrayList<>();
        StringBuilder seq = new StringBuilder();
        for (Event e : order) {
            steps.add(new Step(Step.Kind.VISIT, e.year(), "visit " + e.year()));
            seq.append(seq.length() == 0 ? "" : ", ").append(e.year());
        }
        player.play(steps, this::apply, () -> {
            settleLast();
            out.accept("Output: " + seq);
        });
    }
}
