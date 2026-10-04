package daa.structures;

import daa.metrics.Metrics;

/**
 * Array-based binary min-heap of primitive {@code int} values.
 * For every index {@code i > 0}: {@code heap[(i - 1) / 2] <= heap[i]},
 * so the minimum is always at {@code heap[0]}.
 */
public class MinHeap {
    private static final int DEFAULT_CAPACITY = 8;

    private int[] heap;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        this(DEFAULT_CAPACITY);
    }

    public MinHeap(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("capacity must be positive: " + initialCapacity);
        }
        heap = new int[initialCapacity];
    }

    /** Adds {@code x} at the first free leaf and bubbles it up. O(log n). */
    public void insert(int x) {
        if (size == heap.length) {
            grow();
        }
        heap[size] = x;
        size++;
        siftUp(size - 1);
    }

    /** Returns the minimum without removing it. O(1). */
    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.step();
        return heap[0];
    }

    /** Removes and returns the minimum: the last leaf moves to the root and bubbles down. O(log n). */
    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        int min = heap[0];
        metrics.step();
        size--;
        if (size > 0) {
            heap[0] = heap[size];
            metrics.move();
            siftDown(0);
        }
        return min;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** Copy of the backing array in heap order. Helper for tests; not counted in the metrics. */
    public int[] toArray() {
        int[] copy = new int[size];
        for (int i = 0; i < size; i++) {
            copy[i] = heap[i];
        }
        return copy;
    }

    public Metrics metrics() {
        return metrics;
    }

    /** Bubble-up: swaps the element at {@code i} with its parent while it is smaller. */
    private void siftUp(int i) {
        int x = heap[i];
        while (i > 0) {
            int parent = (i - 1) / 2;
            int p = heap[parent];
            metrics.step();
            metrics.compare();
            if (p <= x) {
                break;
            }
            heap[i] = p;
            heap[parent] = x;
            metrics.moves(2);
            i = parent;
        }
    }

    /** Bubble-down: swaps the element at {@code i} with its smaller child while it is larger. */
    private void siftDown(int i) {
        int x = heap[i];
        while (true) {
            int left = 2 * i + 1;
            if (left >= size) {
                break;
            }
            int smallest = left;
            int s = heap[left];
            metrics.step();
            int right = left + 1;
            if (right < size) {
                int r = heap[right];
                metrics.step();
                metrics.compare();
                if (r < s) {
                    smallest = right;
                    s = r;
                }
            }
            metrics.compare();
            if (x <= s) {
                break;
            }
            heap[i] = s;
            heap[smallest] = x;
            metrics.moves(2);
            i = smallest;
        }
    }

    private void grow() {
        int[] bigger = new int[heap.length * 2];
        for (int i = 0; i < size; i++) {
            bigger[i] = heap[i];
            metrics.move();
        }
        heap = bigger;
    }
}
