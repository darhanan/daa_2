package daa.structures;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class IntListContractTest {
    abstract IntList create();

    private IntList listOf(int... values) {
        IntList list = create();
        for (int v : values) {
            list.add(v);
        }
        return list;
    }

    @Test
    void emptyListHasSizeZero() {
        IntList list = create();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertFalse(list.contains(0));
        assertArrayEquals(new int[0], list.toArray());
    }

    @Test
    void emptyListRejectsGetAndRemove() {
        IntList list = create();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    @Test
    void addAtIndexZeroOfEmptyList() {
        IntList list = create();
        list.add(0, 7);
        assertArrayEquals(new int[] {7}, list.toArray());
    }

    @Test
    void oneElement() {
        IntList list = listOf(42);
        assertEquals(1, list.size());
        assertEquals(42, list.get(0));
        assertTrue(list.contains(42));
        assertFalse(list.contains(41));
        assertEquals(42, list.remove(0));
        assertTrue(list.isEmpty());
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }

    @Test
    void listIsReusableAfterBecomingEmpty() {
        IntList list = listOf(1, 2);
        list.remove(0);
        list.remove(0);
        list.add(3);
        list.add(0, 4);
        assertArrayEquals(new int[] {4, 3}, list.toArray());
    }

    @Test
    void duplicateValues() {
        IntList list = listOf(5, 5, 3, 5);
        assertEquals(4, list.size());
        assertTrue(list.contains(5));
        assertEquals(5, list.remove(1));
        assertArrayEquals(new int[] {5, 3, 5}, list.toArray());
        list.remove(0);
        list.remove(1);
        assertFalse(list.contains(5));
        assertTrue(list.contains(3));
    }

    @Test
    void extremeIntValues() {
        IntList list = listOf(Integer.MIN_VALUE, 0, Integer.MAX_VALUE, -1);
        assertTrue(list.contains(Integer.MIN_VALUE));
        assertTrue(list.contains(Integer.MAX_VALUE));
        assertEquals(-1, list.get(3));
    }

    @Test
    void firstAndLastIndex() {
        IntList list = listOf(10, 20, 30, 40);
        assertEquals(10, list.get(0));
        assertEquals(40, list.get(list.size() - 1));

        list.add(0, 5);
        list.add(list.size(), 50);
        assertArrayEquals(new int[] {5, 10, 20, 30, 40, 50}, list.toArray());

        assertEquals(5, list.remove(0));
        assertEquals(50, list.remove(list.size() - 1));
        assertArrayEquals(new int[] {10, 20, 30, 40}, list.toArray());
    }

    @Test
    void insertAndRemoveInTheMiddle() {
        IntList list = listOf(1, 2, 4, 5);
        list.add(2, 3);
        assertArrayEquals(new int[] {1, 2, 3, 4, 5}, list.toArray());
        assertEquals(3, list.remove(2));
        assertArrayEquals(new int[] {1, 2, 4, 5}, list.toArray());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void getAndRemoveRejectInvalidIndex(int index) {
        IntList list = listOf(1, 2, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(index));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(index));
        assertArrayEquals(new int[] {1, 2, 3}, list.toArray());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 4, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void addRejectsInvalidIndex(int index) {
        IntList list = listOf(1, 2, 3);
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(index, 9));
        assertArrayEquals(new int[] {1, 2, 3}, list.toArray());
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 42, 2026})
    void randomOperationsMatchArrayList(long seed) {
        Random rnd = new Random(seed);
        IntList list = create();
        List<Integer> expected = new ArrayList<>();
        for (int op = 0; op < 20_000; op++) {
            int choice = rnd.nextInt(6);
            int value = rnd.nextInt(200) - 100;
            if (choice == 0 || expected.isEmpty()) {
                list.add(value);
                expected.add(value);
            } else if (choice == 1) {
                int index = rnd.nextInt(expected.size() + 1);
                list.add(index, value);
                expected.add(index, value);
            } else if (choice == 2) {
                int index = rnd.nextInt(expected.size());
                assertEquals((int) expected.remove(index), list.remove(index));
            } else if (choice == 3) {
                int index = rnd.nextInt(expected.size());
                assertEquals((int) expected.get(index), list.get(index));
            } else {
                assertEquals(expected.contains(value), list.contains(value));
            }
            assertEquals(expected.size(), list.size());
        }
        int[] expectedArray = expected.stream().mapToInt(Integer::intValue).toArray();
        assertArrayEquals(expectedArray, list.toArray());
    }

    @Test
    void containsCountsOneStepAndOneComparisonPerElementVisited() {
        IntList list = listOf(4, 8, 15, 16, 23, 42);
        list.metrics().reset();
        assertTrue(list.contains(15));
        assertEquals(3, list.metrics().steps());
        assertEquals(3, list.metrics().comparisons());

        list.metrics().reset();
        assertFalse(list.contains(99));
        assertEquals(6, list.metrics().steps());
        assertEquals(6, list.metrics().comparisons());
    }

    @Test
    void insertAtHeadIsNeverFreeInTheCounters() {
        IntList list = listOf(1, 2, 3, 4, 5);
        list.metrics().reset();
        list.add(0, 0);
        list.remove(0);
        assertTrue(list.metrics().moves() > 0, "W3 must not report zero moves");
    }
}
