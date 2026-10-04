package daa.structures;

import daa.metrics.Metrics;

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

    public void insert(int x) {
        if (size == heap.length) {
            grow();
        }
        heap[size] = x;
        size++;
        siftUp(size - 1);
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.step();
        return heap[0];
    }

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

    public void buildHeap(int[] array) {
        heap = new int[Math.max(array.length, DEFAULT_CAPACITY)];
        for (int i = 0; i < array.length; i++) {
            heap[i] = array[i];
        }
        size = array.length;
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

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
