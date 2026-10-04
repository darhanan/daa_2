package daa.structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DynamicArrayTest extends IntListContractTest {

    @Override
    IntList create() {
        return new DynamicArray();
    }

    @Test
    void capacityDoublesWhenFull() {
        DynamicArray array = new DynamicArray(2);
        array.add(1);
        array.add(2);
        assertEquals(2, array.capacity());
        array.add(3);
        assertEquals(4, array.capacity());
        array.add(4);
        array.add(5);
        assertEquals(8, array.capacity());
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, array.get(i));
        }
    }

    @Test
    void growthCopiesEveryElementOnce() {
        DynamicArray array = new DynamicArray(1);
        for (int i = 0; i < 1024; i++) {
            array.add(i);
        }
        // copies at sizes 1, 2, 4, ..., 512 -> 1023 moves in total, i.e. amortized O(1) per add
        assertEquals(1023, array.metrics().moves());
    }

    @Test
    void invalidCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DynamicArray(0));
    }

    @Test
    void getCostsOneStep() {
        DynamicArray array = new DynamicArray();
        for (int i = 0; i < 100; i++) {
            array.add(i);
        }
        array.metrics().reset();
        array.get(0);
        array.get(99);
        assertEquals(2, array.metrics().steps());
    }

    @Test
    void addAndRemoveAtIndexShiftTheTail() {
        DynamicArray array = new DynamicArray(16);
        for (int i = 0; i < 10; i++) {
            array.add(i);
        }
        array.metrics().reset();
        array.add(0, -1);       // shifts 10 elements
        assertEquals(10, array.metrics().moves());
        array.remove(0);        // shifts 10 elements back
        assertEquals(20, array.metrics().moves());
        array.remove(9);        // last element: nothing to shift
        assertEquals(20, array.metrics().moves());
    }
}
