package daa.benchmark;

import daa.structures.DynamicArray;
import daa.structures.MinHeap;
import daa.structures.MyLinkedList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkloadsTest {

    @Test
    void datasetIsReproducible() {
        Dataset a = Dataset.of(1_000);
        Dataset b = Dataset.of(1_000);
        assertArrayEquals(a.values, b.values);
        assertArrayEquals(a.getIndices, b.getIndices);
        assertArrayEquals(a.searchQueries, b.searchQueries);
        assertArrayEquals(a.insertValues, b.insertValues);
    }

    @Test
    void randomAccessCountsOneStepPerGetInArrayAndMoreInList() {
        Dataset d = Dataset.of(1_000);
        Trial array = Workloads.randomAccess(new DynamicArray(), d);
        Trial list = Workloads.randomAccess(new MyLinkedList(), d);
        assertEquals(Dataset.GET_QUERIES, array.steps());
        assertTrue(list.steps() > 100L * Dataset.GET_QUERIES);
    }

    @Test
    void searchPerformsTheSameStepsInBothStructures() {
        Dataset d = Dataset.of(1_000);
        Trial array = Workloads.search(new DynamicArray(), d);
        Trial list = Workloads.search(new MyLinkedList(), d);
        assertEquals(array.steps(), list.steps());
        assertEquals(array.comparisons(), list.comparisons());
    }

    @Test
    void insertRemoveCountsWorkInBothStructures() {
        Dataset d = Dataset.of(1_000);
        Trial arrayHead = Workloads.insertRemove(new DynamicArray(), d, false);
        Trial listHead = Workloads.insertRemove(new MyLinkedList(), d, false);
        Trial listMiddle = Workloads.insertRemove(new MyLinkedList(), d, true);
        assertTrue(arrayHead.moves() > 1_000_000L, "array shifts about n elements per operation");
        assertEquals(6L * Dataset.EDIT_OPERATIONS, listHead.moves(), "4 links per insert, 2 per remove");
        assertTrue(listMiddle.steps() > 500L * Dataset.EDIT_OPERATIONS);
    }

    @Test
    void priorityProcessingCompletesAndCounts() {
        Trial t = Workloads.priority(new MinHeap(), Dataset.of(10_000));
        assertTrue(t.comparisons() > 0);
        assertTrue(t.moves() > 0);
    }

    @Test
    void medianOfFiveIsTheMiddleTime() {
        long[] times = {999, 999, 999, 999, 999, 50, 10, 40, 20, 30};
        int[] call = {0};
        BenchmarkRunner.Case c = () -> new Trial(times[call[0]++], 1, 1, 1);
        // the 5 slow warm-up runs are discarded; measured 50, 10, 40, 20, 30 -> median 30
        assertEquals(30, BenchmarkRunner.measure(c).nanos());
    }
}
