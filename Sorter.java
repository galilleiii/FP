package lspu_hnn_core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Merge sort, quick sort and insertion sort written by hand (no Collections.sort), with a comparison counter. */
public final class Sorter {

    private Sorter() {
    }

    public enum Key {YEAR, TITLE}

    public enum Algo {
        MERGE("Merge Sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)"),
        QUICK("Quick Sort", "O(n log n)", "O(n log n)", "O(n^2)", "O(log n)"),
        INSERTION("Insertion Sort", "O(n)", "O(n^2)", "O(n^2)", "O(1)");

        public final String label, best, average, worst, space;

        Algo(String label, String best, String average, String worst, String space) {
            this.label = label;
            this.best = best;
            this.average = average;
            this.worst = worst;
            this.space = space;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static final class Result {
        public List<Event> sorted;
        public long comparisons;
        public double avgMicros;
        public Algo algo;
    }

    private static final class Counter {
        long n;
    }

    public static Result sort(List<Event> input, Algo algo, Key key, boolean ascending) {
        Comparator<Event> base = key == Key.YEAR
                ? Comparator.comparingInt(Event::year)
                : (a, b) -> {
                    int c = a.title().compareToIgnoreCase(b.title());
                    return c != 0 ? c : Integer.compare(a.year(), b.year());
                };
        Comparator<Event> cmp = ascending ? base : base.reversed();

        Counter counter = new Counter();
        Comparator<Event> counting = (a, b) -> {
            counter.n++;
            return cmp.compare(a, b);
        };
        Event[] arr = input.toArray(new Event[0]);
        run(algo, arr, counting);

        Result r = new Result();
        r.algo = algo;
        r.sorted = new ArrayList<>(Arrays.asList(arr));
        r.comparisons = counter.n;

        for (int i = 0; i < 200; i++) run(algo, input.toArray(new Event[0]), cmp);   // warm-up
        int runs = 500;
        long t0 = System.nanoTime();
        for (int i = 0; i < runs; i++) run(algo, input.toArray(new Event[0]), cmp);
        r.avgMicros = (System.nanoTime() - t0) / 1000.0 / runs;
        return r;
    }

    private static void run(Algo algo, Event[] a, Comparator<Event> c) {
        switch (algo) {
            case MERGE -> mergeSort(a, new Event[a.length], 0, a.length, c);
            case QUICK -> quickSort(a, 0, a.length - 1, c);
            case INSERTION -> insertionSort(a, c);
        }
    }

    private static void insertionSort(Event[] a, Comparator<Event> c) {
        for (int i = 1; i < a.length; i++) {
            Event x = a[i];
            int j = i - 1;
            while (j >= 0 && c.compare(a[j], x) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = x;
        }
    }

    private static void mergeSort(Event[] a, Event[] tmp, int lo, int hi, Comparator<Event> c) {
        if (hi - lo < 2) return;
        int mid = (lo + hi) >>> 1;
        mergeSort(a, tmp, lo, mid, c);
        mergeSort(a, tmp, mid, hi, c);
        int i = lo, j = mid, k = lo;
        while (i < mid && j < hi) tmp[k++] = c.compare(a[j], a[i]) < 0 ? a[j++] : a[i++];
        while (i < mid) tmp[k++] = a[i++];
        while (j < hi) tmp[k++] = a[j++];
        System.arraycopy(tmp, lo, a, lo, hi - lo);
    }

    /** Lomuto partition with the middle element as pivot; recurses into the smaller side first. */
    private static void quickSort(Event[] a, int lo, int hi, Comparator<Event> c) {
        while (lo < hi) {
            swap(a, (lo + hi) >>> 1, hi);
            Event pivot = a[hi];
            int i = lo;
            for (int j = lo; j < hi; j++) {
                if (c.compare(a[j], pivot) < 0) swap(a, i++, j);
            }
            swap(a, i, hi);
            if (i - lo < hi - i) {
                quickSort(a, lo, i - 1, c);
                lo = i + 1;
            } else {
                quickSort(a, i + 1, hi, c);
                hi = i - 1;
            }
        }
    }

    private static void swap(Event[] a, int i, int j) {
        Event t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}

