package daa.structures;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MinHeapTest {
    private static void assertHeapProperty(MinHeap heap) {
        int[] a = heap.toArray();
        for (int child = 1; child < a.length; child++) {
            int parent = (child - 1) / 2;
            if (a[parent] > a[child]) {
                fail("heap property broken: a[" + parent + "]=" + a[parent] + " > a[" + child + "]=" + a[child]);
            }
        }
    }

    @Test
    void emptyHeapThrowsIllegalState() {
        MinHeap heap = new MinHeap();
        assertTrue(heap.isEmpty());
        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
    }

    @Test
    void heapThrowsAgainAfterBeingEmptied() {
        MinHeap heap = new MinHeap();
        heap.insert(1);
        heap.extractMin();
        assertThrows(IllegalStateException.class, heap::extractMin);
        assertThrows(IllegalStateException.class, heap::peekMin);
    }

    @Test
    void invalidCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new MinHeap(0));
    }

    @Test
    void oneElement() {
        MinHeap heap = new MinHeap();
        heap.insert(5);
        assertEquals(5, heap.peekMin());
        assertEquals(1, heap.size());
        assertEquals(5, heap.extractMin());
        assertTrue(heap.isEmpty());
    }

    @Test
    void peekDoesNotRemove() {
        MinHeap heap = new MinHeap();
        heap.insert(3);
        heap.insert(1);
        assertEquals(1, heap.peekMin());
        assertEquals(1, heap.peekMin());
        assertEquals(2, heap.size());
    }

    @Test
    void duplicatesAreKept() {
        MinHeap heap = new MinHeap();
        for (int v : new int[] {4, 1, 4, 1, 4}) {
            heap.insert(v);
            assertHeapProperty(heap);
        }
        int[] out = new int[5];
        for (int i = 0; i < 5; i++) {
            out[i] = heap.extractMin();
            assertHeapProperty(heap);
        }
        assertArrayEquals(new int[] {1, 1, 4, 4, 4}, out);
    }

    @Test
    void extremeValues() {
        MinHeap heap = new MinHeap();
        heap.insert(Integer.MAX_VALUE);
        heap.insert(0);
        heap.insert(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, heap.extractMin());
        assertEquals(0, heap.extractMin());
        assertEquals(Integer.MAX_VALUE, heap.extractMin());
    }

    @Test
    void growsBeyondInitialCapacity() {
        MinHeap heap = new MinHeap(1);
        for (int i = 100; i > 0; i--) {
            heap.insert(i);
        }
        assertEquals(100, heap.size());
        assertEquals(1, heap.peekMin());
        assertHeapProperty(heap);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 42, 2026})
    void heapPropertyHoldsAfterEveryInsertAndExtract(long seed) {
        Random rnd = new Random(seed);
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int op = 0; op < 5_000; op++) {
            if (expected.isEmpty() || rnd.nextInt(3) != 0) {
                int v = rnd.nextInt(1000) - 500;
                heap.insert(v);
                expected.add(v);
            } else {
                assertEquals((int) expected.poll(), heap.extractMin());
            }
            assertHeapProperty(heap);
            assertEquals(expected.size(), heap.size());
            if (!expected.isEmpty()) {
                assertEquals((int) expected.peek(), heap.peekMin());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 10, 1_000, 50_000})
    void nExtractMinCallsReturnSortedValues(int n) {
        Random rnd = new Random(n);
        int[] values = new int[n];
        MinHeap heap = new MinHeap();
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt();
            heap.insert(values[i]);
        }
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = heap.extractMin();
            if (i > 0) {
                assertTrue(out[i - 1] <= out[i], "output is not non-decreasing at " + i);
            }
        }
        assertFalse(heap.size() > 0);
        Arrays.sort(values);
        assertArrayEquals(values, out);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 7, 8, 1_000, 10_001})
    void buildHeapProducesValidHeap(int n) {
        Random rnd = new Random(n);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(100);
        }
        int[] original = values.clone();
        MinHeap heap = new MinHeap();
        heap.buildHeap(values);
        assertArrayEquals(original, values, "buildHeap must not modify its argument");
        assertEquals(n, heap.size());
        assertHeapProperty(heap);

        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = heap.extractMin();
            assertHeapProperty(heap);
        }
        Arrays.sort(original);
        assertArrayEquals(original, out);
    }

    @Test
    void buildHeapReplacesOldContentAndAllowsInserts() {
        MinHeap heap = new MinHeap();
        heap.insert(-100);
        heap.buildHeap(new int[] {5, 3, 8});
        assertEquals(3, heap.size());
        heap.insert(1);
        assertHeapProperty(heap);
        assertEquals(1, heap.extractMin());
        assertEquals(3, heap.extractMin());
    }

    @Test
    void buildHeapUsesFewerComparisonsThanRepeatedInserts() {
        int n = 100_000;
        int[] descending = new int[n];
        for (int i = 0; i < n; i++) {
            descending[i] = n - i;
        }
        MinHeap byInsert = new MinHeap();
        for (int v : descending) {
            byInsert.insert(v);
        }
        MinHeap byFloyd = new MinHeap();
        byFloyd.buildHeap(descending);
        assertTrue(byFloyd.metrics().comparisons() < 2L * n, "Floyd must be linear");
        assertTrue(byInsert.metrics().comparisons() > 10L * n, "inserts must be n log n");
    }
}
