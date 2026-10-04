package daa.structures;

import daa.metrics.Metrics;

/**
 * Common interface of {@link DynamicArray} and {@link MyLinkedList},
 * so one benchmark can run both structures with exactly the same code.
 */
public interface IntList {

    /** Appends {@code x} to the end. */
    void add(int x);

    /**
     * Inserts {@code x} at {@code index}, shifting later elements to the right.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0 || index > size()}
     */
    void add(int index, int x);

    /**
     * Removes and returns the element at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}
     */
    int remove(int index);

    /**
     * Returns the element at {@code index}.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0 || index >= size()}
     */
    int get(int index);

    /** Returns {@code true} if {@code x} is stored in the structure. */
    boolean contains(int x);

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    /** Copy of the elements in order. Helper for tests; not counted in the metrics. */
    int[] toArray();

    /** Operation counters of this structure. */
    Metrics metrics();
}
