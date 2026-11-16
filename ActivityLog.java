package lspu_hnn_core;

import java.util.ArrayList;
import java.util.List;

/** Who did what and when. Saved to activity_log.txt, one line per action. */
public class ActivityLog {

    public record Entry(long time, String user, String action, String detail) {
    }

    private final List<Entry> entries;
    private final Storage storage;

    ActivityLog(Storage storage) {
        this.storage = storage;
        this.entries = new ArrayList<>(storage.loadLog());
    }

    public synchronized void add(String user, String action, String detail) {
        Entry e = new Entry(System.currentTimeMillis(), user, action, detail);
        entries.add(e);
        storage.appendLog(e);
    }

    public synchronized List<Entry> entries() {
        return List.copyOf(entries);
    }
}
