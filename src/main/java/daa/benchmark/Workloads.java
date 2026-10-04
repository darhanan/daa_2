package daa.benchmark;

import daa.structures.IntList;
import daa.structures.MinHeap;

public final class Workloads {
    private static long sink;

    private Workloads() {
    }

    public static Trial randomAccess(IntList list, Dataset d) {
        fill(list, d.values);
        long sum = 0;
        long start = System.nanoTime();
        for (int index : d.getIndices) {
            sum += list.get(index);
        }
        long elapsed = System.nanoTime() - start;
        sink += sum;
        return Trial.of(elapsed, list.metrics());
    }

    public static Trial search(IntList list, Dataset d) {
        fill(list, d.values);
        int hits = 0;
        long start = System.nanoTime();
        for (int q : d.searchQueries) {
            if (list.contains(q)) {
                hits++;
            }
        }
        long elapsed = System.nanoTime() - start;
        if (hits != d.expectedHits()) {
            throw new IllegalStateException("W2 found " + hits + " values, expected " + d.expectedHits());
        }
        return Trial.of(elapsed, list.metrics());
    }

    public static Trial insertRemove(IntList list, Dataset d, boolean middle) {
        fill(list, d.values);
        int index = middle ? d.size() / 2 : 0;
        long inserted = 0;
        long removed = 0;
        long start = System.nanoTime();
        for (int v : d.insertValues) {
            list.add(index, v);
            inserted += v;
        }
        for (int k = 0; k < d.insertValues.length; k++) {
            removed += list.remove(index);
        }
        long elapsed = System.nanoTime() - start;
        if (inserted != removed || list.size() != d.size()) {
            throw new IllegalStateException("W3 removed different elements than it inserted");
        }
        sink += removed;
        return Trial.of(elapsed, list.metrics());
    }

    public static Trial priority(MinHeap heap, Dataset d) {
        heap.metrics().reset();
        boolean sorted = true;
        long start = System.nanoTime();
        for (int v : d.values) {
            heap.insert(v);
        }
        int previous = Integer.MIN_VALUE;
        for (int k = 0; k < d.size(); k++) {
            int current = heap.extractMin();
            if (current < previous) {
                sorted = false;
            }
            previous = current;
        }
        long elapsed = System.nanoTime() - start;
        if (!sorted || !heap.isEmpty()) {
            throw new IllegalStateException("W4 extractMin returned values out of order");
        }
        return Trial.of(elapsed, heap.metrics());
    }

    public static Trial buildByInserts(int[] values) {
        MinHeap heap = new MinHeap(values.length);
        long start = System.nanoTime();
        for (int v : values) {
            heap.insert(v);
        }
        long elapsed = System.nanoTime() - start;
        Trial trial = Trial.of(elapsed, heap.metrics());
        sink += heap.peekMin();
        return trial;
    }

    public static Trial buildByFloyd(int[] values) {
        MinHeap heap = new MinHeap(values.length);
        long start = System.nanoTime();
        heap.buildHeap(values);
        long elapsed = System.nanoTime() - start;
        Trial trial = Trial.of(elapsed, heap.metrics());
        sink += heap.peekMin();
        return trial;
    }

    public static int[] descending(int n) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = n - i;
        }
        return values;
    }

    static long sink() {
        return sink;
    }

    private static void fill(IntList list, int[] values) {
        for (int v : values) {
            list.add(v);
        }
        list.metrics().reset();
    }
}
