package daa.structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyLinkedListTest extends IntListContractTest {

    @Override
    IntList create() {
        return new MyLinkedList();
    }

    private MyLinkedList filled(int n) {
        MyLinkedList list = new MyLinkedList();
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        list.metrics().reset();
        return list;
    }

    @Test
    void getWalksFromTheNearerEnd() {
        MyLinkedList list = filled(100);
        assertEquals(10, list.get(10));
        assertEquals(11, list.metrics().steps());     // head + 10 hops

        list.metrics().reset();
        assertEquals(95, list.get(95));
        assertEquals(5, list.metrics().steps());      // tail + 4 hops back

        list.metrics().reset();
        list.get(0);
        list.get(99);
        assertEquals(2, list.metrics().steps());
    }

    @Test
    void insertAtHeadUpdatesLinksButShiftsNothing() {
        MyLinkedList list = filled(1000);
        list.add(0, -1);
        assertEquals(1, list.metrics().steps());
        assertEquals(4, list.metrics().moves());      // new.prev, new.next, old.prev, head
        list.remove(0);
        assertEquals(2, list.metrics().steps());
        assertEquals(6, list.metrics().moves());      // head, next.prev
    }

    @Test
    void appendUpdatesThreeLinks() {
        MyLinkedList list = filled(3);
        list.add(3);
        assertEquals(3, list.metrics().moves());
        assertEquals(0, list.metrics().steps());
    }
}
