package lspu_hnn_ui;


import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import javax.swing.Timer;

/**
 * Plays a list of steps one by one on the Swing timer (so the screen never freezes).
 * Used for BST operations, tree traversals and BFS / DFS, so the animation code is written once.
 */
public final class StepPlayer<T> {

    private final IntSupplier delayMs;
    private Timer timer;

    public StepPlayer(IntSupplier delayMs) {
        this.delayMs = delayMs;
    }

    public boolean running() {
        return timer != null && timer.isRunning();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }

    /** onStep runs for every item; onDone runs once after the last one (one delay later, so the last step is visible). */
    public void play(List<T> items, Consumer<T> onStep, Runnable onDone) {
        stop();
        int[] i = {0};
        timer = new Timer(delayMs.getAsInt(), null);
        timer.setInitialDelay(0);
        timer.addActionListener(e -> {
            timer.setDelay(Math.max(1, delayMs.getAsInt()));
            if (i[0] < items.size()) {
                onStep.accept(items.get(i[0]++));
            } else {
                timer.stop();
                if (onDone != null) onDone.run();
            }
        });
        timer.start();
    }
}
